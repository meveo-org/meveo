/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.custom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.JsonUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.crm.custom.PrimitiveTypeEnum;
import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.persistence.neo4j.service.Neo4jService;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.meveo.service.index.ElasticClient;
import org.meveo.util.EntityCustomizationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * @author Clément Bareth
 * @author Wassim Drira
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.9.0
 */
@Stateless
public class CustomEntityTemplateService extends BusinessService<CustomEntityTemplate> {

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private SqlConfigurationService sqlConfigurationService;

    @Inject
    private PermissionService permissionService;

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCache;

    @Inject
    private ElasticClient elasticClient;

    @Inject
    private ParamBeanFactory paramBeanFactory;

    @Inject
    private CustomTableCreatorService customTableCreatorService;

    @Inject
    private CustomEntityInstanceService customEntityInstanceService;

    @Inject
    private Neo4jService neo4jService;
    
    @Inject
    private CustomEntityCategoryService customEntityCategoryService;

    @Inject
    private EntityCustomActionService entityCustomActionService;

    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;

    private static boolean useCETCache = true;

    private final static String CLASSES_DIR = "/classes";

    @PostConstruct
    private void init() {
        useCETCache = Boolean.parseBoolean(ParamBean.getInstance().getProperty("cache.cacheCET", "true"));
    }

    /**
     * @param currentUser Logged user
     * @return the classes directory relative to the file explorer directory for the user's provider
     */
    public static String getClassesDirectory(MeveoUser currentUser) {
        String rootDir = ParamBean.getInstance().getChrootDir(currentUser != null ? currentUser.getProviderCode() : null);
        return rootDir + CLASSES_DIR;
    }

    public static File getClassesDir(MeveoUser currentUser) {
        return new File(getClassesDirectory(currentUser));
    }
    
    /**
     * @param cet The parent template
     * @return the sub-templates of the given template
     */
    public List<CustomEntityTemplate> getSubTemplates(CustomEntityTemplate cet) {
    	/* CustomEntityTemplate result = (CustomEntityTemplate) getEntityManager()
    			.createQuery("SELECT subTemplates FROM CustomEntityTemplate cet WHERE cet.id = :id")
    			.setParameter("id", cet.getId())
    			.getSingleResult();
		return result.getSubTemplates();*/ 
    	return getEntityManager()
			.createQuery("FROM CustomEntityTemplate cet WHERE cet.superTemplate.id = :id", CustomEntityTemplate.class)
			.setParameter("id", cet.getId())
			.getResultList();
    	
    }
    
