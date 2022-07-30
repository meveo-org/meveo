package org.meveo.service.crm.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import javax.enterprise.context.Conversation;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.persistence.CrossStorageApi;
import org.meveo.commons.utils.JpaUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.event.CFEndPeriodEvent;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.*;
import org.meveo.model.admin.User;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.Provider;
import org.meveo.model.crm.custom.CustomFieldMapKeyEnum;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.model.util.CustomFieldUtils;
import org.meveo.persistence.CrossStorageService;
import org.meveo.security.keycloak.CurrentUserProvider;
import org.meveo.service.admin.impl.UserService;
import org.meveo.service.base.BaseService;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomTableService;
import org.meveo.service.storage.RepositoryService;
import org.meveo.util.PersistenceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author Wassim Drira
 * @lastModifiedVersion 6.8.0
 */
@Stateless
public class CustomFieldInstanceService extends BaseService {

    @Inject
    private CustomFieldTemplateService cfTemplateService;

    @Inject
    private Event<CFEndPeriodEvent> cFEndPeriodEventProducer;

    @Resource
    private TimerService timerService;

    @Inject
    private ProviderService providerService;

    @Inject
    private UserService userService;

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    @Inject
    private Conversation conversation;

    @Inject
    private CurrentUserProvider currentUserProvider;

    @Inject
    private ParamBeanFactory paramBeanFactory;
    
    @Inject
    private CustomEntityTemplateService customEntityTemplateService;
    
    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private CrossStorageService crossStorageService;
    
    @Inject
    private CustomEntityInstanceService customEntityInstanceService;

    @Inject
    private CustomTableService customTableService;
    
    @Inject
    private CrossStorageApi crossStorageApi;
    
    @Inject
    private RepositoryService repositoryService;
    
    @Inject
    private Repository repository;
    
    /**
     * Find a entity of a given class and matching given code. In case classname points to CustomEntityTemplate, find CustomEntityInstances of a CustomEntityTemplate code
     *
     * @param classNameAndCode Classname to match. In case of CustomEntityTemplate, classname consist of "CustomEntityTemplate - &lt;CustomEntityTemplate code&gt;:"
     * @param code    Filter by entity code
     * @return A BusinessEntity
     */
    @SuppressWarnings("unchecked")
    public BusinessEntity findBusinessEntityCFVByCode(String classNameAndCode, String value) {
        Query query = null;

        // Extract cet code
        CustomEntityTemplate cet = customEntityTemplateService.findByCode(classNameAndCode);
        if (cet != null || classNameAndCode.startsWith(CustomEntityTemplate.class.getName())) {
            String cetCode = cet != null ? cet.getCode() : CustomFieldTemplate.retrieveCetCode(classNameAndCode);
            
        	try {
        		if(repository == null) {
        			log.warn("Repository should not be null at this point !");
        			return null;
        		}
        		
            	Optional<String> filterFieldOpt = customFieldTemplateService.findByAppliesTo(CustomEntityTemplate.getAppliesTo(cetCode))
            			.values()
            			.stream()
            			.filter(CustomFieldTemplate::isIdentifier)
            			.findFirst()
            			.map(CustomFieldTemplate::getCode);
            	
    			if (filterFieldOpt.isEmpty()) {
            		cet = customEntityTemplateService.findByCode(cetCode);
            		if (cet.getAvailableStorages().contains(DBStorageType.SQL) && !cet.getSqlStorageConfiguration().isStoreAsTable()) {
            			filterFieldOpt = Optional.of("code");
            		}
            	}
    			
    			CustomEntityInstance cei = null;
    			
    			if(filterFieldOpt.isPresent()) {
    				cei = crossStorageApi.find(repository, cetCode)
    	                	.by(filterFieldOpt.get(), value)
    	                	.getResult();
    			}
    			
    			if(cei == null) {
    				cei = crossStorageApi.find(repository, value, cetCode);
    			}
    			
    			if(filterFieldOpt.isPresent()) {
    				cei.setCode(cei.get(filterFieldOpt.get()));
    			} else {
    				cei.setCode(cei.getUuid());
    			}
    			
    			return cei;
            	
			} catch (EntityDoesNotExistsException e) {
				log.error("Can't find {}/{}", cetCode, value);
				return null;
			}
        	
        } else {
            BusinessEntity businessEntity = new BusinessEntity();
            if (classNameAndCode.equals(User.class.getName())) {
                User user = userService.findByUsername(value);
                if (user != null) {
                    businessEntity.setCode(value);
                    businessEntity.setId(user.getId());
                    return businessEntity;
                } else {
                    return null;
                }
            } else if (classNameAndCode.equals(Provider.class.getName())) {
                Provider provider = providerService.findByCode(value);
                if (provider == null) {
                  provider = providerService.findById(Long.valueOf(value));
                }
                if (provider != null) {
                    businessEntity.setCode(provider.getCode());
                    businessEntity.setId(provider.getId());
                    return businessEntity;
                } else {
                    return null;
                }
            }else {
                query = getEntityManager().createQuery("select e from " + classNameAndCode + " e where lower(e.code) = :code");
                query.setParameter("code", value.toLowerCase());
            }
        }

        List<BusinessEntity> entities = query.getResultList();
        if (entities.size() > 0) {
            return entities.get(0);
        } else {
            return null;
        }
    }

    /**
     * Find a list of entities of a given class and matching given code. In case classname points to CustomEntityTemplate, find CustomEntityInstances of a CustomEntityTemplate code
     
     * @param classNameAndCode Classname to match. In case of CustomEntityTemplate, classname consist of "CustomEntityTemplate - &lt;CustomEntityTemplate code&gt;:"
     * @param wildcode         Filter by entity code (wildcard)
     * @return A list of entities
     */
    @SuppressWarnings("unchecked") // TODO review location
    public List<BusinessEntity> findBusinessEntityForCFVByCode(String classNameAndCode, String wildcode) {
        Query query = null;

        List<BusinessEntity> entities = new ArrayList<>();
        
        if (classNameAndCode.startsWith(CustomEntityTemplate.class.getName())) {
        	String cetCode = CustomFieldTemplate.retrieveCetCode(classNameAndCode);
        	Optional<String> filterFieldOpt = customFieldTemplateService.findByAppliesTo(CustomEntityTemplate.getAppliesTo(cetCode))
        			.values()
        			.stream()
        			.filter(CustomFieldTemplate::isIdentifier)
        			.findFirst()
        			.map(CustomFieldTemplate::getCode);
        	CustomEntityTemplate cet;
			if (filterFieldOpt.isEmpty()) {
        		cet = customEntityTemplateService.findByCode(cetCode);
        		if (cet.getAvailableStorages().contains(DBStorageType.SQL) && !cet.getSqlStorageConfiguration().isStoreAsTable()) {
        			filterFieldOpt = Optional.of("code");
        		}
        	}
			String filterField = filterFieldOpt.orElse("uuid");
        	List<CustomEntityInstance> ceis = crossStorageApi.find(repository, cetCode)
            	.like(filterField, wildcode)
            	.limit(20)
            	.getResults();
        	entities.addAll(ceis);
        	
        } else {
        	if (classNameAndCode.equals(User.class.getName())) {
                List<User> users = userService.list();
                for (User user : users) {
                    BusinessEntity businessEntity = new BusinessEntity();
                    businessEntity.setCode(user.getUserName());
                    businessEntity.setId(user.getId());
                    entities.add(businessEntity);
                }
             } else {
                 query = getEntityManager().createQuery("select e from " + classNameAndCode + " e where lower(e.code) like :code");
                 query.setParameter("code", "%" + wildcode.toLowerCase() + "%");
                 entities = query.getResultList();
             }
        }
        
        // Set code = uuid for entities with no codes
        entities.stream()
	    	.filter(e -> e instanceof CustomEntityInstance)
	    	.filter(e -> e.getCode() == null)
	    	.forEach(e -> e.setCode((String) ((CustomEntityInstance) e).getValuesNullSafe().get("code")));
        entities.stream()
        	.filter(e -> e instanceof CustomEntityInstance)
        	.filter(e -> e.getCode() == null)
        	.forEach(e -> e.setCode(((CustomEntityInstance) e).getUuid()));
        entities.stream()
	    	.filter(e -> e instanceof CustomEntityInstance)
	    	.filter(e -> e.getDescription() == null)
	    	.forEach(e -> e.setDescription((String) ((CustomEntityInstance) e).getValuesNullSafe().get("description")));

        return entities;
    }

