package org.meveo.api.module;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ModuleUtil;
import org.meveo.api.*;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomEntityInstanceDto;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.api.dto.EntityCustomActionDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.api.exceptions.ModuleInstallFail;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.DatePeriod;
import org.meveo.model.ModuleInstall;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ModulePostInstall;
import org.meveo.model.VersionedEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.EntityCustomActionService;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.module.ModuleScriptInterface;
import org.meveo.service.script.module.ModuleScriptService;
import org.slf4j.Logger;

/**
 * Meveo module installer.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Stateless
public class MeveoModuleItemInstaller {
	
    public static final ConcurrentHashMap<String, Class<?>> MODULE_ITEM_TYPES = new ConcurrentHashMap<>();
	
    @Inject
    private CustomFieldTemplateApi customFieldTemplateApi;
    
    @Inject
    private EntityCustomActionApi entityCustomActionApi;
    
    @Inject
    private Logger log;
    
    @Inject
    private MeveoModuleService meveoModuleService;
    
    @Inject
    private ModuleScriptService moduleScriptService;

    @Inject
	private CrossStorageService crossStorageService;
    
    @Inject
    private ConcreteFunctionService concreteFunctionService;

    @Inject
	private CustomEntityTemplateService customEntityTemplateService;

    @Inject
	private MeveoModuleApi meveoModuleApi;

	@Inject
	private EntityCustomActionService entityCustomActionService;

	@Inject
	private ScriptInstanceService scriptInstanceService;
	
	@Inject
	private ModuleInstallationContext installCtx;

	@Inject
	@ModuleInstall
	private Event<MeveoModule> installEvent;

	@Inject
	@ModulePostInstall
	private Event<MeveoModule> postInstallEvent;
	
	@EJB
	private MeveoModuleItemInstaller meveoModuleItemInstaller;
	
	@Inject
	private Repository currentRepository;
    
    /**
     * Uninstall the module and disables it items
     * 
     * @param module the module to uninstall
     * @return the uninstalled module
     * @throws BusinessException if an error occurs
     */
    public MeveoModule uninstall(MeveoModule module) throws BusinessException {
        return uninstall(module, false, false);
    }

	/**
	 * Uninstall the module and disables it items
	 * 
	 * @param module      the module to uninstall
	 * @param removeItems if true, module items will be deleted and not disabled
	 * @return the uninstalled module
	 * @throws BusinessException if an error occurs
	 */
    public MeveoModule uninstall(MeveoModule module, boolean removeItems) throws BusinessException {
        return uninstall(module, false, removeItems);
    }

	/**
	 * Uninstall the module and disables it items
	 * 
	 * @param module      the module to uninstall
	 * @param childModule whether the module is a child module
	 * @param removeItems if true, module items will be deleted and not disabled
	 * @return the uninstalled module
	 * @throws BusinessException if an error occurs
	 */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private MeveoModule uninstall(MeveoModule module, boolean childModule, boolean removeItems) throws BusinessException {

        ModuleScriptInterface moduleScript = null;
        if (module.getScript() != null) {
            moduleScript = moduleScriptService.preUninstallModule(module.getScript().getCode(), module);
        }
        
        List<MeveoModuleItem> moduleItems = meveoModuleService.getSortedModuleItems(module.getModuleItems());

        // Load items
        for (MeveoModuleItem item : List.copyOf(moduleItems)) {
            // check if moduleItem is linked to other active module
            if (meveoModuleService.isChildOfOtherActiveModule(item.getItemCode(), item.getItemClass())) {
            	moduleItems.remove(item);
            	continue;
            }

            if (item.getItemClass().equals(CustomEntityInstance.class.getName()) && item.getAppliesTo() != null) {
            	continue;
			}

            meveoModuleService.loadModuleItem(item);
        }
        
        for (MeveoModuleItem item : moduleItems) {
            BusinessEntity itemEntity = item.getItemEntity();
            
            if (item.getItemClass().equals(CustomEntityInstance.class.getName()) && item.getAppliesTo() != null) {
                var cet = customEntityTemplateService.findByCode(item.getAppliesTo());
                crossStorageService.remove(currentRepository, cet, item.getItemCode());
            	continue;
			}
            
            if (itemEntity == null) {
            	log.error("Failed to load item {}, it won't be uninstalled");
                continue;
            }
            
            try {
                if (itemEntity instanceof MeveoModule) {
                    uninstall((MeveoModule) itemEntity, true, removeItems);
                } else if(itemEntity instanceof CustomFieldTemplate) {
                	customFieldTemplateApi.remove(itemEntity.getCode(), ((CustomFieldTemplate) itemEntity).getAppliesTo());
                } else if(itemEntity instanceof EntityCustomAction) {
                	entityCustomActionApi.remove(itemEntity.getCode(), ((EntityCustomAction) itemEntity).getAppliesTo());
                } else {

                    // Find API service class first trying with item's classname and then with its super class (a simplified version instead of trying various class
                    // superclasses)
                    Class clazz = itemEntity.getClass();
                    BaseCrudApi api = (BaseCrudApi) ApiUtils.getApiService(clazz, true);

                    if (api == null) {
                        log.error("Failed to find implementation of api for class {}", item.getItemClass());
                        continue;
                    }
                    
                    if(removeItems) {
						// Handle case where the install script is bundled in the module items
						if(moduleScript != null && itemEntity.getCode().equals(moduleScript.getClass().getName())) {
							scriptInstanceService.disable((ScriptInstance) itemEntity);
							continue;
						}
						
                        if (itemEntity instanceof Endpoint) {
                            Endpoint endpoint = (Endpoint) itemEntity;
                            if (CollectionUtils.isNotEmpty(endpoint.getPathParametersNullSafe())) {
                            	meveoModuleService.getEntityManager().createNamedQuery("deletePathParameterByEndpoint")
                                        .setParameter("endpointId", endpoint.getId())
                                        .executeUpdate();
                            }
                            if (CollectionUtils.isNotEmpty(endpoint.getParametersMapping())) {
                            	meveoModuleService.getEntityManager().createNamedQuery("TSParameterMapping.deleteByEndpoint")
                                        .setParameter("endpointId", endpoint.getId())
                                        .executeUpdate();
                            }
                            Function service = concreteFunctionService.findById(endpoint.getService().getId());
                            meveoModuleService.getEntityManager().createNamedQuery("Endpoint.deleteById")
                                    .setParameter("endpointId", endpoint.getId())
                                    .executeUpdate();
                            log.info("uninstalled endpoint {} / {}", endpoint.getClass(), endpoint.getId());
                        } else {
                        	log.info("Uninstalling module item {}", item);
							if (itemEntity instanceof ScriptInstance) {
								List<EntityCustomAction> entityCustomActions = entityCustomActionService.list();
								ScriptInstance scriptInstance = scriptInstanceService.findByCode(itemEntity.getCode());
								if (CollectionUtils.isNotEmpty(entityCustomActions)) {
									for (EntityCustomAction entityCustomAction : entityCustomActions) {
										if (entityCustomAction.getScript().equals(scriptInstance)) {
											entityCustomActionService.remove(entityCustomAction);
										}
									}
								}
							}
                        	api.getPersistenceService().remove(itemEntity);
                        }
                        
					} else {
						
						if(api.getPersistenceService() instanceof PersistenceService) {
							try {
								((PersistenceService) api.getPersistenceService()).disableNoMerge(itemEntity);
							} catch (Exception e) {
								log.error("Failed to disable {}", itemEntity);
							}
						}
					}

                }
                
            } catch (Exception e) {
            	Throwable cause = e;
            	if (e instanceof EJBTransactionRolledbackException && e.getCause() != null) {
            		while (! (cause instanceof SQLException) && cause.getCause() != null) {
            			cause = cause.getCause();
            		}
            		if (! (cause instanceof SQLException))
            			cause = e.getCause();
            	}
                throw new BusinessException("Failed to uninstall/disable module item " + item + " (cause : "+ cause.getMessage() + ")",e);
            }
        }

        if (moduleScript != null) {
            moduleScriptService.postUninstallModule(moduleScript, module);
        }
        
        // Remove the install script
        if(moduleScript != null) {
        	module.setScript(null);
        	var moduleScriptInstance = scriptInstanceService.findByCode(moduleScript.getClass().getName());
        	if(moduleScriptInstance != null) {
        		scriptInstanceService.remove(moduleScriptInstance);
        	}
        }

        // Remove if it is a child module
        if (childModule) {
        	meveoModuleService.remove(module);
            return null;

        // Otherwise mark it uninstalled and clear module items
        } else {
            MeveoModule moduleUpdated = module;
            module.setInstalled(false);
            // moduleUpdated.getModuleItems().clear();

            /* In case the module is uninstalled because of installation failure
               and that the module is not inserted in db we should not update its persistent state */
            module = meveoModuleService.reattach(module);
            if(meveoModuleService.getEntityManager().contains(module)) {
            	moduleUpdated = meveoModuleService.update(module);
            }
            
			return moduleUpdated;
        }
        
    }
    
    public ModuleInstallResult install(MeveoModule meveoModule, MeveoModuleDto moduleDto, OnDuplicate onDuplicate) throws MeveoApiException, BusinessException {
    	installEvent.fire(meveoModule);
    	installCtx.begin(meveoModule);
    	ModuleInstallResult result = new ModuleInstallResult();
    	
        boolean installed = false;
        if (!meveoModule.isDownloaded()) {
            throw new ActionForbiddenException(meveoModule.getClass(), moduleDto.getCode(), "install", "Module with the same code is being developped locally, can not overwrite it.");
        }

        if (meveoModule.isInstalled()) {
            installed = true;

        } else {
            moduleDto = MeveoModuleUtils.moduleSourceToDto(meveoModule);
        }

        if (!installed) {
        	
        	try {
        		
	            ModuleScriptInterface moduleScript = null;
	            if (meveoModule.getScript() != null) {
	                moduleScript = moduleScriptService.preInstallModule(meveoModule.getScript().getCode(), meveoModule);
	            }
	            
	            unpackAndInstallModuleItems(result, meveoModule, moduleDto, onDuplicate);
	
	            meveoModule.setInstalled(true);
	
	            if (moduleScript != null) {
	                moduleScriptService.postInstallModule(moduleScript, meveoModule);
	            }
	            
	            result.setInstalledModule(meveoModule);
	            postInstallEvent.fire(meveoModule);
	            installCtx.end();
        	} catch(Exception e) {
            	throw new ModuleInstallFail(meveoModule, result, e);
            }

        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Transactional(TxType.REQUIRES_NEW)
	public void uninstallItemDto(MeveoModule meveoModule, MeveoModuleItemDto moduleItemDto) throws BusinessException {
    	Class<? extends BaseEntityDto> dtoClass;
		
    	try {
			dtoClass = (Class<? extends BaseEntityDto>) Class.forName(moduleItemDto.getDtoClassName());
		} catch (ClassNotFoundException e) {
			throw new BusinessException("Can't find DTO class", e);
		}
		
		BaseEntityDto dto = JacksonUtil.convert(moduleItemDto.getDtoData(), dtoClass);
		
		log.info("Uninstalling item {}", dto);

    	if(dto instanceof MeveoModuleDto) {
        	MeveoModule subModule = meveoModuleService.findByCode(((MeveoModuleDto) dto).getCode());
    		uninstall(subModule);
    		
    	} else if(dto instanceof CustomFieldTemplateDto) {
    		try {
				customFieldTemplateApi.remove(dto.getCode(), ((CustomFieldTemplateDto) dto).getAppliesTo());
			} catch (EntityDoesNotExistsException e) {
				// Do nothing
			} catch (MeveoApiException e) {
				throw new BusinessException("Can't remove cft " + dto, e);
			}
    		
    	} else if (dto instanceof EntityCustomActionDto) { 
    		try {
				entityCustomActionApi.remove(dto.getCode(), ((EntityCustomActionDto) dto).getAppliesTo());
			} catch (EntityDoesNotExistsException e) {
				// Do nothing
			} catch (MissingParameterException e) {
				throw new BusinessException(e);
			}
    		
    	} else {
    		
            String moduleItemName = dto.getClass().getSimpleName().substring(0, dto.getClass().getSimpleName().lastIndexOf("Dto"));
            Class<?> entityClass = MODULE_ITEM_TYPES.get(moduleItemName);
            if(entityClass == null) {
            	throw new IllegalArgumentException(moduleItemName + " is not a module item" );
            }
            
		    ApiService<?,BaseEntityDto> apiService = ApiUtils.getApiService(entityClass, true);
		    try {
				apiService.remove(dto);
		    } catch (EntityDoesNotExistsException e) {
				// Do nothing
			} catch (MeveoApiException e) {
				throw new BusinessException("Can't remove dto " + dto, e);
			}
    	}
    }
	
	@SuppressWarnings({ "unchecked" })
	@Transactional(TxType.MANDATORY)
	public ModuleInstallResult unpackAndInstallModuleItem(MeveoModule meveoModule, MeveoModuleItemDto moduleItemDto, OnDuplicate onDuplicate) throws IllegalArgumentException, MeveoApiException, Exception, BusinessException {
		ModuleInstallResult result = new ModuleInstallResult();

		Class<? extends BaseEntityDto> dtoClass;

		boolean skipped = false;
		MeveoModuleItem moduleItem;

		try {
			dtoClass = (Class<? extends BaseEntityDto>) Class.forName(moduleItemDto.getDtoClassName());
			BaseEntityDto dto = JacksonUtil.convert(moduleItemDto.getDtoData(), dtoClass);

			CustomEntityTemplate customEntityTemplate = null;
			if (dto instanceof CustomEntityInstanceDto) {
				customEntityTemplate = customEntityTemplateService.findByCode(((CustomEntityInstanceDto) dto).getCetCode());
			}
			
		    try {

		        if (dto instanceof MeveoModuleDto) {
		        	MeveoModule subModule = meveoModuleService.findByCode(((MeveoModuleDto) dto).getCode());
		        	result = install(subModule, (MeveoModuleDto) dto, onDuplicate);

		            Class<? extends MeveoModule> moduleClazz = MeveoModule.class;
		            moduleItem = new MeveoModuleItem(((MeveoModuleDto) dto).getCode(), moduleClazz.getName(), null, null);
		            meveoModuleService.addModuleItem(moduleItem, meveoModule);

		        } else if (dto instanceof CustomEntityInstanceDto && customEntityTemplate != null && (customEntityTemplate.isStoreAsTable() || customEntityTemplate.storedIn(DBStorageType.NEO4J))) {
                    CustomEntityInstance cei = new CustomEntityInstance();
                    cei.setUuid(dto.getCode());
                    cei.setCetCode(customEntityTemplate.getCode());
                    cei.setCet(customEntityTemplate);
                    
                    try {
                        meveoModuleApi.populateCustomFields(((CustomEntityInstanceDto) dto).getCustomFields(), cei, true);
                    } catch (Exception e) {
                        log.error("Failed to associate custom field instance to an entity: {}", e.getMessage());
                        throw e;
                    }
                    
                    crossStorageService.createOrUpdate(currentRepository, cei);
                    
					moduleItem = new MeveoModuleItem(dto.getCode(), CustomEntityInstance.class.getName(), cei.getCetCode(), null);
					meveoModuleService.addModuleItem(moduleItem, meveoModule);
                } else if (dto instanceof CustomFieldTemplateDto) {
	        		CustomFieldTemplateDto cftDto = (CustomFieldTemplateDto) dto;
	        		if(cftDto.getAppliesTo() == null) {
	        			return result;
	        		}
	        		CustomFieldTemplateDto cft = customFieldTemplateApi.findIgnoreNotFound(cftDto.getCode(), cftDto.getAppliesTo());
					if (cft != null) {
						switch (onDuplicate) {
						case OVERWRITE:
			            	result.incrNbOverwritten();
			            	break;
						case SKIP:
							result.setNbSkipped(1);
							skipped = true;
							break;
						case FAIL:
							throw new EntityAlreadyExistsException(CustomFieldTemplate.class, cft.getCode());
						default:
							break;
						}
					} else {
		            	result.incrNbAdded();
					}

		            if(!skipped) {
			            customFieldTemplateApi.createOrUpdate((CustomFieldTemplateDto) dto, null);
			            result.addItem(moduleItemDto);
		            }

		            moduleItem = new MeveoModuleItem(((CustomFieldTemplateDto) dto).getCode(), CustomFieldTemplate.class.getName(),
			                ((CustomFieldTemplateDto) dto).getAppliesTo(), null);
		            meveoModuleService.addModuleItem(moduleItem, meveoModule);

		        } else if (dto instanceof EntityCustomActionDto) {
					EntityCustomActionDto ecaDto = (EntityCustomActionDto) dto;
					EntityCustomActionDto eca = entityCustomActionApi.findIgnoreNotFound(ecaDto.getCode(), ecaDto.getAppliesTo());
					if (eca != null) {
						switch (onDuplicate) {
						case OVERWRITE:
							result.incrNbOverwritten();
							break;
						case SKIP:
							result.setNbSkipped(1);
							skipped = true;
							break;
						case FAIL:
							throw new EntityAlreadyExistsException(EntityCustomAction.class, eca.getCode());
						default:
							break;
						}
					} else {
						result.incrNbAdded();
					}


		            if(!skipped) {
			            result.addItem(moduleItemDto);
			            entityCustomActionApi.createOrUpdate((EntityCustomActionDto) dto, null);
		            }

		            moduleItem = new MeveoModuleItem(((EntityCustomActionDto) dto).getCode(), EntityCustomAction.class.getName(), ((EntityCustomActionDto) dto).getAppliesTo(), null);
		            meveoModuleService.addModuleItem(moduleItem, meveoModule);
		        } else {

					String moduleItemName = dto.getClass().getSimpleName().substring(0, dto.getClass().getSimpleName().lastIndexOf("Dto"));

					if (checkCetDoesNotExists(moduleItemName, customEntityTemplate)) {
						Class<?> entityClass = MODULE_ITEM_TYPES.get(moduleItemName);

						if (entityClass == null) {
							throw new IllegalArgumentException(moduleItemName + " is not a module item");
						}

						log.info("Installing item {} of module with code={}", dto, meveoModule.getCode());

						Object item = findItem(dto, entityClass);
						if (item != null) {
							switch (onDuplicate) {
								case OVERWRITE:
									result.incrNbOverwritten();
									break;
								case SKIP:
									result.setNbSkipped(1);
									skipped = true;
									break;
								case FAIL: {
									throw new EntityAlreadyExistsException(String.valueOf(item));
								}
								default:
									break;
							}
						} else {
							result.incrNbAdded();
						}

						if (!skipped) {
							createOrUpdateItem(dto, entityClass);
							result.addItem(moduleItemDto);
						}

						DatePeriod validity = null;
						if (ReflectionUtils.hasField(dto, "validFrom")) {
							validity = new DatePeriod((Date) FieldUtils.readField(dto, "validFrom", true), (Date) FieldUtils.readField(dto, "validTo", true));
						}

						if (ReflectionUtils.hasField(dto, "appliesTo")) {
							moduleItem = new MeveoModuleItem((String) FieldUtils.readField(dto, "code", true), entityClass.getName(),
									(String) FieldUtils.readField(dto, "appliesTo", true), validity);

						} else {
							moduleItem = new MeveoModuleItem((String) FieldUtils.readField(dto, "code", true), entityClass.getName(), null, validity);
						}

						//add cft of cet
						if (dto instanceof CustomEntityTemplateDto ) {
							// check and add if cft exists to moduleItem
							addCftToModuleItem((CustomEntityTemplateDto) dto, meveoModule);
						} else if ( dto instanceof CustomRelationshipTemplateDto) {
								// check and add if cft exists to moduleItem
							addCftToModuleItem((CustomRelationshipTemplateDto) dto, meveoModule);
						}

						meveoModuleService.addModuleItem(moduleItem, meveoModule);
						if (skipped) {
							meveoModuleService.loadModuleItem(moduleItem);
							BaseCrudApi api = (BaseCrudApi) ApiUtils.getApiService(entityClass, true);
							api.getPersistenceService().enable(moduleItem.getItemEntity());
						}
					}
				}

		        log.info("Item {} installed", dto);

		    } catch (IllegalAccessException e) {
		        log.error("Failed to access field value in DTO {}", dto, e);
		        throw new MeveoApiException("Failed to access field value in DTO: " + e.getMessage());

		    } catch (MeveoApiException | BusinessException e) {
		        log.error("Failed to transform DTO into a module item. DTO {}", dto, e);
		        throw e;
		    }

		} catch (ClassNotFoundException e1) {
			throw new BusinessException(e1);
		} catch (Exception e) {
			throw e;
		}
		
		return result;
	}

	private boolean checkCetDoesNotExists(String moduleItem, CustomEntityTemplate customEntityTemplate) {
    	if (moduleItem.equals("CustomEntityInstance") && customEntityTemplate == null) {
    		return false;
		}
		return true;
	}

	/**
	 * @param dto
	 * @param entityClass
	 * @throws MeveoApiException
	 * @throws BusinessException
	 */
	private void createOrUpdateItem(BaseEntityDto dto, Class<?> entityClass) throws MeveoApiException, BusinessException {
		if (entityClass.isAnnotationPresent(VersionedEntity.class)) {
		    ApiVersionedService apiService = ApiUtils.getApiVersionedService(entityClass, true);
		    apiService.createOrUpdate(dto);
		    
		} else {
		    ApiService apiService = ApiUtils.getApiService(entityClass, true);
		    apiService.createOrUpdate(dto);
		}
	}
	
	private <T> T findItem(BaseEntityDto dto, Class<T> entityClass) throws MeveoApiException, BusinessException {
		
		if (entityClass.isAnnotationPresent(VersionedEntity.class)) {
		    ApiVersionedService apiService = ApiUtils.getApiVersionedService(entityClass, true);
		    return (T) apiService.findIgnoreNotFound(dto.getCode(), null, null);
		    
		} else {
		    ApiService apiService = ApiUtils.getApiService(entityClass, true);
		    return (T) apiService.findIgnoreNotFound(dto.getCode());
		}
	}
	
	public List<MeveoModuleItemDto> getSortedModuleItems(List<MeveoModuleItemDto> moduleItems) {

		Comparator<MeveoModuleItemDto> comparator = new Comparator<MeveoModuleItemDto>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(MeveoModuleItemDto o1, MeveoModuleItemDto o2) {
				String m1;
				String m2;
				try {
					var dtoClass1 = Class.forName(o1.getDtoClassName());
					var dtoClass2 = Class.forName(o2.getDtoClassName());

					m1 = ModuleUtil.getModuleItemName(dtoClass1);
					m2 = ModuleUtil.getModuleItemName(dtoClass2);

					Class<?> entityClass1 = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(m1);
					if(entityClass1 == null) {
						log.error("Can't get module item type for {}", m1);
						return 0;
					}
					
					Class<?> entityClass2 = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(m2);
					if(entityClass2 == null) {
						log.error("Can't get module item type for {}", m2);
					}
					
					// Both items are same type and we know how to compare them
					if(dtoClass1.equals(dtoClass2) && Comparable.class.isAssignableFrom(dtoClass1)) {
						Comparable<Object> dto1 = (Comparable<Object>) JacksonUtil.convert(o1.getDtoData(), dtoClass1);
						Comparable<Object> dto2 = (Comparable<Object>) JacksonUtil.convert(o2.getDtoData(), dtoClass2);

						return dto1.compareTo(dto2);
					}
					
					ModuleItemOrder sortOrder1 = entityClass1.getAnnotation(ModuleItemOrder.class);
					ModuleItemOrder sortOrder2 = entityClass2.getAnnotation(ModuleItemOrder.class);

					if(sortOrder1 == null || sortOrder2 == null) {
						log.warn("Can't sort module items {} and {} because @ModuleItemOrder is not present on entity class", o1, o2);
						return 0;
					}
					
					return sortOrder1.value() - sortOrder2.value();

				} catch (ClassNotFoundException e) {
					return 0;
				}
			}
		};

		moduleItems.sort(comparator);

		return moduleItems;
	}
	
    private void unpackAndInstallModuleItems(ModuleInstallResult result, MeveoModule meveoModule, MeveoModuleDto moduleDto, OnDuplicate onDuplicate) throws MeveoApiException, BusinessException {
    	if (moduleDto.getModuleItems() != null) {
            meveoModule.getModuleItems().clear();
            
            Collections.sort(moduleDto.getModuleItems());
            /* To avoid conflict we should first create CET, then their fields, so we need to separate them and sort them */
			for (MeveoModuleItemDto moduleItemDto : new ArrayList<>(moduleDto.getModuleItems())) {
				if (moduleItemDto.getDtoClassName().equals(CustomEntityTemplateDto.class.getName())) {
					CustomEntityTemplateDto cet = JacksonUtil.convert(moduleItemDto.getDtoData(), CustomEntityTemplateDto.class);
					if (cet.getFields() != null) {
						for (CustomFieldTemplateDto cftData : new ArrayList<>(cet.getFields())) {
							MeveoModuleItemDto cftModuleItem = new MeveoModuleItemDto();
							cftModuleItem.setDtoClassName(CustomFieldTemplateDto.class.getName());
							cftModuleItem.setDtoData(cftData);
							cftData.setAppliesTo("CE_" + cet.getCode());
							moduleDto.getModuleItems().add(cftModuleItem);
	
							cet.getFields().remove(cftData);
							moduleItemDto.setDtoData(cet);
						}
					}
					
					// Also put entity custom action outside of the cet
					if (cet.getActions() != null) {
						for (var actionDto : List.copyOf(cet.getActions())) {
							MeveoModuleItemDto cftModuleItem = new MeveoModuleItemDto();
							cftModuleItem.setDtoClassName(EntityCustomActionDto.class.getName());
							cftModuleItem.setDtoData(actionDto);
							actionDto.setAppliesTo("CE_" + cet.getCode());
							moduleDto.getModuleItems().add(cftModuleItem);

							cet.getActions().remove(actionDto);
							moduleItemDto.setDtoData(cet);
						}
					}

					if (!StringUtils.isBlank(cet.getCrudEventListenerScript())) {
						cet.setTransientCrudEventListenerScript(cet.getCrudEventListenerScript());
						cet.setCrudEventListenerScript(null);
						moduleItemDto.setDtoData(cet);
					}
				}
				if (moduleItemDto.getDtoClassName().equals(CustomRelationshipTemplateDto.class.getName())) {
					CustomRelationshipTemplateDto cet = JacksonUtil.convert(moduleItemDto.getDtoData(), CustomRelationshipTemplateDto.class);
					for (CustomFieldTemplateDto cftData : new ArrayList<>(cet.getFields())) {
						MeveoModuleItemDto cftModuleItem = new MeveoModuleItemDto();
						cftModuleItem.setDtoClassName(CustomFieldTemplateDto.class.getName());
						cftModuleItem.setDtoData(cftData);
						cftData.setAppliesTo("CRT_" + cet.getCode());
						moduleDto.getModuleItems().add(cftModuleItem);

						cet.getFields().remove(cftData);
						moduleItemDto.setDtoData(cet);
					}
					
				}
			}

			// we need to sort the module items because of dependency hierarchy
			// each item is annotated with @ModuleItemSort
			List<MeveoModuleItemDto> sortedModuleItems = getSortedModuleItems(moduleDto.getModuleItems());
			
			for (MeveoModuleItemDto moduleItemDto : sortedModuleItems) {
				try {
					 var subResult = meveoModuleItemInstaller.unpackAndInstallModuleItem(meveoModule, moduleItemDto, onDuplicate);
					result.merge(subResult);
				} catch (Exception e) {
					if (e instanceof EJBException) {
						throw new BusinessException(e.getCause());
					}

					throw new BusinessException(e);
				}
			}

			for (MeveoModuleItemDto moduleItemDto : sortedModuleItems) {
				if (moduleItemDto.getDtoClassName().equals(CustomEntityTemplateDto.class.getName())) {
					try {
						Class<? extends BaseEntityDto> dtoClass = (Class<? extends BaseEntityDto>) Class.forName(moduleItemDto.getDtoClassName());
						CustomEntityTemplateDto cetDto = (CustomEntityTemplateDto) JacksonUtil.convert(moduleItemDto.getDtoData(), dtoClass);
					if (!StringUtils.isBlank(cetDto.getTransientCrudEventListenerScript())) {
						CustomEntityTemplate cet = customEntityTemplateService.findByCode(cetDto.getCode());
						ScriptInstance si = scriptInstanceService.findByCode(cetDto.getTransientCrudEventListenerScript());
						if (si != null) {
							cet.setCrudEventListenerScript(si);
							customEntityTemplateService.update(cet);
						}
					}
					} catch (ClassNotFoundException e) {
						log.error("Cannot find dto class", e);
					}
				}
			}
        }
    	
    }
	
	/**
	 * Add cft which is a field of cet as a module item.
	 * 
	 * @param dto         CustomEntityTemplateDto instance
	 * @param meveoModule where module item is added
	 * @throws BusinessException 
	 */
	private void addCftToModuleItem(CustomEntityTemplateDto dto, MeveoModule meveoModule) throws BusinessException {
		if (dto.getFields() != null && !dto.getFields().isEmpty()) {
			for (CustomFieldTemplateDto cftDto : dto.getFields()) {
				MeveoModuleItem itemDto = new MeveoModuleItem(cftDto.getCode(), CustomFieldTemplate.class.getName(), CustomEntityTemplate.CFT_PREFIX + "_" + dto.getCode(), null);
				try {
					meveoModuleService.addModuleItem(itemDto, meveoModule);
				} catch (BusinessException e) {
					throw new BusinessException(e.getCause());
				}
			}
		}
	}
	
	private void addCftToModuleItem(CustomRelationshipTemplateDto dto, MeveoModule meveoModule) throws BusinessException {
		if (dto.getFields() != null && !dto.getFields().isEmpty()) {
			for (CustomFieldTemplateDto cftDto : dto.getFields()) {
				MeveoModuleItem itemDto = new MeveoModuleItem(cftDto.getCode(), CustomFieldTemplate.class.getName(), CustomRelationshipTemplate.CRT_PREFIX + "_" + dto.getCode(), null);
				meveoModuleService.addModuleItem(itemDto, meveoModule);
			}
		}
	}


}