    @Override
    public void create(CustomEntityTemplate cet) throws BusinessException {
    	
        if (!EntityCustomizationUtils.validateOntologyCode(cet.getCode())) {
            throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
        }
        
		ParamBean paramBean = paramBeanFactory.getInstance();
                
        super.create(cet);
        
        customFieldsCache.addUpdateCustomEntityTemplate(cet);

        if (cet.getSqlStorageConfiguration() != null && cet.getSqlStorageConfiguration().isStoreAsTable()) {
            customTableCreatorService.createTable(cet);
        }

        elasticClient.createCETMapping(cet);

        try {
            permissionService.createIfAbsent(cet.getModifyPermission(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
            permissionService.createIfAbsent(cet.getReadPermission(), paramBean.getProperty("role.readAllCE", "ReadAllCE"));

            /* If cet is a primitive type, create custom field of corresponding type */
            if (cet.getNeo4JStorageConfiguration() != null && cet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
                createPrimitiveCft(cet);
            }

            // Create neoj4 indexes
            if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
                neo4jService.addUUIDIndexes(cet);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createPrimitiveCft(CustomEntityTemplate cet) throws BusinessException {
        // Define CFT
        final CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
        CustomEntityTemplateUtils.turnIntoPrimitive(cet, customFieldTemplate);
        // Create CFT
        customFieldTemplateService.create(customFieldTemplate);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Asynchronous
    protected void afterUpdate(CustomEntityTemplate cet) throws BusinessException {
        /* Primitive entity and type management */
        if (cet.getNeo4JStorageConfiguration() != null && cet.getNeo4JStorageConfiguration().isPrimitiveEntity() && cet.getNeo4JStorageConfiguration().getPrimitiveType() != null) {
            final Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
            CustomFieldTemplate valueCft = cfts.get(CustomEntityTemplateUtils.PRIMITIVE_CFT_VALUE);

            if (valueCft == null) {
                createPrimitiveCft(cet);
            } else {
                boolean typeChanged = valueCft.getFieldType() != cet.getNeo4JStorageConfiguration().getPrimitiveType().getCftType();
                boolean maxValueChanged = !valueCft.getMaxValue().equals(cet.getNeo4JStorageConfiguration().getMaxValue());
                boolean shouldUpdate = typeChanged || maxValueChanged;
                if (shouldUpdate) {
                    flush();
                }

                if (typeChanged) {
                    valueCft.setFieldType(cet.getNeo4JStorageConfiguration().getPrimitiveType().getCftType());
                }

                if (maxValueChanged) {
                    valueCft.setMaxValue(cet.getNeo4JStorageConfiguration().getMaxValue());
                }

                if (shouldUpdate) {
                    customFieldTemplateService.update(valueCft);
                }
            }
        } else {
            if (cet.getNeo4JStorageConfiguration() != null) {
                cet.getNeo4JStorageConfiguration().setPrimitiveType(null);
                cet.getNeo4JStorageConfiguration().setMaxValue(null);
            }
        }
    }

    @Override
    public CustomEntityTemplate update(CustomEntityTemplate cet) throws BusinessException {
        CustomEntityTemplate oldValue = customFieldsCache.getCustomEntityTemplate(cet.getCode());
        
    	if (!EntityCustomizationUtils.validateOntologyCode(cet.getCode())) {
            throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
        }
    	
        ParamBean paramBean = paramBeanFactory.getInstance();

        /* Update */

        if (cet.getCustomEntityCategory() != null && !cet.getCustomEntityCategory().isTransient()) {
			CustomEntityCategory cec = customEntityCategoryService.reattach(cet.getCustomEntityCategory());
			cet.setCustomEntityCategory(cec);
		}
        
        CustomEntityTemplate cetUpdated = super.update(cet);

        customFieldsCache.addUpdateCustomEntityTemplate(cet);

        try {
            permissionService.createIfAbsent(cet.getModifyPermission(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
            permissionService.createIfAbsent(cet.getReadPermission(), paramBean.getProperty("role.readAllCE", "ReadAllCE"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        flush();

        // Synchronize custom fields storages with CET available storages
        for (CustomFieldTemplate cft : customFieldTemplateService.findByAppliesToNoCache(cet.getAppliesTo()).values()) {
        	cft.setHasReferenceJpaEntity(cet.hasReferenceJpaEntity());
            if (cft.getStoragesNullSafe() != null) {
                for (DBStorageType storage : new ArrayList<>(cft.getStoragesNullSafe())) {
                    if (!cet.getAvailableStorages().contains(storage)) {
                        log.info("Remove storage '{}' from CFT '{}' of CET '{}'", storage, cft.getCode(), cet.getCode());
                        cft.getStoragesNullSafe().remove(storage);
                        customFieldTemplateService.update(cft);
                    }
                }
            }
        }

        // Synchronize neoj4 indexes
        if (cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jService.addUUIDIndexes(cet);
        } else {
            neo4jService.removeUUIDIndexes(cet);
        }
        
        var sqlConfs = sqlConfigurationService.listActiveAndInitialized();
        
        // Handle SQL inheritance
        if(cet.storedIn(DBStorageType.SQL)) {
        	if(oldValue.getSuperTemplate() != null && cet.getSuperTemplate() == null) {
        		// Inheritance removed
        		sqlConfs.forEach(sc -> customTableCreatorService.removeInheritance(sc.getCode(), cet));
        	} else if(oldValue.getSuperTemplate() == null && cet.getSuperTemplate() != null) {
        		// Inheritance added
        		sqlConfs.forEach(sc -> customTableCreatorService.addInheritance(sc.getCode(), cet));
        	}
        }

        return cetUpdated;
    }
    
    @Transactional(TxType.REQUIRES_NEW)
    public void removeInNewTx(CustomEntityTemplate cet) throws BusinessException {
    	remove(cet);
    }

    @Override
    public void remove(CustomEntityTemplate cet) throws BusinessException {

        Map<String, CustomFieldTemplate> fields = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

        Map<String, EntityCustomAction> customActionMap = entityCustomActionService.findByAppliesTo(cet.getAppliesTo());

        for (CustomFieldTemplate cft : fields.values()) {
            customFieldTemplateService.remove(cft.getId());
        }

        if (cet.getSqlStorageConfiguration() != null && cet.getSqlStorageConfiguration().isStoreAsTable()) {
            customTableCreatorService.removeTable(SQLStorageConfiguration.getDbTablename(cet));

        } else if (cet.getSqlStorageConfiguration() != null) {
            customEntityInstanceService.removeByCet(cet.getCode());
        }

        if (cet.getNeo4JStorageConfiguration() != null && cet.getAvailableStorages() != null && cet.getAvailableStorages().contains(DBStorageType.NEO4J)) {
            neo4jService.removeCet(cet);
            neo4jService.removeUUIDIndexes(cet);
        }

        for (EntityCustomAction entityCustomAction : customActionMap.values()) {
            entityCustomActionService.remove(entityCustomAction.getId());
        }

        customFieldsCache.removeCustomEntityTemplate(cet);

        if(getEntityManager().contains(cet)) {
        	super.remove(cet);
        }

    }

    /**
     * List custom entity templates, optionally filtering by an active status. Custom entity templates will be looked up in cache or retrieved from DB.
     *
     * @param active Custom entity template's status. Or any if null
     * @return A list of custom entity templates
     */
    @Override
    public List<CustomEntityTemplate> list(Boolean active) {

        if (useCETCache && (active == null || active)) {

            List<CustomEntityTemplate> cets = new ArrayList<>(customFieldsCache.getCustomEntityTemplates());

            // Populate cache if record is not found in cache
            if (cets.isEmpty()) {
                cets = super.list(active);
                if (cets != null) {
                    cets.forEach((cet) -> customFieldsCache.addUpdateCustomEntityTemplate(cet));
                }
            }

            return cets;

        } else {
            return super.list(active);
        }
    }

    public List<CustomEntityTemplate> listNoCache() {
        return super.list((Boolean) null);
    }

    @Override
    public List<CustomEntityTemplate> list(PaginationConfiguration config) {

        if (useCETCache && (config.getFilters() == null || config.getFilters().isEmpty()
                || (config.getFilters().size() == 1 && config.getFilters().get("disabled") != null && !(boolean) config.getFilters().get("disabled")))) {
            List<CustomEntityTemplate> cets = new ArrayList<>(customFieldsCache.getCustomEntityTemplates());

            // Populate cache if record is not found in cache
            if (cets.isEmpty()) {
                cets = super.list(config);
                if (cets != null) {
                    cets.forEach((cet) -> customFieldsCache.addUpdateCustomEntityTemplate(cet));
                }
            }

            return cets;

        } else {
            return super.list(config);
        }
    }

    public List<CustomEntityTemplate> getCETsWithSubTemplates() {
        String query = new StringBuffer()
                .append("SELECT DISTINCT cet from CustomEntityTemplate cet ")
                .append("LEFT JOIN FETCH cet.subTemplates")
                .toString();

        return getEntityManager().createQuery(query, CustomEntityTemplate.class).getResultList();
    }


    /**
     * Get a list of custom entity templates for cache
     *
     * @return A list of custom entity templates
     */
    public List<CustomEntityTemplate> getCETForCache() {
        return getEntityManager().createNamedQuery("CustomEntityTemplate.getCETForCache", CustomEntityTemplate.class).getResultList();
    }

    /**
     * A generic method that returns a filtered list of ICustomFieldEntity given an entity class and code.
     *
     * @param entityClass - class of an entity. eg. org.meveo.catalog.OfferTemplate
     * @param entityCode  - code of entity
     * @return customer field entity
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public ICustomFieldEntity findByClassAndCode(Class entityClass, String entityCode) {
        ICustomFieldEntity result = null;
        QueryBuilder queryBuilder = new QueryBuilder(entityClass, "a", null);
        queryBuilder.addCriterion("code", "=", entityCode, true);
        List<ICustomFieldEntity> entities = (List<ICustomFieldEntity>) queryBuilder.getQuery(getEntityManager()).getResultList();
        if (entities != null && !entities.isEmpty()) {
            result = entities.get(0);
        }

        return result;
    }

    /**
     * Get a list of custom entity templates for Configuration
     *
     * @return A list of custom entity templates
     */
    public List<CustomEntityTemplate> getCETForConfiguration() {
        return getEntityManager().createNamedQuery("CustomEntityTemplate.getCETForConfiguration", CustomEntityTemplate.class).getResultList();
    }

    /**
     * Find the primitive type of a given CustomEntityTemplate
     *
     * @param code Code of the CustomEntityTemplate to find primitive type
     * @return the primitive type of the CustomEntityTemplate or null if it does not have one or is not a CustomEntityTemplate
     */
    public PrimitiveTypeEnum getPrimitiveType(String code) {
        try {
            return getEntityManager().createNamedQuery("CustomEntityTemplate.PrimitiveType", PrimitiveTypeEnum.class)
                    .setParameter("code", code)
                    .getSingleResult();
        } catch (NoResultException ignored) {
            return null;
        }
    }

    /**
     * Get a list of custom entity templates that use custom tables as implementation
     *
     * @return A list of custom entity templates
     */
    public List<CustomEntityTemplate> listCustomTableTemplates() {

        if (useCETCache) {
            List<CustomEntityTemplate> cets = new ArrayList<>();
            for (CustomEntityTemplate customEntityTemplate : customFieldsCache.getCustomEntityTemplates()) {
                if (customEntityTemplate.getSqlStorageConfiguration() != null && customEntityTemplate.getSqlStorageConfiguration().isStoreAsTable()) {
                    cets.add(customEntityTemplate);
                }
            }
            return cets;

        } else {
            return super.list(new PaginationConfiguration(MapUtils.putAll(new HashMap<>(), new Object[]{"sqlStorageConfiguration.storeAsTable", true})));
        }
    }

    /**
     * Find a custom entity template that uses a given custom table as implementation
     *
     * @param codeOrDbTablename Custom entity code or a corresponding database table name
     * @return A custom entity template
     */
    public CustomEntityTemplate findByCodeOrDbTablename(String codeOrDbTablename) {

    	CustomEntityTemplate cet = null;
    	if(useCETCache) {
    		cet = customFieldsCache.getCustomEntityTemplate(codeOrDbTablename);
    	}
    	
		if (cet == null) {
			cet = findByCode(codeOrDbTablename);
			if (cet != null) {
				return cet;
			}
		}
        return findByDbTablename(codeOrDbTablename);
    }

    /**
     * Find a custom entity template that uses a given custom table as implementation
     *
     * @param dbTablename Database table name
     * @return A custom entity template
     */
    public CustomEntityTemplate findByDbTablename(String dbTablename) {

        List<CustomEntityTemplate> cets = listCustomTableTemplates();

        for (CustomEntityTemplate cet : cets) {
            if (SQLStorageConfiguration.getDbTablename(cet).equalsIgnoreCase(dbTablename)) {
                return cet;
            }
        }
        return null;
    }

    /**
     * retrieve Custom Entity Templates given by categoryId then remove it so that we can remove it in the cache
     *
     * @param categoryId
     */
    public void removeCETsByCategoryId(Long categoryId) throws BusinessException {
    	
        TypedQuery<CustomEntityTemplate> query = getEntityManager().createNamedQuery("CustomEntityTemplate.getCETsByCategoryId", CustomEntityTemplate.class);
        List<CustomEntityTemplate> results = query.setParameter("id", categoryId).getResultList();
        if (CollectionUtils.isNotEmpty(results)) {
            for (CustomEntityTemplate entityTemplate : results) {
                remove(entityTemplate);
            }
        }
    }

    /**
     * update cet base on category id
     *
     * @param categoryId Cateogry id
     */
    public void resetCategoryCETsByCategoryId(Long categoryId) throws BusinessException {
        TypedQuery<CustomEntityTemplate> query = getEntityManager().createNamedQuery("CustomEntityTemplate.getCETsByCategoryId", CustomEntityTemplate.class);
        List<CustomEntityTemplate> results = query.setParameter("id", categoryId).getResultList();
        if (CollectionUtils.isNotEmpty(results)) {
            for (CustomEntityTemplate entityTemplate : results) {
                entityTemplate.setCustomEntityCategory(null);
                update(entityTemplate);
            }
        }
    }

    /**
     * Computes the cartesian product all {@linkplain CustomFieldTemplate} sample
     * values.
     *
     * @param cetCode                 {@link CustomEntityTemplate} code
     * @param paginationConfiguration page information
     * @return list of list of string of sample values.
     */
    public List<Map<String, String>> listExamples(String cetCode, PaginationConfiguration paginationConfiguration) {

        CustomEntityTemplate cet = findByCode(cetCode);
        Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

        Collection<Collection<String>> listOfValues = new HashSet<>();
        for (Entry<String, CustomFieldTemplate> es : cfts.entrySet()) {
            List<String> sampleValues = es.getValue().getSamples().stream().map(e -> es.getKey() + "|" + e).collect(Collectors.toList());
            listOfValues.add(sampleValues);
        }

        List<ImmutableList<String>> immutableElements = makeListofImmutable(listOfValues);

        List<List<String>> cartesianValues = Lists.cartesianProduct(immutableElements);

        return convertListToMap(cartesianValues);
    }

    private List<Map<String, String>> convertListToMap(List<List<String>> cartesianValues) {

        List<Map<String, String>> result = new ArrayList<>();

        for (List<String> listOfValues : cartesianValues) {
            Map<String, String> mapOfValues = new HashMap<>();
            for (String codeValue : listOfValues) {
                String[] token = codeValue.split("\\|");
                mapOfValues.put(token[0], token[1]);
            }

            result.add(mapOfValues);
        }

        return result;
    }

    /**
     * Converts to {@linkplain LinkedList} of {@linkplain ImmutableList} object.
     *
     * @param listOfValues list of values to be converted
     * @return the converted values
     */
    private static List<ImmutableList<String>> makeListofImmutable(Collection<Collection<String>> listOfValues) {

        List<ImmutableList<String>> converted = new LinkedList<>();
        listOfValues.forEach(array -> {
            converted.add(ImmutableList.copyOf(array));
        });

        return converted;
    }
    
	public boolean hasReferenceJpaEntity(String cetCode) {
        CustomEntityTemplate cet = findByCode(cetCode);
        return hasReferenceJpaEntity(cet);
	}
	
	public boolean hasReferenceJpaEntity(CustomEntityTemplate cet) {
		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

		if (cfts.size() > 0) {
			Optional<CustomFieldTemplate> opt = cfts.values().stream()
					.filter(e -> e.getFieldType().equals(CustomFieldTypeEnum.ENTITY) && customFieldTemplateService.isReferenceJpaEntity(e.getEntityClazzCetCode())).findAny();
			if (opt.isPresent()) {
				return true;
			}
		}

		return false;
	}
	
	public String requestSchema(String code) {
		
//		CustomEntityTemplate cet = findByCode(code);
//		jsonSchemaGenerator.buildSchema("ontology", jsonSchemaGenerator.processorOf(entityTemplate), allRefs)
		return null;
	}

	public CustomEntityTemplate updateWithNewCategory(CustomEntityTemplate cet, CustomEntityCategory customEntityCategory) throws BusinessException {

		customEntityCategoryService.create(customEntityCategory);
		cet.setCustomEntityCategory(customEntityCategory);
		return update(cet);
	}

    @SuppressWarnings("unchecked")
	public String getJsonSchemaContent(String cetCode) throws IOException {

        final File cetDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode() + "/src/main/java/custom/entities");
        File file = new File(cetDir.getAbsolutePath(), cetCode + ".json");
        byte[] mapData = Files.readAllBytes(file.toPath());
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> jsonMap = objectMapper.readValue(mapData, HashMap.class);
        
        // Replace references in allOf
        List<Map<String, Object>> allOf = (List<Map<String, Object>>) jsonMap.getOrDefault("allOf", List.of());
        allOf.forEach(item -> {
    		item.computeIfPresent("$ref", (key, ref) -> ((String) ref).replace("./", ""));
        });

        Map<String, Object> items = (Map<String, Object>) jsonMap.get("properties");
        if (items != null) {
            for (Map.Entry<String, Object> item : items.entrySet()) {
                Map<String, Object> values = (Map<String, Object>) item.getValue();
                if (values.containsKey("$ref")) {
                    String ref = (String) values.get("$ref");
                    if (ref != null) {
                        values.put("$ref", ref.replace("./", ""));
                    }
                } else {
                    if (values.get("type") != null && values.get("type").equals("array")) {
                        Map<String, Object> value = (Map<String, Object>) values.get("items");
                        if (value.containsKey("$ref")) {
                            String ref = (String) value.get("$ref");
                            if (ref != null) {
                                value.put("$ref", ref.replace("./", ""));
                            }
                        }
                    }
                }
            }
        }

        String json = JsonUtils.toJson(jsonMap, true);
        return json;
    }
}