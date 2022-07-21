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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.CustomEntity;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.ModulePostUninstall;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.crm.custom.PrimitiveTypeEnum;
import org.meveo.model.customEntities.CrudEventListenerScript;
import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.StorageImplProvider;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.MeveoModuleHelper;
import org.meveo.service.admin.impl.ModuleUninstall;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.crm.impl.JSONSchemaGenerator;
import org.meveo.service.crm.impl.JSONSchemaIntoJavaClassParser;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.util.EntityCustomizationUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ast.CompilationUnit;
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

    private final static String CLASSES_DIR = "/classes";

    private static boolean useCETCache = true;


    /**
     * @param currentUser the current meveo user
     * @return the directory where the classes are stored
     */
    public static File getClassesDir(MeveoUser currentUser) {
        return new File(getClassesDirectory(currentUser));
    }

    /**
     * @param currentUser Logged user
     * @return the classes directory relative to the file explorer directory for the user's provider
     */
    public static String getClassesDirectory(MeveoUser currentUser) {
        String rootDir = ParamBean.getInstance().getChrootDir(currentUser != null ? currentUser.getProviderCode() : null);
        return rootDir + CLASSES_DIR;
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

    @Inject
    private CustomEntityCategoryService customEntityCategoryService;

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCache;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private CustomTableCreatorService customTableCreatorService;

    @Inject
    private EntityCustomActionService entityCustomActionService;

    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;

    @Inject
    private ParamBeanFactory paramBeanFactory;

    @Inject
    private PermissionService permissionService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    private SqlConfigurationService sqlConfigurationService;

    @Inject
    private JSONSchemaIntoJavaClassParser jSONSchemaIntoJavaClassParser;

    @Inject
    private JSONSchemaGenerator jSONSchemaGenerator;

    @Inject
    private GitClient gitClient;

    @Inject
    private CustomEntityTemplateCompiler cetCompiler;
    
	@Inject
	private StorageImplProvider provider;
    

    @Inject
    CommitMessageBean commitMessageBean;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void create(CustomEntityTemplate cet) throws BusinessException {

        if (!EntityCustomizationUtils.validateOntologyCode(cet.getCode())) {
            throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
        }

        checkCrudEventListenerScript(cet);

        ParamBean paramBean = paramBeanFactory.getInstance();

        super.create(cet);

        customFieldsCache.addUpdateCustomEntityTemplate(cet);

        try {
            permissionService.createIfAbsent(cet.getModifyPermission(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
            permissionService.createIfAbsent(cet.getDecrpytPermission(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
            permissionService.createIfAbsent(cet.getReadPermission(), paramBean.getProperty("role.readAllCE", "ReadAllCE"));

            /* If cet is a primitive type, create custom field of corresponding type */
            if (cet.getNeo4JStorageConfiguration() != null && cet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
                createPrimitiveCft(cet);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        for (var storage : cet.getAvailableStorages()) {
        	provider.findImplementation(storage).cetCreated(cet);
        }
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
     * Find a custom entity template that uses a given custom table as implementation
     *
     * @param codeOrDbTablename Custom entity code or a corresponding database table name
     * @return A custom entity template
     */
    public CustomEntityTemplate findByCodeOrDbTablename(String codeOrDbTablename) {

        CustomEntityTemplate cet = null;
        if(useCETCache) {
            cet = customFieldsCache.getCustomEntityTemplate(codeOrDbTablename);
            if(cet != null) {
                return cet;
            }
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
     * Get a list of custom entity templates for cache
     *
     * @return A list of custom entity templates
     */
    public List<CustomEntityTemplate> getCETForCache() {
        return getEntityManager().createNamedQuery("CustomEntityTemplate.getCETForCache", CustomEntityTemplate.class).getResultList();
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
     * @return a list of custom entity templates that have sub-templates
     */
    public List<CustomEntityTemplate> getCETsWithSubTemplates() {
        String query = new StringBuffer()
                .append("SELECT DISTINCT cet from CustomEntityTemplate cet ")
                .append("LEFT JOIN FETCH cet.subTemplates")
                .toString();

        return getEntityManager().createQuery(query, CustomEntityTemplate.class).getResultList();
    }

    /**
     *
     * @param cet the custom entity template
     * @return the json schema of the custom entity template
     * @throws IOException if a file can't be written / read
     */
    @SuppressWarnings("unchecked")
    public String getJsonSchemaContent(CustomEntityTemplate cet) throws IOException {

        MeveoModule module = this.findModuleOf(cet);
        final File cetDir = GitHelper.getRepositoryDir(currentUser, module.getCode() + "/facets/json");
        File file = new File(cetDir.getAbsolutePath(), cet.getCode() + "-schema.json");
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

        return JacksonUtil.toStringPrettyPrinted(jsonMap);
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

    /**
     * @param cet the custom entity template
     * @return whether the given template has a reference to a jpa entity in one of its fields
     */
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

    @Override
    public List<CustomEntityTemplate> list(PaginationConfiguration config) {

        if (useCETCache && (config.getFetchFields() == null || config.getFetchFields().isEmpty()) && (config.getFilters() == null || config.getFilters().isEmpty()
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

    /**
     * @return the list of custom entity templates from database
     */
    public List<CustomEntityTemplate> listNoCache() {
        return super.list((Boolean) null);
    }

    /**
     * Instantiate the crud event listener for the given cet
     * @param cet The cet to load the listener
     */
    @SuppressWarnings("unchecked")
    public CrudEventListenerScript<CustomEntity> loadCrudEventListener(CustomEntityTemplate cet) {
        if(cet.getCrudEventListener() == null && cet.getCrudEventListenerScript() != null) {
            var listener = (CrudEventListenerScript<CustomEntity>) scriptInstanceService.getExecutionEngine(cet.getCrudEventListenerScript(), null);
            cet.setCrudEventListener(listener);
        }
        return cet.getCrudEventListener();
    }

    public void removeData(CustomEntityTemplate cet) {
    	if (cet == null) {
    		return;
    	}
    	
        for (var storage : cet.getAvailableStorages()) {
        	provider.findImplementation(storage).removeCet(cet);
        }
    }

    @Override
    public void remove(CustomEntityTemplate cet) throws BusinessException {

        Map<String, CustomFieldTemplate> fields = customFieldTemplateService.findByAppliesToNoCache(cet.getAppliesTo());

        Map<String, EntityCustomAction> customActionMap = entityCustomActionService.findByAppliesTo(cet.getAppliesTo());

        for (CustomFieldTemplate cft : fields.values()) {
            // Don't remove super-template cfts
            if(cft.getAppliesTo().equals(cet.getAppliesTo())) {
                customFieldTemplateService.remove(cft);
            }
        }

        for (EntityCustomAction entityCustomAction : customActionMap.values()) {
            entityCustomActionService.remove(entityCustomAction.getId());
        }

        customFieldsCache.removeCustomEntityTemplate(cet);

        // Remove permissions
        permissionService.removeIfPresent(cet.getModifyPermission());
        permissionService.removeIfPresent(cet.getDecrpytPermission());
        permissionService.removeIfPresent(cet.getReadPermission());

        super.remove(cet);
    }

    /**
     * retrieve Custom Entity Templates given by categoryId then remove it so that we can remove it in the cache
     *
     * @param categoryId if of the category
     * @throws BusinessException the the CETs can't be removed
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
     * Remove the given CET in a new transaction
     *
     * @see #remove(CustomEntityTemplate)
     * @param cet the CET to remove
     * @throws BusinessException if CET can't be removed
     */
    @Transactional(TxType.REQUIRES_NEW)
    public void removeInNewTx(CustomEntityTemplate cet) throws BusinessException {
        remove(cet);
    }

    /**
     * Remove the relation toward the category of the attached CETs
     *
     * @param categoryId Category id
     * @throws BusinessException if a relation can't be removed
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

    @Override
    public CustomEntityTemplate update(CustomEntityTemplate cet) throws BusinessException {
        CustomEntityTemplate oldValue = customFieldsCache.getCustomEntityTemplate(cet.getCode());

        if (!EntityCustomizationUtils.validateOntologyCode(cet.getCode())) {
            throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
        }

        checkCrudEventListenerScript(cet);

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

        Set<DBStorageType> storages = new HashSet<>();
        storages.addAll(cet.getAvailableStorages());
        storages.addAll(oldValue.getAvailableStorages());
        for (var storage : storages) {
        	provider.findImplementation(storage).cetUpdated(oldValue, cet);
        }

        return cetUpdated;
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

    private void checkCrudEventListenerScript(CustomEntityTemplate cet) throws IllegalArgumentException {
        if(cet.getCrudEventListener() != null) {
            var listener = scriptInstanceService.getExecutionEngine(cet.getCrudEventListenerScript(), null);
            if(!(listener instanceof CrudEventListenerScript)) {
                throw new IllegalArgumentException("The crud event listener script should implements the following interface: " + CrudEventListenerScript.class);
            }
        }
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

    private void createPrimitiveCft(CustomEntityTemplate cet) throws BusinessException {
        // Define CFT
        final CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
        CustomEntityTemplateUtils.turnIntoPrimitive(cet, customFieldTemplate);
        // Create CFT
        customFieldTemplateService.create(customFieldTemplate);
    }

    @PostConstruct
    private void init() {
        useCETCache = Boolean.parseBoolean(ParamBean.getInstance().getProperty("cache.cacheCET", "true"));
    }

    @Override
    public void removeFilesFromModule(CustomEntityTemplate cet, MeveoModule module) throws BusinessException {
        super.removeFilesFromModule(cet, module);

        final File cetJsonDir = cetCompiler.getJsonCetDir(cet, module);
        final File cetJavaDir = cetCompiler.getJavaCetDir(cet, module);
        final File classDir = CustomEntityTemplateService.getClassesDir(currentUser);
        List<File> fileList = new ArrayList<>();

        final File schemaFile = new File(cetJsonDir, cet.getCode() + "-schema.json");
        if (schemaFile.exists()) {
            schemaFile.delete();
            fileList.add(schemaFile);
        }

        final File javaFile = new File(cetJavaDir, cet.getCode() + ".java");
        if (javaFile.exists()) {
            javaFile.delete();
            fileList.add(javaFile);
        }

        final File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".class");
        if (classFile.exists()) {
            classFile.delete();
        }
        
        final File cftDir = new File(GitHelper.getRepositoryDir(null, module.getCode()), "customFieldTemplates/" + cet.getAppliesTo());
        if (cftDir.exists()) {
	        for (File cftFile : cftDir.listFiles()) {
	        	cftFile.delete();
	        	fileList.add(cftFile);
	        }
	        cftDir.delete();
	        fileList.add(cftDir);
        }

        if(!fileList.isEmpty()) {
            String message = "Deleted custom entity template " + cet.getCode();
            try {
                message+=" "+commitMessageBean.getCommitMessage();
            } catch (ContextNotActiveException e) {
                log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
            }
            gitClient.commitFiles(meveoRepository, fileList,message);
        }

    }

    /**
     * see java-doc {@link BusinessService#addFilesToModule(org.meveo.model.BusinessEntity, MeveoModule)}
     */
    @Override
    public void addFilesToModule(CustomEntityTemplate entity, MeveoModule module) throws BusinessException {
        super.addFilesToModule(entity, module);

        File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
        String pathJavaFile = "facets/java/org/meveo/model/customEntities/" + entity.getCode() + ".java";
        String pathJsonSchemaFile = "facets/json/" + entity.getCode() + "-schema" + ".json";

        File newJavaFile = new File (gitDirectory, pathJavaFile);
        File newJsonSchemaFile = new File(gitDirectory, pathJsonSchemaFile);

        try {
            MeveoFileUtils.writeAndPreserveCharset(this.jSONSchemaGenerator.generateSchema(pathJsonSchemaFile, entity), newJsonSchemaFile);
        } catch (IOException e) {
            throw new BusinessException("File cannot be write", e);
        }

        String message = "Add the cet json schema : " + entity.getCode()+".json" + " in the module : " + module.getCode();
        try {
            message+=" "+commitMessageBean.getCommitMessage();
        } catch (ContextNotActiveException e) {
            log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
        }
        gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(newJsonSchemaFile), message);

        String schemaLocation = this.cetCompiler.getTemplateSchema(entity);

        final CompilationUnit compilationUnit = this.jSONSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(schemaLocation, entity);

        try {
            MeveoFileUtils.writeAndPreserveCharset(compilationUnit.toString(), newJavaFile);
        } catch (IOException e) {
            throw new BusinessException("File cannot be write", e);
        }

        message = "Add the cet java source file : " + entity.getCode()+".java" + "in the module : " + module.getCode();
        try {
            message+=" "+commitMessageBean.getCommitMessage();
        } catch (ContextNotActiveException e) {
            log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
        }
        gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(newJavaFile), message);
    }

    @Override
    public void onAddToModule(CustomEntityTemplate entity, MeveoModule module) throws BusinessException {
        super.onAddToModule(entity, module);

        for (var cft : customFieldTemplateService.findByAppliesTo(entity.getAppliesTo()).values()) {
            meveoModuleService.addModuleItem(new MeveoModuleItem(cft), module);
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void afterModuleUninstall(@Observes @ModulePostUninstall ModuleUninstall event, MeveoModuleHelper moduleHelper) {
        if (event.removeData() && event.removeItems()) {
            List<CustomEntityTemplate> cets = moduleHelper.getEntities(event.module(), CustomEntityTemplate.class);
            List<Repository> repositories = event.module().getRepositories();
            cets.stream()
                    .map(e -> {
                        e.setRepositories(repositories);
                        return e;
                    })
                    .forEach(this::removeData);
        }
    }

    @Override
    public void moveFilesToModule(CustomEntityTemplate entity, MeveoModule oldModule, MeveoModule newModule) throws BusinessException, IOException {
        super.moveFilesToModule(entity, oldModule, newModule);

        // Move CFTs and CEAs at the same time
        for (CustomFieldTemplate cft : customFieldTemplateService.findByAppliesTo(entity.getAppliesTo()).values()) {
            customFieldTemplateService.moveFilesToModule(cft, oldModule, newModule);
        }

        for (EntityCustomAction cea : entityCustomActionService.findByAppliesTo(entity.getAppliesTo()).values()) {
            entityCustomActionService.moveFilesToModule(cea, oldModule, newModule);
        }
    }

    @Override
    protected BaseEntityDto getDto(CustomEntityTemplate entity) throws BusinessException {
        CustomEntityTemplateDto dto = (CustomEntityTemplateDto) super.getDto(entity);
        dto.setFields(null);
        dto.setActions(null);
        return dto;
    }
    
	@Override
	public CustomEntityTemplate findByCode(String code) {
		return super.findByCode(code, List.of("availableStorages"));
	}

}