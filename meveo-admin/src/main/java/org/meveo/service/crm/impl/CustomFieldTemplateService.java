package org.meveo.service.crm.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.hibernate.util.HibernateUtils;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.catalog.CalendarDaily;
import org.meveo.model.catalog.CalendarInterval;
import org.meveo.model.catalog.CalendarYearly;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldMatrixColumn;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.persistence.StorageImplProvider;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.service.base.BusinessService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.git.GitHelper;
import org.meveo.service.storage.FileSystemService;
import org.meveo.util.EntityCustomizationUtils;
import org.meveo.util.PersistenceUtils;

/**
 * @author Wassim Drira
 * @author Clément Bareth
 * @author Edward P. Legaspi czetsuya@gmail.com
 * @lastModifiedVersion 6.8.0
 */
@Stateless
public class CustomFieldTemplateService extends BusinessService<CustomFieldTemplate> {

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCache;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private FileSystemService fileSystemService;

    @Inject
    private CrossStorageService crossStorageService;

    @Inject
    private ModuleInstallationContext moduleInstallCtx;
    
	@Inject
	private StorageImplProvider provider;
    

    @Inject
    CommitMessageBean commitMessageBean;

    static boolean useCFTCache = true;

    @PostConstruct
    private void init() {
        useCFTCache = Boolean.parseBoolean(ParamBean.getInstance().getProperty("cache.cacheCFT", "true"));
    }

    @Override
    protected void beforeUpdateOrCreate(CustomFieldTemplate entity) throws BusinessException {
        super.beforeUpdateOrCreate(entity);

        if(entity.getRelationship() != null) {
            if(entity.getRelationship().getId() != null) {
                CustomRelationshipTemplate crt = getEntityManager().find(CustomRelationshipTemplate.class, entity.getRelationship().getId());
                entity.setRelationship(crt);
            }
        }
    }

