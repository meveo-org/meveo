package org.meveo.api.module;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import org.apache.commons.lang.reflect.FieldUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.ApiService;
import org.meveo.api.ApiUtils;
import org.meveo.api.ApiVersionedService;
import org.meveo.api.CustomFieldTemplateApi;
import org.meveo.api.EntityCustomActionApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.EntityCustomActionDto;
import org.meveo.api.dto.catalog.BusinessServiceModelDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.DatePeriod;
import org.meveo.model.VersionedEntity;
import org.meveo.model.catalog.BusinessServiceModel;
import org.meveo.model.catalog.ServiceTemplate;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.catalog.impl.ServiceTemplateService;
import org.meveo.service.script.module.ModuleScriptInterface;
import org.meveo.service.script.module.ModuleScriptService;
import org.slf4j.Logger;

@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
public class MeveoModuleItemInstaller {
	
    protected static final ConcurrentHashMap<String, Class<?>> MODULE_ITEM_TYPES = new ConcurrentHashMap<>();
	
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
    private ServiceTemplateService serviceTemplateService;
    
    @Resource
    private UserTransaction transaction;
    
    public MeveoModule install(MeveoModule meveoModule, MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {

        boolean installed = false;
        if (!meveoModule.isDownloaded()) {
            throw new ActionForbiddenException(meveoModule.getClass(), moduleDto.getCode(), "install", "Module with the same code is being developped locally, can not overwrite it.");
        }

        if (meveoModule.isInstalled()) {
            // throw new ActionForbiddenException(meveoModule.getClass(), moduleDto.getCode(), "install", "Module is already installed");
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
	
	            unpackAndInstallModuleItems(meveoModule, moduleDto);
	
	            meveoModule.setInstalled(true);
	            
	            transaction.begin();
	            meveoModule = meveoModuleService.update(meveoModule);
	            transaction.commit();
	
	            if (moduleScript != null) {
	                moduleScriptService.postInstallModule(moduleScript, meveoModule);
	            }
            
        	} catch(Exception e) {
            	this.meveoModuleService.uninstall(meveoModule);
            	throw new BusinessException(e);
            }

        }
        

        return meveoModule;
    }
    
    private void unpackAndInstallBSMItems(BusinessServiceModel bsm, BusinessServiceModelDto bsmDto) {
        ServiceTemplate serviceTemplate = serviceTemplateService.findByCode(bsmDto.getServiceTemplate().getCode());
        bsm.setServiceTemplate(serviceTemplate);
    }
	
