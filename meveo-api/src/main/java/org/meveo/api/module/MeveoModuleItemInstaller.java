package org.meveo.api.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.reflect.FieldUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ModuleUtil;
import org.meveo.api.ApiService;
import org.meveo.api.ApiUtils;
import org.meveo.api.ApiVersionedService;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.CustomFieldTemplateApi;
import org.meveo.api.EntityCustomActionApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
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
import org.meveo.model.BusinessEntity;
import org.meveo.model.DatePeriod;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.VersionedEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.script.ConcreteFunctionService;
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
//@TransactionManagement(TransactionManagementType.BEAN)
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
    
    @EJB
    private MeveoModuleItemInstaller meveoModuleItemInstaller;
    
    @Inject
    private ConcreteFunctionService concreteFunctionService;
    
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
            
            meveoModuleService.loadModuleItem(item);
        }
        
        for (MeveoModuleItem item : moduleItems) {
            BusinessEntity itemEntity = item.getItemEntity();
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
                            meveoModuleService.getEntityManager().createNamedQuery("Endpoint.deleteByService")
                                    .setParameter("serviceId", service.getId())
                                    .executeUpdate();
                            
                        } else {
                        	log.info("Uninstalling module item {}", item);
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
                throw new BusinessException("Failed to uninstall/disable module item " + item ,e);
            }
        }

        if (moduleScript != null) {
            moduleScriptService.postUninstallModule(moduleScript, module);
        }

        // Remove if it is a child module
        if (childModule) {
        	meveoModuleService.remove(module);
            return null;

        // Otherwise mark it uninstalled and clear module items
        } else {
            MeveoModule moduleUpdated = module;
            module.setInstalled(false);
            
            /* In case the module is uninstalled because of installation failure
               and that the module is not inserted in db we should not update its persistent state */
            module = meveoModuleService.reattach(module);
            if(meveoModuleService.getEntityManager().contains(module)) {
            	moduleUpdated = meveoModuleService.update(module);
            }
            
            meveoModuleService.getEntityManager().createNamedQuery("MeveoModuleItem.deleteByModule")
            	.setParameter("meveoModule", module)
            	.executeUpdate();
            
			return moduleUpdated;
        }
    }
    
    public ModuleInstallResult install(MeveoModule meveoModule, MeveoModuleDto moduleDto, OnDuplicate onDuplicate) throws MeveoApiException, BusinessException {
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
		
		    try {

		        if (dto instanceof MeveoModuleDto) {
		        	MeveoModule subModule = meveoModuleService.findByCode(((MeveoModuleDto) dto).getCode());
		        	result = install(subModule, (MeveoModuleDto) dto, onDuplicate);

		            Class<? extends MeveoModule> moduleClazz = MeveoModule.class;
		            moduleItem = new MeveoModuleItem(((MeveoModuleDto) dto).getCode(), moduleClazz.getName(), null, null);
		        	meveoModule.addModuleItem(moduleItem);

		        } else if (dto instanceof CustomFieldTemplateDto) {
	        		CustomFieldTemplateDto cftDto = (CustomFieldTemplateDto) dto;
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
		        	meveoModule.addModuleItem(moduleItem);

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
		        	meveoModule.addModuleItem(moduleItem);
		        } else {

		            String moduleItemName = dto.getClass().getSimpleName().substring(0, dto.getClass().getSimpleName().lastIndexOf("Dto"));
		            	            
		            Class<?> entityClass = MODULE_ITEM_TYPES.get(moduleItemName);
		            
		            if(entityClass == null) {
		            	throw new IllegalArgumentException(moduleItemName + " is not a module item" );
		            }
		            
		            log.info("Installing item {} of module {}", dto, meveoModule);
		            	
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
					
					if(!skipped) {
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
					if (dto instanceof CustomEntityTemplateDto) {
						// check and add if cft exists to moduleItem
						addCftToModuleItem((CustomEntityTemplateDto) dto, meveoModule);
					}
					
		        	meveoModule.addModuleItem(moduleItem);
		            if(skipped) {
		            	meveoModuleService.loadModuleItem(moduleItem);
		            	BaseCrudApi api = (BaseCrudApi) ApiUtils.getApiService(entityClass, true);
		            	api.getPersistenceService().enable(moduleItem.getItemEntity());
		            }
		        }
		        


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
					for (CustomFieldTemplateDto cftData : new ArrayList<>(cet.getFields())) {
						MeveoModuleItemDto cftModuleItem = new MeveoModuleItemDto();
						cftModuleItem.setDtoClassName(CustomFieldTemplateDto.class.getName());
						cftModuleItem.setDtoData(cftData);
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
					// var subResult = meveoModuleItemInstaller.unpackAndInstallModuleItem(meveoModule, moduleItemDto, onDuplicate);
					var subResult = unpackAndInstallModuleItem(meveoModule, moduleItemDto, onDuplicate);
					result.merge(subResult);
				} catch (Exception e) {
					if (e instanceof EJBException) {
						throw new BusinessException(e.getCause());
					}

					throw new BusinessException(e);
				}
			}
			
        }
    	
    }
	
	/**
	 * Add cft which is a field of cet as a module item.
	 * 
	 * @param dto         CustomEntityTemplateDto instance
	 * @param meveoModule where module item is added
	 */
	private void addCftToModuleItem(CustomEntityTemplateDto dto, MeveoModule meveoModule) {
		if (dto.getFields() != null && !dto.getFields().isEmpty()) {
			for (CustomFieldTemplateDto cftDto : dto.getFields()) {
				MeveoModuleItem itemDto = new MeveoModuleItem(cftDto.getCode(), CustomFieldTemplate.class.getName(), CustomEntityTemplate.CFT_PREFIX + "_" + dto.getCode(), null);
				meveoModule.addModuleItem(itemDto);
			}
		}
	}

}