    public boolean exists(String code, String appliesTo) {
        try {
            return getEntityManager().createNativeQuery("SELECT 1 FROM crm_custom_field_tmpl WHERE "
                            + "code = :code and applies_to = :appliesTo")
                    .setParameter("code", code)
                    .setParameter("appliesTo",appliesTo)
                    .getSingleResult() != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<CustomFieldTemplate> findCftUniqueFieldsByApplies(String appliesTo) {

        Query q = getEntityManager().createNamedQuery("CustomFieldTemplate.getCftUniqueFieldsByApplies") //
                .setParameter("appliesTo", appliesTo);

        try {
            return q.getResultList();

        } catch (NoResultException e) {
            return null;

        } catch (Exception e) {
            log.error("Failed to retrieve custom field templates", e);
            return null;
        }
    }

    /**
     * Find a list of custom field templates corresponding to a given entity
     *
     * @param entity Entity that custom field templates apply to
     * @return A list of custom field templates mapped by a template key
     */
    public Map<String, CustomFieldTemplate> findByAppliesTo(ICustomFieldEntity entity) {
        try {
            return findByAppliesTo(CustomFieldTemplateUtils.calculateAppliesToValue(entity));

        } catch (CustomFieldException e) {
            // Its ok, handles cases when value that is part of CFT.AppliesTo calculation is not set yet on entity
            return new HashMap<String, CustomFieldTemplate>();
        }
    }

    public Map<String, CustomFieldTemplate> getCftsWithInheritedFields(CustomEntityTemplate cet) {
        Map<String, CustomFieldTemplate> customFieldTemplates = new HashMap<>();
        for (CustomEntityTemplate e = cet; e != null; e = e.getSuperTemplate()) {
            if (HibernateUtils.isLazyLoaded(e)) {
                e = customEntityTemplateService.findById(e.getId(), List.of("superTemplate"));
            }
            findByAppliesTo(e.getAppliesTo()).forEach(customFieldTemplates::putIfAbsent);	// Preserve overriden cfts
        }
        return customFieldTemplates;
    }

    /**
     * Find a list of custom field templates corresponding to a given entity. Custom field templates are looked up in cache or retrieved from DB.
     *
     * @param appliesTo Entity (CFT appliesTo code) that custom field templates apply to
     * @return A list of custom field templates mapped by a template code
     */
    public Map<String, CustomFieldTemplate> findByAppliesTo(String appliesTo) {

        if (useCFTCache) {

            Map<String, CustomFieldTemplate> cfts = customFieldsCache.getCustomFieldTemplates(appliesTo);

            // Populate cache if record is not found in cache
            if (cfts == null) {
                cfts = findByAppliesToNoCache(appliesTo);
                if (cfts.isEmpty()) {
                    customFieldsCache.markNoCustomFieldTemplates(appliesTo);
                } else {
                    cfts.forEach((code, cft) -> customFieldsCache.addUpdateCustomFieldTemplate(cft));
                }
            }

            return cfts;

        } else {
            return findByAppliesToNoCache(appliesTo);
        }
    }

    /**
     * Find a list of custom field templates corresponding to a given entity - always do a lookup in DB
     *
     * @param appliesTo Entity (CFT appliesTo code) that custom field templates apply to
     * @return A list of custom field templates mapped by a template key
     */
    public Map<String, CustomFieldTemplate> findByAppliesToNoCache(String appliesTo) {

        try {
            List<CustomFieldTemplate> values = getEntityManager()
                    .createNamedQuery("CustomFieldTemplate.getCFTByAppliesTo", CustomFieldTemplate.class)
                    .setParameter("appliesTo", appliesTo).getResultList();

            Map<String, CustomFieldTemplate> cftMap = values.stream().collect(Collectors.toMap(cft -> cft.getCode(), cft -> cft));
            return cftMap;

        } catch (Exception e) {
            log.error("Failed to retrieve custom field templates", e);
            return new HashMap<>();
        }
    }

    /**
     * Find a specific custom field template by a code
     *
     * @param code Custom field template code
     * @param entity Entity that custom field templates apply to
     * @return Custom field template or NULL if not found
     */
    public CustomFieldTemplate findByCodeAndAppliesTo(String code, ICustomFieldEntity entity) {

        try {
            String calculatedAppliesToValue = CustomFieldTemplateUtils.calculateAppliesToValue(entity);
            CustomFieldTemplate field = null;
            if (calculatedAppliesToValue != null) {
                field = findByCodeAndAppliesTo(code, calculatedAppliesToValue);
            } else {
                log.error("Can not calculate applicable AppliesToValue for entity of {} class.", entity.getClass().getSimpleName());
            }

            if(field == null && entity instanceof CustomEntityInstance) {
                var cet = ((CustomEntityInstance) entity).getCet();
                if (cet.getSuperTemplate() != null) {
                    var parentCet = customEntityTemplateService.findById(cet.getSuperTemplate().getId());
                    if (parentCet.getAppliesTo() != null) {
                        CustomEntityInstance parentCei = new CustomEntityInstance();
                        parentCei.setCet(parentCet);
                        return findByCodeAndAppliesTo(code, parentCei);
                    }
                }
            }
            return field;

        } catch (CustomFieldException e) {
            log.error("Can not determine applicable CFT type for entity of {} class.", entity.getClass().getSimpleName());
        }

        return null;
    }

    /**
     * Find a specific custom field template by a code. Custom field template will be looked up from cache or retrieved from DB.
     *
     * @param code Custom field template code
     * @param appliesTo Entity (CFT appliesTo code) that custom field templates apply to
     * @return Custom field template or NULL if not found
     */
    public CustomFieldTemplate findByCodeAndAppliesTo(String code, String appliesTo) {

        if (useCFTCache) {

            CustomFieldTemplate cft = customFieldsCache.getCustomFieldTemplate(code, appliesTo);

            // Populate cache if record is not found in cache
            if (cft == null) {
                cft = findByCodeAndAppliesToNoCache(code, appliesTo);
                if (cft != null) {
                    customFieldsCache.addUpdateCustomFieldTemplate(cft);
                }
            }
            return cft;

        } else {
            return findByCodeAndAppliesToNoCache(code, appliesTo);
        }
    }

    /**
     * Find a specific custom field template by a code bypassing cache - always do a lookup in DB
     *
     * @param code Custom field template code
     * @param appliesTo Entity (CFT appliesTo code) that custom field templates apply to
     * @return Custom field template or NULL if not found
     */
    public CustomFieldTemplate findByCodeAndAppliesToNoCache(String code, String appliesTo) {

        try {
            return getEntityManager()
                    .createNamedQuery("CustomFieldTemplate.getCFTByCodeAndAppliesTo", CustomFieldTemplate.class)
//            		.createQuery("FROM CustomFieldTemplate where code=:code and appliesTo=:appliesTo", CustomFieldTemplate.class)
                    .setParameter("code", code)
                    .setParameter("appliesTo", appliesTo)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;

        } catch (Exception e) {
            log.error("Failed to retrieve custom field template", e);
            return null;
        }
    }

    @Override
    public void create(CustomFieldTemplate cft) throws BusinessException {
        if (!EntityCustomizationUtils.validateOntologyCode(cft.getCode())) {
            throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
        }
        checkIdentifierTypeAndUniqueness(cft);

        //  if CFT is of type STRING
        if ((cft.getFieldType() == CustomFieldTypeEnum.STRING || cft.getFieldType() == CustomFieldTypeEnum.SECRET) && (cft.getMaxValue()== null || cft.getMaxValue() == 0)) {
            cft.setMaxValue(CustomFieldTemplate.DEFAULT_MAX_LENGTH_STRING);
        }

        //  if CFT is of type DATE
        if (CustomFieldTypeEnum.DATE.equals(cft.getFieldType()) && cft.getDisplayFormat() != null) {
            checkDateFormat(cft);
        }

        customFieldsCache.addUpdateCustomFieldTemplate(cft);

        try {
            super.create(cft);
        } catch (Exception e) {
            customFieldsCache.removeCustomFieldTemplate(cft);
            throw e;
        }

        String entityCode = EntityCustomizationUtils.getEntityCode(cft.getAppliesTo());

		CustomModelObject appliesToTemplate = getAppliesToTemplate(cft, entityCode);
		
		if (appliesToTemplate != null && cft.getStorages() != null) {
	        for (var storage : cft.getStorages()) {
	        	provider.findImplementation(storage).cftCreated(appliesToTemplate, cft);
	        }
		}
		
		MeveoModule relatedModule = null;
		
        if (moduleInstallCtx.isActive()) {
            relatedModule = meveoModuleService.findByCode(moduleInstallCtx.getModuleCodeInstallation());
        }

        // Synchronize CET / CRT POJO
        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = customFieldsCache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if(relatedModule == null) {
                relatedModule = customEntityTemplateService.findModuleOf(cet);
            }
            customEntityTemplateService.addFilesToModule(cet, relatedModule);
        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = customFieldsCache.getCustomRelationshipTemplate(CustomRelationshipTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if (relatedModule == null) {
                relatedModule = customRelationshipTemplateService.findModuleOf(crt);
            }
            customRelationshipTemplateService.addFilesToModule(crt, relatedModule);
        }

        // We only do this sync in case the CFT is created outside of a module installation context
        if (relatedModule != null && !moduleInstallCtx.isActive()) {
            MeveoModuleItem mi = new MeveoModuleItem();
            mi.setMeveoModule(relatedModule);
            mi.setAppliesTo(cft.getAppliesTo());
            mi.setItemClass(CustomFieldTemplate.class.getName());
            mi.setItemCode(cft.getCode());
            meveoModuleService.addModuleItem(mi, relatedModule);
        }
	}

	private CustomModelObject getAppliesToTemplate(CustomFieldTemplate cft, String entityCode) {
		CustomModelObject appliesToTemplate = null;
		// CF applies to a CET
		if(cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
			appliesToTemplate = customEntityTemplateService.findByCode(entityCode);
			if(appliesToTemplate == null) {
				log.warn("Custom entity template {} was not found", entityCode);
			}
			
		// CF Applies to a CRT
		} else if(cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
			appliesToTemplate = customRelationshipTemplateService.findByCode(entityCode);
            if (appliesToTemplate == null) {
                log.warn("Custom relationship template {} was not found", entityCode);
            }
        }
		return appliesToTemplate;
	}
    
	/**
	 * @param cft
	 * @throws BusinessException
	 */
	public void checkDateFormat(CustomFieldTemplate cft) throws BusinessException {
        if (cft.getDisplayFormat() != null) {
            try {
                new SimpleDateFormat(cft.getDisplayFormat());
            }catch(IllegalArgumentException e){
                throw new BusinessException("Wrong syntax for date format : " + e.getMessage());
            }
        }
    }

    @Override
    public CustomFieldTemplate update(CustomFieldTemplate cft) throws BusinessException {
        if (!EntityCustomizationUtils.validateOntologyCode(cft.getCode())) {
            throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
        }

        CustomFieldTemplate cachedCft = customFieldsCache.getCustomFieldTemplate(cft.getCode(), cft.getAppliesTo());

        //  if CFT is of type DATE
        if (CustomFieldTypeEnum.DATE.equals(cft.getFieldType()) && cft.getDisplayFormat() != null) {
            checkDateFormat(cft);
        }

        checkIdentifierTypeAndUniqueness(cft);

        CustomFieldTemplate cftUpdated = super.update(cft);

		String entityCode = EntityCustomizationUtils.getEntityCode(cft.getAppliesTo());
		CustomModelObject appliesToTemplate = getAppliesToTemplate(cftUpdated, entityCode);
		
		// CF applies to a CET
		if(cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
			CustomEntityTemplate cet = customEntityTemplateService.findByCode(entityCode);
			if(cet == null) {
				log.warn("Custom entity template {} was not found", entityCode);
			} 
			
			// Move files from / to file explorer if isSaveOnExplorer attribute has changed
			if(cachedCft != null && cft.isSaveOnExplorer() != cachedCft.isSaveOnExplorer()) {
				try {
					Map<EntityRef, List<File>> summary = fileSystemService.moveBinaries(cet.getCode(), cft.getCode(), cft.isSaveOnExplorer());
					for(Map.Entry<EntityRef, List<File>> summaryEntry : summary.entrySet()) {
						Repository repository = summaryEntry.getKey().getRepository();
						String uuid = summaryEntry.getKey().getUuid();
						crossStorageService.setBinaries(repository, cet, cftUpdated, uuid, summaryEntry.getValue());
					}
					
				} catch (IOException e) {
					throw new BusinessException("Error while moving files from / to file explorer", e);
				}
			}

		// CF Applies to a CRT
		} else if(cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
			CustomRelationshipTemplate crt = customRelationshipTemplateService.findByCode(entityCode);
			if(crt == null) {
				log.warn("Custom relationship template {} was not found", entityCode);
			} 
		}
		
        Set<DBStorageType> storages = new HashSet<>();
        storages.addAll(cft.getStorages());
        storages.addAll(cachedCft.getStorages());
		if (appliesToTemplate != null && cft.getStorages() != null) {
	        for (var storage : storages) {
	        	provider.findImplementation(storage).cftUpdated(appliesToTemplate, cachedCft, cftUpdated);
	        }
		}

        customFieldsCache.addUpdateCustomFieldTemplate(cftUpdated);
        
		MeveoModule relatedModule = null;
		
        if (moduleInstallCtx.isActive()) {
            relatedModule = meveoModuleService.findByCode(moduleInstallCtx.getModuleCodeInstallation());
        }

        // Synchronize CET / CRT POJO
        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = customFieldsCache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if(relatedModule == null) {
                relatedModule = customEntityTemplateService.findModuleOf(cet);
            }
            customEntityTemplateService.addFilesToModule(cet, relatedModule);
        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = customFieldsCache.getCustomRelationshipTemplate(CustomRelationshipTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if (relatedModule == null) {
                relatedModule = customRelationshipTemplateService.findModuleOf(crt);
            }
            customRelationshipTemplateService.addFilesToModule(crt, relatedModule);
        }

        return cftUpdated;
    }

    @Override
    public void remove(CustomFieldTemplate cft) throws BusinessException {
        remove(cft, false);
    }

    public void remove(CustomFieldTemplate cft, boolean withData) throws BusinessException {
        customFieldsCache.removeCustomFieldTemplate(cft);
        super.remove(cft);

    	String entityCode = EntityCustomizationUtils.getEntityCode(cft.getAppliesTo());
    	CustomModelObject appliesToTemplate = getAppliesToTemplate(cft, entityCode);
    	
		// CF applies to a CET
		if(appliesToTemplate instanceof CustomEntityTemplate) {
			if(cft.getFieldType().equals(CustomFieldTypeEnum.BINARY)) {
				try {
					fileSystemService.removeBinaries(entityCode, cft);
				} catch (IOException e) {
					throw new BusinessException("Can't remove binaries associated to " + cft, e); 
				}
			}

		}
		
		if (withData && appliesToTemplate != null && cft.getStorages() != null) {
	        for (var storage : cft.getStorages()) {
	        	provider.findImplementation(storage).removeCft(appliesToTemplate, cft);
	        }
		}
		
		// Synchronize CET / CRT POJO
        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = customFieldsCache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if (cet != null) {
                MeveoModule cetModule = customEntityTemplateService.findModuleOf(cet);
                customEntityTemplateService.addFilesToModule(cet, cetModule);
            }
        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = customFieldsCache.getCustomRelationshipTemplate(CustomRelationshipTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if (crt != null) {
                MeveoModule cetModule = customRelationshipTemplateService.findModuleOf(crt);
                customRelationshipTemplateService.addFilesToModule(crt, cetModule);
            }
        }

    }

    @Override
    public CustomFieldTemplate enable(CustomFieldTemplate cft) throws BusinessException {
        cft = super.enable(cft);
        customFieldsCache.addUpdateCustomFieldTemplate(cft);
        return cft;
    }

    @Override
    public CustomFieldTemplate disable(CustomFieldTemplate cft) throws BusinessException {
        cft = super.disable(cft);
        customFieldsCache.removeCustomFieldTemplate(cft);
        return cft;
    }

    /**
     * Get a list of custom field templates for cache
     *
     * @return A list of custom field templates
     */
    public List<CustomFieldTemplate> getCFTForCache() {
        List<CustomFieldTemplate> cfts = getEntityManager().createNamedQuery("CustomFieldTemplate.getCFTForCache", CustomFieldTemplate.class).getResultList();
        return cfts;
    }

    /**
     * Get a list of custom field templates for index
     *
     * @return A list of custom field templates
     */
    public List<CustomFieldTemplate> getCFTForIndex() {
        List<CustomFieldTemplate> cfts = getEntityManager().createNamedQuery("CustomFieldTemplate.getCFTForIndex", CustomFieldTemplate.class).getResultList();
        return cfts;
    }

    /**
     * Check and create missing templates given a list of templates.
     *
     * @param entity Entity that custom field templates apply to
     * @param templates A list of templates to check
     * @return A complete list of templates for a given entity. Mapped by a custom field template key.
     * @throws BusinessException business exception.
     */
    public Map<String, CustomFieldTemplate> createMissingTemplates(ICustomFieldEntity entity, Collection<CustomFieldTemplate> templates) throws BusinessException {
        try {
            return createMissingTemplates(CustomFieldTemplateUtils.calculateAppliesToValue(entity), templates, false, false);

        } catch (CustomFieldException e) {
            // Its OK, handles cases when value that is part of CFT.AppliesTo calculation is not set yet on entity
            return new HashMap<String, CustomFieldTemplate>();
        }
    }

    /**
     * Check and create missing templates given a list of templates.
     *
     * @param appliesTo Entity (CFT appliesTo code) that custom field templates apply to
     * @param templates A list of templates to check
     * @return A complete list of templates for a given entity. Mapped by a custom field template key.
     * @throws BusinessException business exception.
     */
    public Map<String, CustomFieldTemplate> createMissingTemplates(String appliesTo, Collection<CustomFieldTemplate> templates) throws BusinessException {
        return createMissingTemplates(appliesTo, templates, false, false);
    }

    /**
     * Check and create missing templates given a list of templates.
     *
     * @param entity Entity that custom field templates apply to
     * @param templates A list of templates to check
     * @param removeOrphans When set to true, this will remove custom field templates that are not included in the templates collection.
     * @param updateExisting true if updating existing templates
     * @return A complete list of templates for a given entity. Mapped by a custom field template key.
     * @throws BusinessException business exception.
     */
    public Map<String, CustomFieldTemplate> createMissingTemplates(ICustomFieldEntity entity, Collection<CustomFieldTemplate> templates, boolean updateExisting,
                                                                   boolean removeOrphans) throws BusinessException {
        try {
            return createMissingTemplates(CustomFieldTemplateUtils.calculateAppliesToValue(entity), templates, updateExisting, removeOrphans);
        } catch (CustomFieldException e) {
            // Its OK, handles cases when value that is part of CFT.AppliesTo calculation is not set yet on entity
            return new HashMap<String, CustomFieldTemplate>();
        }
    }

    /**
     * Check and create missing templates given a list of templates.
     *
     * @param appliesTo Entity (CFT appliesTo code) that custom field templates apply to
     * @param templates A list of templates to check
     * @param removeOrphans When set to true, this will remove custom field templates that are not included in the templates collection.
     * @param updateExisting true when updating missing template.
     * @return A complete list of templates for a given entity. Mapped by a custom field template key.
     * @throws BusinessException business exception.
     */
    public Map<String, CustomFieldTemplate> createMissingTemplates(String appliesTo, Collection<CustomFieldTemplate> templates, boolean updateExisting, boolean removeOrphans)
            throws BusinessException {

        // Get templates corresponding to an entity type
        Map<String, CustomFieldTemplate> allTemplates = findByAppliesToNoCache(appliesTo);

        if (templates != null) {
            CustomFieldTemplate existingCustomField = null;
            for (CustomFieldTemplate cft : templates) {
                if (!allTemplates.containsKey(cft.getCode())) {
                    log.debug("Create a missing CFT {} for {} entity", cft.getCode(), appliesTo);
                    create(cft);
                    allTemplates.put(cft.getCode(), cft);
                } else if (updateExisting) {
                    existingCustomField = allTemplates.get(cft.getCode());
                    existingCustomField.setDescription(cft.getDescription());
                    existingCustomField.setStorageType(cft.getStorageType());
                    existingCustomField.setAllowEdit(cft.isAllowEdit());
                    existingCustomField.setDefaultValue(cft.getDefaultValue());
                    existingCustomField.setFieldType(cft.getFieldType());
                    existingCustomField.setEntityClazz(cft.getEntityClazz());
                    existingCustomField.setListValues(cft.getListValues());
                    existingCustomField.setGuiPosition(cft.getGuiPosition());
                    log.debug("Update existing CFT {} for {} entity", cft.getCode(), appliesTo);
                    update(existingCustomField);
                }
            }
            if (removeOrphans) {
                CustomFieldTemplate customFieldTemplate = null;
                List<CustomFieldTemplate> forRemoval = new ArrayList<>();
                for (Map.Entry<String, CustomFieldTemplate> customFieldTemplateEntry : allTemplates.entrySet()) {
                    customFieldTemplate = customFieldTemplateEntry.getValue();
                    if (!templates.contains(customFieldTemplate)) {
                        // add to separate list to avoid ConcurrentModificationException
                        forRemoval.add(customFieldTemplate);
                    }
                }
                for (CustomFieldTemplate fieldTemplate : forRemoval) {
                    remove(fieldTemplate);
                }
            }
        }
        return allTemplates;
    }

    /**
     * Copy and associate a given custom field template to a given target entity type.
     *
     * @param cft Custom field template to copy
     * @param targetAppliesTo Target CFT.appliesTo value associate custom field template with
     * @return custom field template
     * @throws BusinessException business exception.
     */
    public CustomFieldTemplate copyCustomFieldTemplate(CustomFieldTemplate cft, String targetAppliesTo) throws BusinessException {

        if (findByCodeAndAppliesTo(cft.getCode(), targetAppliesTo) != null) {
            throw new ValidationException("Custom field template " + cft.getCode() + " already exists in targe entity " + targetAppliesTo,
                    "customFieldTemplate.copyCFT.alreadyExists");
        }

        // Load calendar for lazy loading
        if (cft.getCalendar() != null) {
            cft.setCalendar(PersistenceUtils.initializeAndUnproxy(cft.getCalendar()));
            if (cft.getCalendar() instanceof CalendarDaily) {
                ((CalendarDaily) cft.getCalendar()).setHours(PersistenceUtils.initializeAndUnproxy(((CalendarDaily) cft.getCalendar()).getHours()));
                cft.getCalendar().nextCalendarDate(new Date());
            } else if (cft.getCalendar() instanceof CalendarYearly) {
                ((CalendarYearly) cft.getCalendar()).setDays(PersistenceUtils.initializeAndUnproxy(((CalendarYearly) cft.getCalendar()).getDays()));
                cft.getCalendar().nextCalendarDate(new Date());
            } else if (cft.getCalendar() instanceof CalendarInterval) {
                ((CalendarInterval) cft.getCalendar()).setIntervals(PersistenceUtils.initializeAndUnproxy(((CalendarInterval) cft.getCalendar()).getIntervals()));
                cft.getCalendar().nextCalendarDate(new Date());
            }
        }
        if (cft.getListValues() != null) {
            cft.getListValues().values().toArray(new String[] {});
        }

        if (cft.getMatrixColumns() != null) {
            cft.getMatrixColumns().toArray(new CustomFieldMatrixColumn[] {});
        }

        detach(cft);

        CustomFieldTemplate cftCopy = SerializationUtils.clone(cft);
        cftCopy.setId(null);
        cftCopy.setVersion(null);
        cftCopy.setAppliesTo(targetAppliesTo);

        if (cft.getListValues() != null) {
            cftCopy.setListValues(new HashMap<>());
            cftCopy.getListValues().putAll(cft.getListValues());
        }

        if (cft.getMatrixColumns() != null) {
            cftCopy.setMatrixColumns(new ArrayList<>());
            cftCopy.getMatrixColumns().addAll(cft.getMatrixColumns());
        }

        create(cftCopy);

        return cftCopy;
    }

    /**
     * There can only be one identifier field for a CET / CFT, and it must be a String or a Long
     *
     * @param cft Field to validate
     * @throws ValidationException if field is not valid
     */
    private void checkIdentifierTypeAndUniqueness(CustomFieldTemplate cft) throws ValidationException {
        if(cft.isIdentifier()){
            if(cft.getFieldType() != CustomFieldTypeEnum.STRING && cft.getFieldType() != CustomFieldTypeEnum.LONG && cft.getFieldType() != CustomFieldTypeEnum.EXPRESSION){
                throw new ValidationException(cft.getAppliesTo() + ": Identifier field can only be String or Long !");
            }

            final Map<String, CustomFieldTemplate> fields = findByAppliesTo(cft.getAppliesTo());
            final boolean identifierAlreadyExist = fields.values()
                    .stream()
                    .anyMatch(customFieldTemplate -> customFieldTemplate.isIdentifier() && !customFieldTemplate.getCode().equals(cft.getCode()));
            if(identifierAlreadyExist){
                throw new ValidationException(cft.getAppliesTo() + " An other field has already been defined as identifier !");
            }
        }
    }

    public boolean isReferenceJpaEntity(String clazz) {

        final CustomEntityTemplate referenceCet = customEntityTemplateService.findByCodeOrDbTablename(clazz);
        if (referenceCet == null) {
            try {
                Class.forName(clazz);
                return true;

            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Class " + clazz + " does not exists.");
            }

        }

        return false;
    }

    public String getFieldName(CustomEntityTemplate customEntityTemplate) {

        if (customEntityTemplate != null && customEntityTemplate.isStoreAsTable()) {
            Map<String, CustomFieldTemplate> cfts = findByAppliesTo(customEntityTemplate.getAppliesTo());
            if (cfts != null) {
                List<String> identifierFields = cfts.values().stream().filter(f -> f.isIdentifier()).map(CustomFieldTemplate::getDbFieldname).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(identifierFields)) {
                    return identifierFields.get(0);
                }
                List<String> requireFields = cfts.values().stream().filter(f -> f.isValueRequired()).map(CustomFieldTemplate::getDbFieldname).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(requireFields)) {
                    return requireFields.get(0);
                }
                List<String> summaryFields = cfts.values().stream().filter(f -> f.isSummary()).map(CustomFieldTemplate::getDbFieldname).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(summaryFields)) {
                    return summaryFields.get(0);
                }
            }
        }
        return null;
    }

    @Override
    public void addFilesToModule(CustomFieldTemplate entity, MeveoModule module) throws BusinessException {
        BaseEntityDto businessEntityDto = businessEntitySerializer.serialize(entity);
        String businessEntityDtoSerialize = JacksonUtil.toStringPrettyPrinted(businessEntityDto);

        File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getCode());

        String path = entity.getClass().getAnnotation(ModuleItem.class).path() + "/" + entity.getAppliesTo();

        File newDir = new File (gitDirectory, path);
        newDir.mkdirs();

        File newJsonFile = new File(newDir, entity.getCode() +".json");
        try {
            MeveoFileUtils.writeAndPreserveCharset(businessEntityDtoSerialize, newJsonFile);
        } catch (IOException e) {
            throw new BusinessException("File cannot be updated or created", e);
        }

        GitRepository gitRepository = gitRepositoryService.findByCode(module.getCode());
        String message = "Add JSON file for CFT " + entity.getAppliesTo() + "." + entity.getCode();
        try {
            message+=" "+commitMessageBean.getCommitMessage();
        } catch (ContextNotActiveException e) {
            log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
        }
        gitClient.commitFiles(gitRepository, Collections.singletonList(newDir), message);
    }

}