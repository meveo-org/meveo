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

import org.apache.commons.lang.reflect.FieldUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ModuleUtil;
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
import org.meveo.model.ModuleItemOrder;
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
    
    @Inject
    private ServiceTemplateService serviceTemplateService;
    
    @EJB
    private MeveoModuleItemInstaller meveoModuleItemInstaller;
    
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
	
	            if (moduleScript != null) {
	                moduleScriptService.postInstallModule(moduleScript, meveoModule);
	            }
            
        	} catch(Exception e) {
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
		    
		} catch (ClassNotFoundException e1) {
			throw new BusinessException(e1);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public List<MeveoModuleItemDto> getSortedModuleItems(List<MeveoModuleItemDto> moduleItems) {

		Comparator<MeveoModuleItemDto> comparator = new Comparator<MeveoModuleItemDto>() {

			@Override
			public int compare(MeveoModuleItemDto o1, MeveoModuleItemDto o2) {
				String m1;
				String m2;
				try {
					m1 = ModuleUtil.getModuleItemName(Class.forName(o1.getDtoClassName()));
					m2 = ModuleUtil.getModuleItemName(Class.forName(o2.getDtoClassName()));

					Class<?> entityClass1 = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(m1);
					Class<?> entityClass2 = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(m2);

					ModuleItemOrder sortOrder1 = entityClass1.getAnnotation(ModuleItemOrder.class);
					ModuleItemOrder sortOrder2 = entityClass2.getAnnotation(ModuleItemOrder.class);

					return sortOrder1.value() - sortOrder2.value();

				} catch (ClassNotFoundException e) {
					return 0;
				}
			}
		};

		moduleItems.sort(comparator);

		return moduleItems;
	}
	
    private void unpackAndInstallModuleItems(MeveoModule meveoModule, MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {
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
					meveoModuleItemInstaller.unpackAndInstallModuleItem(meveoModule, moduleItemDto);
				} catch (Exception e) {
					if (e instanceof EJBException) {
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