    /**
     * Return a value from either a custom field value or a settings/configuration parameter if CF value was not set yet by optionally setting custom field value.
     *
     * @param cfCode                Custom field and/or settings/configuration parameter code
     * @param defaultParamBeanValue A default value to set as custom field value in case settings/configuration parameter was not set
     * @param entity                Entity holding custom field value
     * @param saveInCFIfNotExist    Set CF value if it does not exist yet
     * 
     * @return A value, or a default value if none was found in neither custom field nor settings/configuration parameter
     * @throws BusinessException business exception.
     */
    public Object getOrCreateCFValueFromParamValue(String cfCode, String defaultParamBeanValue, ICustomFieldEntity entity, boolean saveInCFIfNotExist) throws BusinessException {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = getCFValue(entity, cfCode, true);
        if (value != null) {
            return value;
        }

        // If value is not found, create a new Custom field with a value taken from configuration parameters
        value = paramBeanFactory.getInstance().getProperty(cfCode, defaultParamBeanValue);
        if (value == null) {
            return null;
        }
        try {
            // If no template found - create it first

            CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
            if (cft == null) {
                cft = new CustomFieldTemplate();
                cft.setCode(cfCode);
                cft.setAppliesTo(CustomFieldTemplateUtils.calculateAppliesToValue(entity));
                cft.setActive(true);
                cft.setDescription(cfCode);
                cft.setFieldType(CustomFieldTypeEnum.STRING);
                cft.setDefaultValue(value.toString());
                cft.setValueRequired(false);
                cfTemplateService.create(cft);
            }

            if (saveInCFIfNotExist) {
                entity.getCfValuesNullSafe().setValue(cfCode, value.toString());
            }
        } catch (CustomFieldException e) {
            log.error("Can not determine applicable CFT type for entity of {} class. Value from propeties file will NOT be saved as customfield",
                    entity.getClass().getSimpleName());
        }
        return value;
    }

    /**
     * Get a custom field value for a given entity. If custom field is versionable, a current date will be used to access the value. Will instantiate a default value if value was
     * not found.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return Custom field value
     */
    public Object getCFValue(ICustomFieldEntity entity, String cfCode) {
        return getCFValue(entity, cfCode, true);
    }

    /**
     * Get a custom field value for a given entity. If custom field is versionable, a current date will be used to access the value.
     *
     * @param entity                  Entity
     * @param cfCode                  Custom field code
     * @param instantiateDefaultValue Should a default value be instantiated if value was not found
     * @return Custom field value
     */
    public Object getCFValue(ICustomFieldEntity entity, String cfCode, boolean instantiateDefaultValue) {

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            // log.trace("No CFT found {}/{}", entity, code);
            return null;
        }

        if (cft.isVersionable()) {
            log.warn("Trying to access a versionable custom field {}/{} value with no provided date. Current date will be used", entity.getClass().getSimpleName(), cfCode);
            return getCFValue(entity, cfCode, new Date(), instantiateDefaultValue);
        }

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = null;
        if (entity.getCfValues() != null) {
            value = entity.getCfValues().getValue(cfCode);
        }

        // Create such CF with default value if one is specified on CFT and other conditions match
        if (value == null && instantiateDefaultValue) {
            value = instantiateCFWithDefaultValue(entity, cft);
        }