	/**
	 * @param meveoModule
	 * @param moduleItemDto
	 * @throws IllegalArgumentException
	 * @throws MeveoApiException
	 * @throws Exception
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void unpackAndInstallModuleItem(MeveoModule meveoModule, MeveoModuleItemDto moduleItemDto) throws IllegalArgumentException, MeveoApiException, Exception, BusinessException {
		Class<? extends BaseEntityDto> dtoClass;
		
		try {
			transaction.begin();
			dtoClass = (Class<? extends BaseEntityDto>) Class.forName(moduleItemDto.getDtoClassName());
			BaseEntityDto dto = JacksonUtil.convert(moduleItemDto.getDtoData(), dtoClass);
		
		    try {

		        if (dto instanceof MeveoModuleDto) {
		        	MeveoModule subModule = meveoModuleService.findByCode(((MeveoModuleDto) dto).getCode());
		            install(subModule, (MeveoModuleDto) dto);

		            Class<? extends MeveoModule> moduleClazz = MeveoModule.class;
		            meveoModule.addModuleItem(new MeveoModuleItem(((MeveoModuleDto) dto).getCode(), moduleClazz.getName(), null, null));

		        } else if (dto instanceof CustomFieldTemplateDto) {
		            customFieldTemplateApi.createOrUpdate((CustomFieldTemplateDto) dto, null);
		            meveoModule.addModuleItem(new MeveoModuleItem(((CustomFieldTemplateDto) dto).getCode(), CustomFieldTemplate.class.getName(),
		                ((CustomFieldTemplateDto) dto).getAppliesTo(), null));

		        } else if (dto instanceof EntityCustomActionDto) {
		            entityCustomActionApi.createOrUpdate((EntityCustomActionDto) dto, null);
		            meveoModule.addModuleItem(
		                new MeveoModuleItem(((EntityCustomActionDto) dto).getCode(), EntityCustomAction.class.getName(), ((EntityCustomActionDto) dto).getAppliesTo(), null));

		        } else {

		            String moduleItemName = dto.getClass().getSimpleName().substring(0, dto.getClass().getSimpleName().lastIndexOf("Dto"));
		            
//	                        MODULE_ITEM_TYPES.values().stream()
//	                        	.filter(entityClass::equals)
//	                        	.findFirst()
//	                        	.orElseThrow(() -> );
		            
		            Class<?> entityClass = MODULE_ITEM_TYPES.get(moduleItemName);
		            
		            if(entityClass == null) {
		            	throw new IllegalArgumentException(moduleItemName + " is not a module item" );
		            }
		            
		            log.info("Installing item {} of module {}", dto, meveoModule);
		            	
		            if (entityClass.isAnnotationPresent(VersionedEntity.class)) {
		                ApiVersionedService apiService = ApiUtils.getApiVersionedService(entityClass, true);
		                apiService.createOrUpdate(dto);
		                
		            } else {
		                ApiService apiService = ApiUtils.getApiService(entityClass, true);
		                apiService.createOrUpdate(dto);
		            }
		            
		            DatePeriod validity = null;
		            if (ReflectionUtils.hasField(dto, "validFrom")) {
		                validity = new DatePeriod((Date) FieldUtils.readField(dto, "validFrom", true), (Date) FieldUtils.readField(dto, "validTo", true));
		            }

		            if (ReflectionUtils.hasField(dto, "appliesTo")) {
		                meveoModule.addModuleItem(new MeveoModuleItem((String) FieldUtils.readField(dto, "code", true), entityClass.getName(),
		                    (String) FieldUtils.readField(dto, "appliesTo", true), validity));
		            
		            } else {
		                meveoModule.addModuleItem(new MeveoModuleItem((String) FieldUtils.readField(dto, "code", true), entityClass.getName(), null, validity));
		            }
		            
		            //add cft of cet
					if (dto instanceof CustomEntityTemplateDto) {
						// check and add if cft exists to moduleItem
						addCftToModuleItem((CustomEntityTemplateDto) dto, meveoModule);
					}
		        }

		    } catch (IllegalAccessException e) {
		        log.error("Failed to access field value in DTO {}", dto, e);
		        throw new MeveoApiException("Failed to access field value in DTO: " + e.getMessage());

		    } catch (MeveoApiException | BusinessException e) {
		        log.error("Failed to transform DTO into a module item. DTO {}", dto, e);
		        throw e;
		    }
		    
		    transaction.commit();
		    
		} catch (ClassNotFoundException e1) {
			throw new BusinessException(e1);
		}
	}
	
    private void unpackAndInstallModuleItems(MeveoModule meveoModule, MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {
        if (moduleDto.getModuleItems() != null) {
            meveoModule.getModuleItems().clear();
            for (MeveoModuleItemDto moduleItemDto : moduleDto.getModuleItems()) {
            	try {
					unpackAndInstallModuleItem(meveoModule, moduleItemDto);
				} catch (Exception e) {
					if(e instanceof EJBException) {
						throw new BusinessException(((EJBException) e).getCausedByException());
					}
					
					throw new BusinessException(e);
				}
            }
        }

        // Converting subclasses of MeveoModuleDto class
        if (moduleDto instanceof BusinessServiceModelDto) {
            unpackAndInstallBSMItems((BusinessServiceModel) meveoModule, (BusinessServiceModelDto) moduleDto);
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