        return value;
    }

    /**
     * Get a custom field value for a given entity and a date. Will instantiate a default value if value not found.
     *
     * @param entity Entity
     * @param code   Custom field code
     * @param date   Date
     * @return Custom field value
     */
    public Object getCFValue(ICustomFieldEntity entity, String code, Date date) {
        return getCFValue(entity, code, date, true);
    }

    /**
     * Get a custom field value for a given entity and a date.
     *
     * @param entity                  Entity
     * @param cfCode                  Custom field code
     * @param date                    Date
     * @param instantiateDefaultValue Should a default value be instantiated if value was not found
     * @return Custom field value
     */
    public Object getCFValue(ICustomFieldEntity entity, String cfCode, Date date, boolean instantiateDefaultValue) {

        // If field is not versionable - get the value without the date
        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            // log.trace("No CFT found {}/{}", entity, code);
            return null;
        }
        if (!cft.isVersionable()) {
            return getCFValue(entity, cfCode, instantiateDefaultValue);
        }

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = null;
        if (entity.getCfValues() != null) {
            value = entity.getCfValues().getValue(cfCode, date);
        }

        // Create such CF with default value if one is specified on CFT and other conditions match
        if (value == null && instantiateDefaultValue) {
            value = instantiateCFWithDefaultValue(entity, cft, date);
        }

        return value;
    }

    public String getCFValuesAsJson(ICustomFieldEntity entity) {
        return getCFValuesAsJson(entity, false);
    }

    /**
     * Get custom field values of an entity as JSON string
     *
     * @param entity        Entity
     * @param includeParent include parentCFEntities or not
     * @return JSON format string
     */
    public String getCFValuesAsJson(ICustomFieldEntity entity, boolean includeParent) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        String result = "";
        String sep = "";
        Map<String, CustomFieldTemplate> cfts = null;

        if (entity.getCfValues() != null) {
            cfts = cfTemplateService.findByAppliesTo(entity);
            result = entity.getCfValues().asJson(cfts);
            sep = ",";
        }

        if (includeParent) {
            ICustomFieldEntity[] parentCFEntities = getHierarchyParentCFEntities(entity);
            if (parentCFEntities != null && parentCFEntities.length > 0) {
                for (ICustomFieldEntity parentCF : parentCFEntities) {
                    if (parentCF.getCfValues() != null) {
                        cfts = cfTemplateService.findByAppliesTo(parentCF);
                        result += sep + parentCF.getCfValues().asJson(cfts);
                        sep = ",";
                    }
                }
            }
        }

        return result;
    }

    public Element getCFValuesAsDomElement(ICustomFieldEntity entity, Document doc) {
        return getCFValuesAsDomElement(entity, doc, false);
    }

    public Element getCFValuesAsDomElement(ICustomFieldEntity entity, Document doc, boolean includeParent) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Element customFieldsTag = doc.createElement("customFields");

        if (entity.getCfValues() == null) {
            return customFieldsTag;
        }

        Map<String, CustomFieldTemplate> cfts = cfTemplateService.findByAppliesTo(entity);

        entity.getCfValues().asDomElement(doc, customFieldsTag, cfts);

        if (includeParent) {
            ICustomFieldEntity[] parentCFEntities = getHierarchyParentCFEntities(entity);
            if (parentCFEntities != null && parentCFEntities.length > 0) {
                for (ICustomFieldEntity parentCF : parentCFEntities) {
                    if (parentCF.getCfValues() != null) {
                        cfts = cfTemplateService.findByAppliesTo(parentCF);
                        parentCF.getCfValues().asDomElement(doc, customFieldsTag, cfts);
                    }
                }
            }
        }

        return customFieldsTag;
    }

    /**
     * Set a Custom field value on an entity.
     *
     * @param entity Entity
     * @param cfCode Custom field value code
     * @param value  Value to set
     * @return custom field value
     * @throws BusinessException business exception.
     */
    @SuppressWarnings("unchecked")
	public CustomFieldValue setCFValue(ICustomFieldEntity entity, String cfCode, Object value) throws BusinessException {

        log.trace("Setting CF value. Code: {}, entity {} value {}", cfCode, entity, value);
        
        String repository = "default";
        CustomEntityTemplate cet = null;
        if(entity instanceof CustomEntityInstance) {
        	cet = ((CustomEntityInstance) entity).getCet();
        	repository = ((CustomEntityInstance) entity).getRepository() != null ? ((CustomEntityInstance) entity).getRepository().getCode() : "default";
        }

        // Can not set the value if field is versionable without a date
        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            throw new BusinessException("Custom field template with code " + cfCode + " not found found for entity " + entity);
        }
        
        // Handle serialized map values
        if((cft.getStorageType() == CustomFieldStorageTypeEnum.MAP || cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) && value instanceof String) {
        	value = JacksonUtil.fromString((String) value, Map.class);
        }

        if (cft.isVersionable()) {
            throw new RuntimeException(
                    "Can not determine a period for Custom Field " + entity.getClass().getSimpleName() + "/" + cfCode + " value if no date or date range is provided");
        }

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        CustomFieldValue cfValue = null;
        if (entity.getCfValues() != null) {
            cfValue = entity.getCfValues().getCfValue(cfCode);
        }
        log.trace("Setting CF value1. Code: {}, cfValue {}", cfCode, cfValue);
        // No existing CF value. Create CF value with new value. Assign(persist) NULL value only if cft.defaultValue is present
        if (cfValue == null) {
        	
        	if(cft.getFieldType() == CustomFieldTypeEnum.ENTITY) {
        		/* Don't wrap primitive entity references */
        		if(cft.getStoragesNullSafe().contains(DBStorageType.NEO4J)) {
        			String cetCode = cft.getEntityClazzCetCode();
        			CustomEntityTemplate refCet = customEntityTemplateService.findByCode(cetCode);
        			if(refCet == null) {
        				throw new org.meveo.exceptions.EntityDoesNotExistsException(CustomEntityTemplate.class, cetCode);
        			}
        			
        			if(refCet.getNeo4JStorageConfiguration() != null && refCet.getNeo4JStorageConfiguration().isPrimitiveEntity()) {
        				return entity.getCfValuesNullSafe().setValue(cfCode, value);
        			}
        		}
        		
				if (customFieldTemplateService.isReferenceJpaEntity(cft.getEntityClazzCetCode())) {
					
					if(cft.getStorageType() == CustomFieldStorageTypeEnum.LIST) {
						try {
							var collectionValue = JacksonUtil.convert(value, new TypeReference<List<EntityReferenceWrapper>>() {});
							cfValue = entity.getCfValuesNullSafe().setValue(cfCode, collectionValue, EntityReferenceWrapper.class);
						} catch (Exception e) {
							//FIXME: Handle these cases
							log.warn("Unhandled data : {}", value.toString());
						}
						
					} else {
		        		EntityReferenceWrapper entityReferenceWrapper = new EntityReferenceWrapper();
		    			entityReferenceWrapper.setClassnameCode(cft.getEntityClazzCetCode());
		    			
						if(value instanceof EntityReferenceWrapper) {
							entityReferenceWrapper = (EntityReferenceWrapper) value;
							
						} else if (value instanceof Number) {
							entityReferenceWrapper.setId(((Number) value).longValue());
						} else if (value instanceof String) {
							entityReferenceWrapper.setUuid((String) value);
							entityReferenceWrapper.setCode((String) value);
							
						} else if (value instanceof BusinessEntity) { 
							entityReferenceWrapper.setCode(((BusinessEntity) value).getCode());
							entityReferenceWrapper.setId(((BusinessEntity) value).getId());
							
						} else if (StringUtils.isNumeric(String.valueOf(value))) {
							entityReferenceWrapper.setId(Long.parseLong(String.valueOf(value)));
							
						} else if (value instanceof BaseEntity) {
							JpaUtils.extractNaturalId(value).ifPresent(entityReferenceWrapper::setCode);
							entityReferenceWrapper.setId(((BaseEntity) value).getId());
						}

						cfValue = entity.getCfValuesNullSafe().setValue(cfCode, entityReferenceWrapper);
					}
     
				} else {
					
	        		EntityReferenceWrapper entityReferenceWrapper = new EntityReferenceWrapper();
	    			entityReferenceWrapper.setClassnameCode(cft.getEntityClazzCetCode());
	    			entityReferenceWrapper.setRepository(repository);

					if (value instanceof Map) {
						Map<String, Object> valueAsMap = (Map<String, Object>) value;
						entityReferenceWrapper.setCode((String) valueAsMap.get("code"));
						entityReferenceWrapper.setUuid((String) valueAsMap.get("uuid"));
						if (entityReferenceWrapper.getUuid() == null) {
							entityReferenceWrapper.setUuid((String) valueAsMap.get("meveo_uuid"));
						}
						
					} else if (value instanceof String) {
						entityReferenceWrapper.setUuid((String) value);
						fetchCode(cft, (String) value, entityReferenceWrapper);
					} else if (value instanceof EntityReferenceWrapper) {
					    entityReferenceWrapper = (EntityReferenceWrapper) value;
                    }

					if (entityReferenceWrapper.getUuid() != null) {
						cfValue = entity.getCfValuesNullSafe().setValue(cfCode, entityReferenceWrapper);

					} else if (value instanceof Collection) {
						List<EntityReferenceWrapper> entityReferences = new ArrayList<>();
						List<Map<String, Object>> entityValues = new ArrayList<>();
						
 						for (Object item : (Collection<?>) value) {
							EntityReferenceWrapper itemWrapper = new EntityReferenceWrapper();
							itemWrapper.setClassnameCode(cft.getEntityClazzCetCode());

							if (item instanceof Map) {
								Map<String, Object> valueAsMap = (Map<String, Object>) item;
								itemWrapper.setCode((String) valueAsMap.get("code"));
								itemWrapper.setUuid((String) valueAsMap.get("uuid"));
								if (itemWrapper.getUuid() == null) {
									itemWrapper.setUuid((String) valueAsMap.get("meveo_uuid"));
								}
								
								if(itemWrapper.getUuid() == null) {
									entityValues.add(valueAsMap);
								}

							} else if (item instanceof String) {
								itemWrapper.setUuid((String) item);
								itemWrapper.setCode((String) item);
								
								// Try to fetch code
								fetchCode(cft, (String) item, itemWrapper);

							} else if (item instanceof EntityReferenceWrapper) {
                                itemWrapper = (EntityReferenceWrapper) item;
                            }

							if(itemWrapper.getUuid() != null || itemWrapper.getCode() != null) {
								entityReferences.add(itemWrapper);
							}
						}

						// If entity references list is empty, the entities referenced are probably being created
						if(!entityReferences.isEmpty()) {
							cfValue = entity.getCfValuesNullSafe().setValue(cfCode, entityReferences);
						} else if (!entityValues.isEmpty()) {
							cfValue = entity.getCfValuesNullSafe().setValue(cfCode, entityValues);
						}
					} else {
						entity.getCfValuesNullSafe().setValue(cfCode, value);
					}
				}

				if (value instanceof CustomEntityInstance) {
                    cfValue = entity.getCfValuesNullSafe().setValue(cfCode, value);
                }
        		
        	} else {
        		cfValue = entity.getCfValuesNullSafe().setValue(cfCode, value);
        	}
        	
            log.trace("Setting CF value 2. Code: {}, cfValue {}", cfCode, cfValue);
            // Existing CFI found. Update with new value or NULL value only if cft.defaultValue is present
        } else if (value != null || cft.getDefaultValue() != null) {
            try {
				cfValue.setValue(value);
			} catch (IllegalArgumentException e) {
				log.error("Error setting value for field template with code " + cfCode + " for entity " + entity + " : " + e.getMessage());
				throw e;
			}

            // Existing CF value found, but new value is null, so remove CF value all together
        } else {
            entity.getCfValues().removeValue(cfCode);
            return null;
        }
        return cfValue;
    }

    public CustomFieldValue setCFValue(ICustomFieldEntity entity, String cfCode, Object value, Date valueDate) throws BusinessException {

        log.trace("Setting CF value. Code: {}, entity {} value {} valueDate {}", cfCode, entity, value, valueDate);

        // If field is not versionable - set the value without the date
        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            throw new BusinessException("Custom field template with code " + cfCode + " not found found for entity " + entity);
        }

        if (!cft.isVersionable()) {
            return setCFValue(entity, cfCode, value);

            // Calendar is needed to be able to set a value with a single date
        } else if (cft.getCalendar() == null) {
            log.error("Can not determine a period for Custom Field {}/{} value if no calendar is provided", entity.getClass().getSimpleName(), cfCode);
            throw new RuntimeException("Can not determine a period for Custom Field " + entity.getClass().getSimpleName() + "/" + cfCode + " value if no calendar is provided");
        }

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        // Should not match more then one record as periods are calendar based
        CustomFieldValue cfValue = null;
        if (entity.getCfValues() != null) {
            cfValue = entity.getCfValues().getCfValue(cfCode, valueDate);
        }
        // No existing CF value. Create CF value with new value. Persist NULL value only if cft.defaultValue is present
        if (cfValue == null) {
            if (value == null && cft.getDefaultValue() == null) {
                return null;
            }
            entity.getCfValuesNullSafe().setValue(cfCode, cft.getDatePeriod(valueDate), null, value);

            // Existing CFI found. Update with new value or NULL value only if cft.defaultValue is present
        } else if (value != null || (value == null && cft.getDefaultValue() != null)) {
            cfValue.setValue(value);

            // Existing CFI found, but new value is null, so remove CFI
        } else {
            entity.getCfValues().removeValue(cfCode, valueDate);
            return null;
        }

        return cfValue;
    }

    public CustomFieldValue setCFValue(ICustomFieldEntity entity, String cfCode, Object value, Date valueDateFrom, Date valueDateTo, Integer valuePriority)
            throws BusinessException {

        log.trace("Setting CF value. Code: {}, entity {} value {} valueDateFrom {} valueDateTo {}", cfCode, entity, value, valueDateFrom, valueDateTo);

        // If field is not versionable - set the value without the date
        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            throw new BusinessException("Custom field template with code " + cfCode + " not found found for entity " + entity);
        }

        if (!cft.isVersionable()) {
            return setCFValue(entity, cfCode, value);

            // If calendar is provided - use calendar by the valueDateFrom date
        } else if (cft.getCalendar() != null) {
            log.warn(
                    "Calendar is provided in Custom Field template {}/{} while trying to assign value period start and end dates with two values. Only start date will be considered",
                    entity.getClass().getSimpleName(), cfCode);
            return setCFValue(entity, cfCode, value, valueDateFrom);
        }

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        // Should not match more then one record, as match is strict
        CustomFieldValue cfValue = null;
        if (entity.getCfValues() != null) {
            cfValue = entity.getCfValues().getCfValue(cfCode, valueDateFrom, valueDateTo);
        }
        // No existing CF value. Create CF value with new value. Persist NULL value only if cft.defaultValue is present
        if (cfValue == null) {
            if (value == null && cft.getDefaultValue() == null) {
                return null;
            }
            entity.getCfValuesNullSafe().setValue(cfCode,
            		new DatePeriod(valueDateFrom.toInstant(), valueDateTo.toInstant()),
            		valuePriority,
            		value);

            // Existing CF value found. Update with new value or NULL value only if cft.defaultValue is present
        } else if (value != null || (value == null && cft.getDefaultValue() != null)) {
            cfValue.setValue(value);

            // Existing CF value found, but new value is null, so remove CF value
        } else {
            entity.getCfValues().removeValue(cfCode, valueDateFrom, valueDateTo);
            return null;
        }

        return cfValue;
    }

    /**
     * Remove Custom field instance.
     *
     * @param entity custom field entity
     * @param cfCode Custom field code to remove
     * @throws BusinessException business exception.
     */
    public void removeCFValue(ICustomFieldEntity entity, String cfCode) throws BusinessException {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() != null) {
            entity.getCfValues().removeValue(cfCode);
        }
    }

    /**
     * Remove all custom field values for a given entity.
     *
     * @param entity custom field entity
     * @throws BusinessException business exception.
     */
    public void removeCFValues(ICustomFieldEntity entity) throws BusinessException {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        entity.clearCfValues();
    }

    /**
     * Get a custom field value for a given entity's parent's. (DOES NOT include a given entity). If custom field is versionable, a current date will be used to access the value.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return Custom field value
     */
    public Object getInheritedOnlyCFValue(ICustomFieldEntity entity, String cfCode) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        ICustomFieldEntity[] parentCFEntities = entity.getParentCFEntities();
        if (parentCFEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCFEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object value = getInheritedCFValue(parentCfEntity, cfCode);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Get a a list of custom field CFvalues for a given entity and its parent's CF entity hierarchy up.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return A list of Custom field CFvalues. From this and all the entities CF entity hierarchy up.
     */
    public List<CustomFieldValue> getInheritedAllCFValues(ICustomFieldEntity entity, String cfCode) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        List<CustomFieldValue> allValues = new ArrayList<>();

        if (entity.getCfValues() != null) {
            List<CustomFieldValue> entityValues = entity.getCfValues().getValuesByCode().get(cfCode);
            if (entityValues != null) {
                allValues.addAll(entityValues);
            }
        }

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                allValues.addAll(getInheritedAllCFValues(parentCfEntity, cfCode));
            }
        }

        return allValues;
    }

    /**
     * Get a a list of custom field CFvalues for a given entity's parent's hierarchy up. (DOES NOT include a given entity)
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return A list of Custom field CFvalues. From all the entities CF entity hierarchy up.
     */
    public List<CustomFieldValue> getInheritedOnlyAllCFValues(ICustomFieldEntity entity, String cfCode) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        List<CustomFieldValue> allValues = new ArrayList<>();

        ICustomFieldEntity[] parentCFEntities = entity.getParentCFEntities();
        if (parentCFEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCFEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                allValues.addAll(getInheritedAllCFValues(parentCfEntity, cfCode));
            }
        }
        return allValues;
    }

    /**
     * Check if give entity's parent has any custom field value defined (in any period for versionable fields)
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return True if any of entity's CF parents have value for a given custom field (in any period for versionable fields)
     */
    public boolean hasInheritedOnlyCFValue(ICustomFieldEntity entity, String cfCode) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        ICustomFieldEntity[] parentCFEntities = entity.getParentCFEntities();
        if (parentCFEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCFEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                boolean hasValue = hasInheritedCFValue(parentCfEntity, cfCode);
                if (hasValue) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Check if given entity or any of its parent has any custom field value defined (in any period for versionable fields)
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return True if entity or any of entity's CF parents have value for a given custom field (in any period for versionable fields)
     */
    public boolean hasInheritedCFValue(ICustomFieldEntity entity, String cfCode) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        boolean hasValue = hasCFValue(entity, cfCode);
        if (hasValue) {
            return true;
        }

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                hasValue = hasInheritedCFValue(parentCfEntity, cfCode);
                if (hasValue) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if given entity has custom field value defined (in any period for versionable fields)
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return True if entity or any of entity's CF parents have value for a given custom field (in any period for versionable fields)
     */
    public boolean hasCFValue(ICustomFieldEntity entity, String cfCode) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return false;
        }
        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            // log.trace("No CFT found {}/{}", entity, code);
            return false;
        }

        return entity.getCfValues().hasCfValue(cfCode);

    }

    /**
     * get hierarchy parents of cf entity
     *
     * @param entity
     * @return
     */
    private ICustomFieldEntity[] getHierarchyParentCFEntities(ICustomFieldEntity entity) {

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities == null) {
            return null;
        }
        Set<ICustomFieldEntity> result = new HashSet<ICustomFieldEntity>();
        for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
            if (parentCfEntity == null) {
                continue;
            }
            // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
            if (parentCfEntity instanceof Provider) {
                parentCfEntity = providerService.findById(appProvider.getId());
            } else {
                parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
            }
            result.add(parentCfEntity);
            ICustomFieldEntity[] recurseCfes = getHierarchyParentCFEntities(parentCfEntity);
            if (recurseCfes != null && recurseCfes.length > 0) {
                result.addAll(Arrays.asList(recurseCfes));
            }
        }
        return result.toArray(new ICustomFieldEntity[0]);
    }

    /**
     * Get a cumulative and unique custom field value for a given entity's all parent chain. (DOES NOT include a given entity). Applies to Map (matrix) values only. The closest
     * parent entity's CF value will be preserved. If custom field is versionable, a current date will be used to access the value.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return Custom field value
     */
    @SuppressWarnings("unchecked")
    public Object getInheritedOnlyCFValueCumulative(ICustomFieldEntity entity, String cfCode) {

        if (entity == null) {
            return null;
        }

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        List<Object> cfValues = new ArrayList<>();

        ICustomFieldEntity[] parentCfEntities = getHierarchyParentCFEntities(entity);
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                if (parentCfEntity.getCfValues() != null) {
                    Object value = parentCfEntity.getCfValues().getValue(cfCode);
                    if (value != null) {
                        cfValues.add(value);
                    }
                }
            }
        }

        if (cfValues.isEmpty()) {
            return null;

        } else if (!(cfValues.get(0) instanceof Map) || cfValues.size() == 0) {
            return cfValues.get(0);

        } else {
            Map<String, Object> valueMap = new LinkedHashMap<>();
            valueMap.putAll((Map<String, Object>) cfValues.get(0));
            for (int i = 1; i < cfValues.size(); i++) {
                Map<String, Object> iterMap = (Map<String, Object>) cfValues.get(i);
                for (Entry<String, Object> mapItem : iterMap.entrySet()) {
                    if (!valueMap.containsKey(mapItem.getKey())) {
                        valueMap.put(mapItem.getKey(), mapItem.getValue());
                    }
                }
            }
            return valueMap;
        }
    }

    /**
     * Get a custom field value for a given entity's parent's and a date. (DOES NOT include a given entity)
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @param date   Date
     * @return Custom field value
     */
    public Object getInheritedOnlyCFValue(ICustomFieldEntity entity, String cfCode, Date date) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object value = getInheritedCFValue(parentCfEntity, cfCode, date);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Get a custom field value for a given entity or its parent's. If custom field is versionable, a current date will be used to access the value.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return Custom field value
     */
    public Object getInheritedCFValue(ICustomFieldEntity entity, String cfCode) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        // Get value without instantiating a default value if value not found
        if (entity.getCfValues() != null) {
            Object value = entity.getCfValues().getValue(cfCode);
            if (value != null) {
                return value;
            }
        }

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValue(parentCfEntity, cfCode);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }

        // Instantiate default value if applicable
        return instantiateCFWithDefaultValue(entity, cfCode);

    }

    /**
     * Get a custom field value for a given entity or its parent's and a date
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @param date   Date
     * @return Custom field value
     */
    public Object getInheritedCFValue(ICustomFieldEntity entity, String cfCode, Date date) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        // Get value without instantiating a default value if value not found
        if (entity.getCfValues() != null) {
            Object value = entity.getCfValues().getValue(cfCode, date);
            if (value != null) {
                return value;
            }
        }

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValue(parentCfEntity, cfCode, date);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }

        // Instantiate default value if applicable
        return instantiateCFWithDefaultValue(entity, cfCode, date);
    }

    /**
     * Match for a given entity's or its parent's custom field (non-versionable values) as close as possible map's key to the key provided and return a map value. Match is
     * performed by matching a full string and then reducing one by one symbol until a match is found.
     * 
     *
     * @param entity     Entity to match
     * @param cfCode     Custom field code
     * @param keyToMatch Key to match
     * @return Map value that closely matches map key
     */
    public Object getInheritedCFValueByClosestMatch(ICustomFieldEntity entity, String cfCode, String keyToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = getCFValueByClosestMatch(entity, cfCode, keyToMatch);
        if (value != null) {
            return value;
        }
        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValueByClosestMatch(parentCfEntity, cfCode, keyToMatch);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }
        return null;
    }

    /**
     * Match for a given date (versionable values) for a given entity's or its parent's custom field as close as possible map's key to the key provided and return a map value.
     * Match is performed by matching a full string and then reducing one by one symbol until a match is found.
     * 
     *
     * @param entity     Entity to match
     * @param code       Custom field code
     * @param date       Date
     * @param keyToMatch Key to match
     * @return Map value that closely matches map key
     */
    public Object getInheritedCFValueByClosestMatch(ICustomFieldEntity entity, String code, Date date, String keyToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = getCFValueByClosestMatch(entity, code, date, keyToMatch);
        if (value != null) {
            return value;
        }
        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValueByClosestMatch(parentCfEntity, code, date, keyToMatch);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }
        return null;
    }

    /**
     * Match for a given entity's or its parent's custom field (non-versionable values) map's key (map or matrix) and return a map value.
     * 
     * For matrix, map key is assumed to be the following format. Note that MATRIX_STRING and MATRIX_RON keys can be mixed
     * 
     * &lt;matrix first key&gt;|&lt;matrix second key&gt;|&lt;matrix xx key&gt;|&lt;range of numbers for the third key&gt;
     *
     * @param entity Entity to match
     * @param cfCode Custom field code
     * @param keys   Keys to match. For matrix, the order must correspond to the order of the keys during data entry
     * @return Map value that matches the map key (map key or matrix formated map key)
     */
    public Object getInheritedCFValueByKey(ICustomFieldEntity entity, String cfCode, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = getCFValueByKey(entity, cfCode, keys);
        if (value != null) {
            return value;
        }
        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValueByKey(parentCfEntity, cfCode, keys);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }
        return null;
    }

    /**
     * Match for a given date (versionable values) for a given entity's or its parent's custom field (versionable values) map's key (map or matrix) and return a map value.
     * 
     * For matrix, map key is assumed to be the following format. Note that MATRIX_STRING and MATRIX_RON keys can be mixed
     * 
     * &lt;matrix first key&gt;|&lt;matrix second key&gt;|&lt;matrix xx key&gt;|&lt;range of numbers for the third key&gt;
     *
     * @param entity Entity to match
     * @param cfCode Custom field code
     * @param date   Date to match
     * @param keys   Keys to match. For matrix, the order must correspond to the order of the keys during data entry
     * @return Map value that matches the map key (map key or matrix formated map key)
     */
    public Object getInheritedCFValueByKey(ICustomFieldEntity entity, String cfCode, Date date, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = getCFValueByKey(entity, cfCode, date, keys);
        if (value != null) {
            return value;
        }
        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValueByKey(parentCfEntity, cfCode, date, keys);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }
        return null;
    }

    /**
     * Match for a given entity's or its parent's custom field (non-versionable values) map's key as a range of numbers value and return a map value.
     * 
     * Number ranges is assumed to be the following format: &lt;number from&gt;&gt;&lt;number to&gt;
     *
     * @param entity        Entity to match
     * @param cfCode        Custom field code
     * @param numberToMatch Number (long, integer, double, bigdecimal) value to match
     * @return Map value that matches the range of numbers in a map key
     */
    public Object getInheritedCFValueByRangeOfNumbers(ICustomFieldEntity entity, String cfCode, Object numberToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = getCFValueByRangeOfNumbers(entity, cfCode, numberToMatch);
        if (value != null) {
            return value;
        }
        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValueByRangeOfNumbers(parentCfEntity, cfCode, numberToMatch);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }
        return null;
    }

    /**
     * Match for a given date (versionable values) for a given entity's custom field (versionable values) map's key as a range of numbers value and return a map value.
     * 
     * Number ranges is assumed to be the following format: &lt;number from&gt;&gt;&lt;number to&gt;
     *
     * @param entity        Entity to match
     * @param cfCode        Custom field code
     * @param date          Date to match
     * @param numberToMatch Number (long, integer, double, bigdecimal) value to match
     * @return Map value that matches the range of numbers in a map key
     */
    public Object getInheritedCFValueByRangeOfNumbers(ICustomFieldEntity entity, String cfCode, Date date, Object numberToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        Object value = getCFValueByRangeOfNumbers(entity, cfCode, date, numberToMatch);
        if (value != null) {
            return value;
        }
        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                // If Parent entity is Provider, lookup provider from appProvider as appProvider is not managed
                if (parentCfEntity instanceof Provider) {
                    parentCfEntity = providerService.findById(appProvider.getId());
                } else {
                    parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                }
                Object cfeValue = getInheritedCFValueByRangeOfNumbers(parentCfEntity, cfCode, date, numberToMatch);
                if (cfeValue != null) {
                    return cfeValue;
                }
            }
        }
        return null;
    }

    /**
     * A trigger when a future custom field end period event expired
     *
     * @param timer Timer information
     */
    @Timeout
    private void triggerEndPeriodEventExpired(Timer timer) {
        log.debug("Custom field value period has expired {}", timer);
        try {
            CFEndPeriodEvent event = (CFEndPeriodEvent) timer.getInfo();

            currentUserProvider.forceAuthentication(null, event.getProviderCode());
            cFEndPeriodEventProducer.fire(event);
        } catch (Exception e) {
            log.error("Failed executing end period event timer", e);
        }
    }

    /**
     * Initiate custom field end period event - either right away, or delay it for the future
     *
     */
    private void triggerEndPeriodEvent(ICustomFieldEntity entity, String cfCode, DatePeriod period) {

        if (period != null && period.getTo() != null && period.getTo().isBefore(Instant.now())) {
            CFEndPeriodEvent event = new CFEndPeriodEvent(entity, cfCode, period, currentUser.getProviderCode());
            cFEndPeriodEventProducer.fire(event);

        } else if (period != null && period.getTo() != null) {
            CFEndPeriodEvent event = new CFEndPeriodEvent(entity, cfCode, period, currentUser.getProviderCode());

            TimerConfig timerConfig = new TimerConfig();
            timerConfig.setInfo(event);

            // used for testing
            // expiration = new Date();
            // expiration = DateUtils.addMinutes(expiration, 1);

            log.debug("Creating timer for triggerEndPeriodEvent for Custom field value {} with expiration={}", event, period.getTo());

            timerService.createSingleActionTimer(Date.from(period.getTo()), timerConfig);
        }
    }

    private IEntity refreshOrRetrieveAny(IEntity entity) {

        if (entity.isTransient()) {
            return entity;
        }

        if (getEntityManager().contains(entity)) {
            // Entity is managed already, no need to refresh
            // getEntityManager().refresh(entity);
            return entity;

        } else {
            log.trace("Find {}/{} by id", entity.getClass().getSimpleName(), entity.getId());
            entity = getEntityManager().find(PersistenceUtils.getClassForHibernateObject(entity), entity.getId());
            return entity;
        }
    }

    /**
     * Match for a given entity's custom field (non-versionable values) as close as possible map's key to the key provided and return a map value. Match is performed by matching a
     * full string and then reducing one by one symbol until a match is found.
     * 
     *
     * @param entity     Entity to match
     * @param cfCode     Custom field code
     * @param keyToMatch Key to match
     * @return Map value that closely matches map key
     */
    public Object getCFValueByClosestMatch(ICustomFieldEntity entity, String cfCode, String keyToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return null;
        }

        Object value = entity.getCfValues().getValue(cfCode);
        Object valueMatched = CustomFieldUtils.matchClosestValue(value, keyToMatch);

        log.trace("Found closest match value {} for keyToMatch={}", valueMatched, keyToMatch);

        // Need to check if it is a multi-value type value and convert it to a map
        if (valueMatched != null && valueMatched instanceof String) {
            CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
            if (cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {
                return cft.deserializeMultiValue((String) valueMatched, null);
            }
        }

        return valueMatched;
    }

    /**
     * Match for a given date (versionable values) for a given entity's custom field as close as possible map's key to the key provided and return a map value. Match is performed
     * by matching a full string and then reducing one by one symbol until a match is found.
     * 
     *
     * @param entity     Entity to match
     * @param cfCode     Custom field code
     * @param date       Date
     * @param keyToMatch Key to match
     * @return Map value that closely matches map key
     */
    public Object getCFValueByClosestMatch(ICustomFieldEntity entity, String cfCode, Date date, String keyToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return null;
        }

        Object value = entity.getCfValues().getValue(cfCode, date);

        Object valueMatched = CustomFieldUtils.matchClosestValue(value, keyToMatch);
        log.trace("Found closest match value {} for period {} and keyToMatch={}", valueMatched, date, keyToMatch);

        // Need to check if it is a multi-value type value and convert it to a map
        if (valueMatched != null && valueMatched instanceof String) {
            CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
            if (cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {
                return cft.deserializeMultiValue((String) valueMatched, null);
            }
        }

        return valueMatched;
    }

    /**
     * Match for a given entity's custom field (non-versionable values) map's key (map or matrix) and return a map value.
     * 
     * For matrix, map key is assumed to be the following format. Note that MATRIX_STRING and MATRIX_RON keys can be mixed
     * 
     * &lt;matrix first key&gt;|&lt;matrix second key&gt;|&lt;matrix xx key&gt;|&lt;range of numbers for the third key&gt;
     *
     * @param entity Entity to match
     * @param cfCode Custom field code
     * @param keys   Keys to match. For matrix, the order must correspond to the order of the keys during data entry
     * @return Map value that matches the map key (map key or matrix formated map key)
     */
    @SuppressWarnings("unchecked")
    public Object getCFValueByKey(ICustomFieldEntity entity, String cfCode, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return null;
        }

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            log.trace("No CFT found {}/{}", entity, cfCode);
            return null;
        }

        if (cft.getStorageType() != CustomFieldStorageTypeEnum.MAP && cft.getStorageType() != CustomFieldStorageTypeEnum.MATRIX) {
            log.trace("getCFValueByKey does not apply to storage type {}", cft.getStorageType());
            return null;
        }
        if (keys.length == 0) {
            log.trace("getCFValueByKey needs at least one key passed");
            return null;
        }

        Map<String, Object> value = (Map<String, Object>) entity.getCfValues().getValue(cfCode);
        if (value == null) {
            return null;
        }
        Object valueMatched = null;
        if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
            valueMatched = CustomFieldUtils.matchMatrixValue(value, keys);

        } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
            if (keys[0] == null) {
                return null;
            }
            if (cft.getMapKeyType() == CustomFieldMapKeyEnum.STRING) {
                valueMatched = value.get(keys[0].toString());

            } else if (cft.getMapKeyType() == CustomFieldMapKeyEnum.RON) {
                if (keys[0] instanceof String) {
                    try {
                        keys[0] = Double.parseDouble((String) keys[0]);
                    } catch (NumberFormatException e) {
                        // Don't care about error nothing will be found later
                    }
                }
                valueMatched = CustomFieldInstanceService.matchRangeOfNumbersValue(value, keys[0]);
            }
        }

        log.trace("Found value match {} by keyToMatch={}", valueMatched, keys);

        // Need to check if it is a multi-value type value and convert it to a map
        if (valueMatched != null && valueMatched instanceof String && cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {
            return cft.deserializeMultiValue((String) valueMatched, null);
        }

        return valueMatched;

    }

    /**
     * Match for a given date (versionable values) for a given entity's custom field (versionable values) map's key (map or matrix) and return a map value.
     * 
     * For matrix, map key is assumed to be the following format. Note that MATRIX_STRING and MATRIX_RON keys can be mixed
     * 
     * &lt;matrix first key&gt;|&lt;matrix second key&gt;|&lt;matrix xx key&gt;|&lt;range of numbers for the third key&gt;
     *
     * @param entity Entity to match
     * @param cfCode Custom field code
     * @param date   Date to match
     * @param keys   Keys to match. For matrix, the order must correspond to the order of the keys during data entry
     * @return Map value that matches the map key (map key or matrix formated map key)
     */
    @SuppressWarnings("unchecked")
    public Object getCFValueByKey(ICustomFieldEntity entity, String cfCode, Date date, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return null;
        }

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            log.trace("No CFT found {}/{}", entity, cfCode);
            return null;
        }

        if (cft.getStorageType() != CustomFieldStorageTypeEnum.MAP && cft.getStorageType() != CustomFieldStorageTypeEnum.MATRIX) {
            log.trace("getCFValueByKey does not apply to storage type {}", cft.getStorageType());
            return null;
        }
        if (keys.length == 0) {
            log.trace("getCFValueByKey needs at least one key passed");
            return null;
        }

        Map<String, Object> value = (Map<String, Object>) entity.getCfValues().getValue(cfCode, date);
        if (value == null) {
            return null;
        }
        Object valueMatched = null;
        if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
            valueMatched = CustomFieldUtils.matchMatrixValue(value, keys);

        } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
            if (keys[0] == null) {
                return null;
            }
            if (cft.getMapKeyType() == CustomFieldMapKeyEnum.STRING) {
                valueMatched = value.get(keys[0].toString());

            } else if (cft.getMapKeyType() == CustomFieldMapKeyEnum.RON) {
                if (keys[0] instanceof String) {
                    try {
                        keys[0] = Double.parseDouble((String) keys[0]);
                    } catch (NumberFormatException e) {
                        // Don't care about error nothing will be found later
                    }
                }
                valueMatched = CustomFieldInstanceService.matchRangeOfNumbersValue(value, keys[0]);
            }
        }

        log.trace("Found matrix value match {} for period {} and keyToMatch={}", valueMatched, date, keys);

        // Need to check if it is a multi-value type value and convert it to a map
        if (valueMatched != null && valueMatched instanceof String && cft.getFieldType() == CustomFieldTypeEnum.MULTI_VALUE) {
            return cft.deserializeMultiValue((String) valueMatched, null);
        }

        return valueMatched;
    }

    /**
     * Match for a given entity's custom field (non-versionable values) map's key as a range of numbers value and return a map value.
     * 
     * Number ranges is assumed to be the following format: &lt;number from&gt;&gt;&lt;number to&gt;
     *
     * @param entity        Entity to match
     * @param cfCode        Custom field code
     * @param numberToMatch Number (long, integer, double, bigdecimal) value to match
     * @return Map value that matches the range of numbers in a map key
     */
    @SuppressWarnings("unchecked")
    public Object getCFValueByRangeOfNumbers(ICustomFieldEntity entity, String cfCode, Object numberToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return null;
        }

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            log.trace("No CFT found {}/{}", entity, cfCode);
            return null;
        }

        if (!(cft.getStorageType() == CustomFieldStorageTypeEnum.MAP && cft.getMapKeyType() == CustomFieldMapKeyEnum.RON)) {
            log.trace("getCFValueByRangeOfNumbers does not apply to storage type {} and mapKeyType {}", cft.getStorageType(), cft.getMapKeyType());
            return null;
        }

        Map<String, Object> value = (Map<String, Object>) entity.getCfValues().getValue(cfCode);
        Object valueMatched = CustomFieldInstanceService.matchRangeOfNumbersValue(value, numberToMatch);

        log.trace("Found map value match {} for numberToMatch={}", valueMatched, numberToMatch);
        return valueMatched;

    }

    /**
     * Match for a given date (versionable values) for a given entity's custom field (versionable values) map's key as a range of numbers value and return a map value.
     * 
     * Number ranges is assumed to be the following format: &lt;number from&gt;&gt;&lt;number to&gt;
     *
     * @param entity        Entity to match
     * @param cfCode        Custom field code
     * @param date          Date to match
     * @param numberToMatch Number (long, integer, double, bigdecimal) value to match
     * @return Map value that matches the range of numbers in a map key
     */
    public Object getCFValueByRangeOfNumbers(ICustomFieldEntity entity, String cfCode, Date date, Object numberToMatch) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return null;
        }

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            log.trace("No CFT found {}/{}", entity, cfCode);
            return null;
        }

        if (!(cft.getStorageType() == CustomFieldStorageTypeEnum.MAP && cft.getMapKeyType() == CustomFieldMapKeyEnum.RON)) {
            log.trace("getCFValueByRangeOfNumbers does not apply to storage type {} and mapKeyType {}", cft.getStorageType(), cft.getMapKeyType());
            return null;
        }

        Object value = entity.getCfValues().getValue(cfCode, date);
        Object valueMatched = CustomFieldInstanceService.matchRangeOfNumbersValue(value, numberToMatch);

        log.trace("Found matrix value match {} for period {} and numberToMatch={}", valueMatched, date, numberToMatch);
        return valueMatched;

    }

    /**
     * Match map's key as a range of numbers value and return a matched value.
     * 
     * Number ranges is assumed to be the following format: &lt;number from&gt;&lt;&lt;number to&gt;
     *
     * @param value         Value to inspect
     * @param numberToMatch Number to match
     * @return Map value that closely matches map key
     */
    @SuppressWarnings("unchecked")
    private static Object matchRangeOfNumbersValue(Object value, Object numberToMatch) {
        if (value == null || !(value instanceof Map) || numberToMatch == null
                || !(numberToMatch instanceof Long || numberToMatch instanceof Integer || numberToMatch instanceof Double || numberToMatch instanceof BigDecimal)) {
            return null;
        }

        for (Entry<String, Object> valueInfo : ((Map<String, Object>) value).entrySet()) {
            if (CustomFieldUtils.isNumberRangeMatch(valueInfo.getKey(), numberToMatch)) {
                return valueInfo.getValue();
            }
        }

        return null;
    }

    /**
     * Check if a match map's key as a range of numbers value is present.
     * 
     * Number ranges is assumed to be the following format: &lt;number from&gt;&lt;&lt;number to&gt;
     *
     * @param value         Value to inspect
     * @param numberToMatch Number to match
     * @return True if map value matches map key
     */
    @SuppressWarnings("unchecked")
    private  static boolean isMatchRangeOfNumbersValue(Object value, Object numberToMatch) {
        if (value == null || !(value instanceof Map) || numberToMatch == null
                || !(numberToMatch instanceof Long || numberToMatch instanceof Integer || numberToMatch instanceof Double || numberToMatch instanceof BigDecimal)) {
            return false;
        }

        for (Entry<String, Object> valueInfo : ((Map<String, Object>) value).entrySet()) {
            if (CustomFieldUtils.isNumberRangeMatch(valueInfo.getKey(), numberToMatch)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Instantiate a custom field value with default value for a given entity. If custom field is versionable, a current date will be used to access the value.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @return Custom field value
     */
    public Object instantiateCFWithDefaultValue(ICustomFieldEntity entity, String cfCode) {

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            // log.trace("No CFT found or no default value specified {}/{}", entity, code);
            return null;
        }

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        return instantiateCFWithDefaultValue(entity, cft);
    }

    /**
     * Instantiate a custom field value with default or inherited value for a given entity. If custom field is versionable, a current date will be used to access the value.
     *
     * @param entity Entity
     * @param cft    Custom field definition
     * @return Custom field value
     */
    public Object instantiateCFWithInheritedOrDefaultValue(ICustomFieldEntity entity, CustomFieldTemplate cft) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (cft.isUseInheritedAsDefaultValue()) {
            Object value = getInheritedOnlyCFValue(entity, cft.getCode());
            if (value != null) {
                try {
                    if (cft.isVersionable()) {
                        setCFValue(entity, cft.getCode(), value, new Date());
                    } else {
                        setCFValue(entity, cft.getCode(), value);
                    }
                    return value;
                } catch (BusinessException e) {
                    log.error("Failed to instantiate field with inherited value as default value {}/{}", entity.getClass().getSimpleName(), cft.getCode(), e);
                }
            }
        }

        return instantiateCFWithDefaultValue(entity, cft.getCode());
    }

    /**
     * Instantiate a custom field value with default value for a given entity and a date.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @param date   Date
     * @return Custom field value
     */
    private Object instantiateCFWithDefaultValue(ICustomFieldEntity entity, String cfCode, Date date) {

        // If field is not versionable - get the value without the date
        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            // log.trace("No CFT found or no default value or calendar specified {}/{}", entity, code);
            return null;
        }

        return instantiateCFWithDefaultValue(entity, cft, date);
    }

    /**
     * Instantiate a custom field value with default value for a given entity. If custom field is versionable, a current date will be used to access the value. Can be instantiated
     * only if cft.applicableOnEl condition pass
     *
     * @param entity Entity
     * @param cft    Custom field template
     * @return Custom field value
     */
    private Object instantiateCFWithDefaultValue(ICustomFieldEntity entity, CustomFieldTemplate cft) {

        Object value = cft.getDefaultValueConverted();

        if (value == null || StringUtils.isEmpty(value.toString()) || cft.getStorageType() != CustomFieldStorageTypeEnum.SINGLE || !isCFTApplicableToEntity(cft, entity)) {
            // log.trace("No CFT found or no default value specified {}/{}", entity, cft.getCode());
            return null;
        }

        if (cft.isVersionable()) {
            log.warn("Trying to instantiate CF value from default value on a versionable custom field {}/{} value with no provided date. Current date will be used",
                    entity.getClass().getSimpleName(), cft.getCode());
            return instantiateCFWithDefaultValue(entity, cft, new Date());
        }

        // Create such CF with default value if one is specified on CFT
        entity.getCfValuesNullSafe().setValue(cft.getCode(), value);

        return value;
    }

    /**
     * Instantiate a custom field value with default value for a given entity and a date. Can be instantiated only if values are versioned by a calendar and cft.applicableOnEl
     * condition pass
     *
     * @param entity Entity
     * @param cft    Custom field template
     * @param date   Date
     * @return Custom field value
     */
    private Object instantiateCFWithDefaultValue(ICustomFieldEntity entity, CustomFieldTemplate cft, Date date) {

        Object value = cft.getDefaultValueConverted();

        if (value == null || StringUtils.isEmpty(value.toString()) || cft.getCalendar() == null || cft.getStorageType() != CustomFieldStorageTypeEnum.SINGLE || !isCFTApplicableToEntity(cft, entity)) {
            // log.trace("No CFT found or no default value or calendar specified {}/{}", entity, code);
            return null;
        }

        // If field is not versionable - instantiate the value without the date
        if (!cft.isVersionable()) {
            return instantiateCFWithDefaultValue(entity, cft);
        }

        entity.getCfValuesNullSafe().setValue(cft.getCode(), cft.getDatePeriod(date), null, value);

        return value;
    }

    /**
     * Check if a given entity has a CF value of type Map or Matrix with a given key.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @param keys   Key or keys (in case of matrix) to match
     * @return True if CF value has a given key
     */
    @SuppressWarnings("unchecked")
    public boolean isCFValueHasKey(ICustomFieldEntity entity, String cfCode, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return false;
        }

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            log.trace("No CFT found {}/{}", entity, cfCode);
            return false;
        }

        if (cft.getStorageType() != CustomFieldStorageTypeEnum.MAP && cft.getStorageType() != CustomFieldStorageTypeEnum.MATRIX) {
            log.trace("isCFValueHasKey does not apply to storage type {}", cft.getStorageType());
            return false;
        }
        if (keys.length == 0) {
            log.trace("isCFValueHasKey needs at least one key passed");
            return false;
        }

        Map<String, Object> value = (Map<String, Object>) entity.getCfValues().getValue(cfCode);
        if (value == null) {
            return false;
        }
        boolean hasKey = false;
        if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
            hasKey = CustomFieldUtils.isMatchMatrixValue(cft, value, keys);

        } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
            if (keys[0] == null) {
                return false;
            }
            if (cft.getMapKeyType() == CustomFieldMapKeyEnum.STRING) {
                hasKey = value.containsKey(keys[0].toString());

            } else if (cft.getMapKeyType() == CustomFieldMapKeyEnum.RON) {
                if (keys[0] instanceof String) {
                    try {
                        keys[0] = Double.parseDouble((String) keys[0]);
                    } catch (NumberFormatException e) {
                        // Don't care about error nothing will be found later
                    }
                }
                hasKey = CustomFieldInstanceService.isMatchRangeOfNumbersValue(value, keys[0]);
            }
        }
        log.trace("Value match {} for keyToMatch={}", hasKey, keys);
        return hasKey;
    }

    /**
     * Check if a given entity at a given period date has a CF value of type Map or Matrix with a given key.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @param date   Date
     * @param keys   Key or keys (in case of matrix) to match
     * @return True if CF value has a given key at a given period date
     */
    @SuppressWarnings("unchecked")
    public boolean isCFValueHasKey(ICustomFieldEntity entity, String cfCode, Date date, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        if (entity.getCfValues() == null) {
            return false;
        }

        CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(cfCode, entity);
        if (cft == null) {
            log.trace("No CFT found {}/{}", entity, cfCode);
            return false;
        }

        if (cft.getStorageType() != CustomFieldStorageTypeEnum.MAP && cft.getStorageType() != CustomFieldStorageTypeEnum.MATRIX) {
            log.trace("isCFValueHasKey does not apply to storage type {}", cft.getStorageType());
            return false;
        }
        if (keys.length == 0) {
            log.trace("isCFValueHasKey needs at least one key passed");
            return false;
        }

        Map<String, Object> value = (Map<String, Object>) entity.getCfValues().getValue(cfCode, date);
        if (value == null) {
            return false;
        }
        boolean hasKey = false;
        if (cft.getStorageType() == CustomFieldStorageTypeEnum.MATRIX) {
            hasKey = CustomFieldUtils.isMatchMatrixValue(cft, value, keys);

        } else if (cft.getStorageType() == CustomFieldStorageTypeEnum.MAP) {
            if (keys[0] == null) {
                return false;
            }
            if (cft.getMapKeyType() == CustomFieldMapKeyEnum.STRING) {
                hasKey = value.containsKey(keys[0].toString());

            } else if (cft.getMapKeyType() == CustomFieldMapKeyEnum.RON) {
                if (keys[0] instanceof String) {
                    try {
                        keys[0] = Double.parseDouble((String) keys[0]);
                    } catch (NumberFormatException e) {
                        // Don't care about error nothing will be found later
                    }
                }
                hasKey = CustomFieldInstanceService.isMatchRangeOfNumbersValue(value, keys[0]);
            }

        }
        log.trace("Value match {} for date for keyToMatch={}", hasKey, date, keys);
        return hasKey;
    }

    /**
     * Check if a given entity or its parents have a CF value of type Map or Matrix with a given key.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @param keys   Key or keys (in case of matrix) to match
     * @return True if CF value has a given key
     */
    public boolean isInheritedCFValueHasKey(ICustomFieldEntity entity, String cfCode, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        boolean hasKey = isCFValueHasKey(entity, cfCode, keys);
        if (hasKey) {
            return true;
        }

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                hasKey = isInheritedCFValueHasKey(parentCfEntity, cfCode, keys);
                if (hasKey) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Check if a given entity or its parents at a given perio date have a CF value of type Map or Matrix with a given key.
     *
     * @param entity Entity
     * @param cfCode Custom field code
     * @param date   Date
     * @param keys   Key or keys (in case of matrix) to match
     * @return True if CF value has a given key at a given perio date
     */
    public boolean isInheritedCFValueHasKey(ICustomFieldEntity entity, String cfCode, Date date, Object... keys) {

        // Handle cases when appProvider was passed instead of a real Provider entity. The class in this case is org.meveo.model.crm.Provider$Proxy$_$$_WeldClientProxy
        if (entity instanceof Provider && entity.getClass().getSimpleName().contains("Proxy")) {
            entity = providerService.findById(appProvider.getId());
        }

        boolean hasKey = isCFValueHasKey(entity, cfCode, date, keys);
        if (hasKey) {
            return true;
        }

        ICustomFieldEntity[] parentCfEntities = entity.getParentCFEntities();
        if (parentCfEntities != null) {
            for (ICustomFieldEntity parentCfEntity : parentCfEntities) {
                if (parentCfEntity == null) {
                    continue;
                }
                parentCfEntity = (ICustomFieldEntity) refreshOrRetrieveAny((IEntity) parentCfEntity);
                hasKey = isInheritedCFValueHasKey(parentCfEntity, cfCode, date, keys);
                if (hasKey) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Deprecated. See getCFValueByKey function
     *
     * @param entity custom field entity
     * @param cfCode custom field code
     * @param keys   list of key
     * @return custom field value.
     */
    @Deprecated
    public Object getCFValueByMatrix(ICustomFieldEntity entity, String cfCode, Object... keys) {
        return getCFValueByKey(entity, cfCode, keys);
    }

    /**
     * Deprecated. See getCFValueByKey function
     *
     * @param entity custom field entity
     * @param cfCode custom field code
     * @param date   date to check
     * @param keys   list of key
     * @return custom field value.
     */
    @Deprecated
    public Object getCFValueByMatrix(ICustomFieldEntity entity, String cfCode, Date date, Object... keys) {
        return getCFValueByKey(entity, cfCode, date, keys);
    }

    /**
     * Deprecated. See getInheritedCFValueByKey function
     *
     * @param entity custom field entity
     * @param cfCode custom field code
     * @param keys   list of key
     * @return custom field value.
     */
    @Deprecated
    public Object getInheritedCFValueByMetrix(ICustomFieldEntity entity, String cfCode, Object... keys) {
        return getInheritedCFValueByKey(entity, cfCode, keys);
    }

    /**
     * Deprecated. See getInheritedCFValueByKey function
     *
     * @param entity custom field entity
     * @param cfCode custom field code
     * @param date   date to check
     * @param keys   list of key
     * @return custom field value.
     */
    @Deprecated
    public Object getInheritedCFValueByMatrix(ICustomFieldEntity entity, String cfCode, Date date, Object... keys) {
        return getInheritedCFValueByKey(entity, cfCode, date, keys);
    }

    /**
     * Check if Custom field template is applicable to a given entity - evaluate cft.applicableOnEl expression is set
     *
     * @param cft    Custom field template
     * @param entity Entity to check
     * @return True if cft.applicableOnEl expression is null or evaluates to true
     */
    private boolean isCFTApplicableToEntity(CustomFieldTemplate cft, ICustomFieldEntity entity) {
        if (cft.getApplicableOnEl() != null) {
            return MeveoValueExpressionWrapper.evaluateToBooleanIgnoreErrors(cft.getApplicableOnEl(), "entity", entity);
        }
        return true;
    }

    /**
     * Schedule end period events for an entity if applicable.
     *
     * @param entity Entity
     */
    public void scheduleEndPeriodEvents(ICustomFieldEntity entity) {

        if (entity.getCfValues() == null) {
            return;
        }

        Map<String, List<DatePeriod>> newCfValuePeriods = entity.getCfValues().getNewVersionedCFValuePeriods();
        if (newCfValuePeriods == null || newCfValuePeriods.isEmpty()) {
            return;
        }

        for (Entry<String, List<DatePeriod>> periodInfo : newCfValuePeriods.entrySet()) {
            CustomFieldTemplate cft = cfTemplateService.findByCodeAndAppliesTo(periodInfo.getKey(), entity);
            if (cft != null && cft.isTriggerEndPeriodEvent()) {
                for (DatePeriod period : periodInfo.getValue()) {
                    triggerEndPeriodEvent(entity, periodInfo.getKey(), period);
                }
            }
        }
    }

    private EntityManager getEntityManager() {
        return emWrapper.getEntityManager();
    }
    
    /**
     * Sets the {@link CustomFieldValues} of a given {@link CustomEntityInstance}.
     * @param entity the custom entity instance
     * @param cetCode custom entity template code
     * @param values map of cft values
     * @throws BusinessException thrown when values are not set
     */
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public void setCfValues(ICustomFieldEntity entity, String cetCode, Map<String, Object> values) throws BusinessException {
		Map<String, CustomFieldTemplate> cetFields = null;
		try {
			cetFields = customFieldTemplateService.findByAppliesTo(entity);
			if(entity instanceof CustomEntityInstance) {
				var cei = (CustomEntityInstance) entity;
				if(cei.getCet() != null && cei.getCet().getSuperTemplate() != null) {
					cetFields = customFieldTemplateService.getCftsWithInheritedFields(cei.getCet());
				}
			}
			
			for (Map.Entry<String, CustomFieldTemplate> cetField : cetFields.entrySet()) {
				Object value = values.getOrDefault(cetField.getKey(), values.get(cetField.getValue().getDbFieldname()));
				if (cetField.getValue().getFieldType().name().equals("BOOLEAN") && value instanceof Integer) {
				    if ((Integer) value == 1) {
			            value = true;
			        } else {
				        value = false;
			        }
				}
				
			    if (cetField.getValue().getFieldType().name().equals("ENTITY") && value instanceof BigInteger) {
			        EntityReferenceWrapper entityReferenceWrapper = new EntityReferenceWrapper();
			        if (cetField.getValue().getEntityClazz().equals(User.class.getName())) {
			            User user = userService.findById(((BigInteger) value).longValue());
			            entityReferenceWrapper.setCode(user.getUserName());
			        } else if (cetField.getValue().getEntityClazz().equals(Provider.class.getName())) {
			            Provider provider = providerService.findById(((BigInteger) value).longValue());
			            entityReferenceWrapper.setCode(provider.getCode());
			        }
			        entityReferenceWrapper.setClassname(cetField.getValue().getEntityClazz());
			        entityReferenceWrapper.setId(((BigInteger) value).longValue());
			        value = entityReferenceWrapper;
			        
			    } /* else if (cetField.getValue().getFieldType().name().equals("ENTITY") && value instanceof String) {
			    	EntityReferenceWrapper entityReferenceWrapper = new EntityReferenceWrapper();
			        if (cetField.getValue().getEntityClazz().equals(User.class.getName())) {
			            User user = userService.findById(((BigInteger) value).longValue());
			            entityReferenceWrapper.setCode(user.getUserName());
			        } else if (cetField.getValue().getEntityClazz().equals(Provider.class.getName())) {
			            Provider provider = providerService.findById(((BigInteger) value).longValue());
			            entityReferenceWrapper.setCode(provider.getCode());
			        }
			        entityReferenceWrapper.setClassname(cetField.getValue().getEntityClazz());
			        entityReferenceWrapper.setId(((BigInteger) value).longValue());
			        value = entityReferenceWrapper;
			    } */

			    setCFValue(entity, cetField.getKey(), value);
			}
		} catch (NullPointerException e) {
			if (entity == null)
				throw new RuntimeException("NullPointerException cetCode:" + cetCode + " entity: null", e);
			throw new RuntimeException("nullpointer cetCode:" + cetCode + " entity class:"+entity.getClass().getName() + " values:" + values+ " cetFields:"+cetFields, e);
		}
		
	}
	
	/**
	 * Try to fetch the code of the referenced entity if exists. If cft is null, will do nothing.
	 * 
	 * Note : silently fails if error occurs
	 * 
	 * @param cft     The cft corresponding to the 'code' field
	 * @param uuid    Uuid of the referenced entity
	 * @param wrapper reference wrapper object to update
	 */
	private void fetchCode(CustomFieldTemplate cft, String uuid, EntityReferenceWrapper wrapper) {
		try {
			
			String appliesTo = CustomEntityTemplate.getAppliesTo(cft.getEntityClazzCetCode());
			CustomFieldTemplate codeCft = cfTemplateService.findByCodeAndAppliesTo("code", appliesTo);
			if(codeCft != null) {
				CustomEntityTemplate refCet = customEntityTemplateService.findByCode(cft.getEntityClazzCetCode());
				Map<String, Object> result = crossStorageService.findById(repositoryService.findByCode(wrapper.getRepository()), 
						refCet, 
						uuid,
						Collections.singletonList("code"), 
						new HashMap<>(), 
						false);
				wrapper.setCode((String) result.get("code")); 
			}
			
		} catch (Exception e) {
			//NOOP
		}
	}
}