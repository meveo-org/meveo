package org.meveo.api.module;

import org.apache.commons.lang.reflect.FieldUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ModuleUtil;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.*;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.EntityCustomActionDto;
import org.meveo.api.dto.catalog.BusinessServiceModelDto;
import org.meveo.api.dto.catalog.ServiceTemplateDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.exception.*;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.DatePeriod;
import org.meveo.model.ModuleItem;
import org.meveo.model.VersionedEntity;
import org.meveo.model.catalog.BusinessServiceModel;
import org.meveo.model.catalog.ServiceTemplate;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.catalog.impl.ServiceTemplateService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.module.ModuleScriptInterface;
import org.meveo.service.script.module.ModuleScriptService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Clément Bareth
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Wassim Drira
 * @lastModifiedVersion 6.3.0
 * 
 **/
@Stateless
public class MeveoModuleApi extends BaseCrudApi<MeveoModule, MeveoModuleDto> {

    @Inject
    private MeveoModuleService meveoModuleService;

    @Inject
    private CustomFieldTemplateApi customFieldTemplateApi;

    @Inject
    private EntityCustomActionApi entityCustomActionApi;

    @Inject
    private ScriptInstanceApi scriptInstanceApi;

    @Inject
    private ServiceTemplateService serviceTemplateService;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    private ModuleScriptService moduleScriptService;

    private static JAXBContext jaxbCxt;
    
    static {
        try {
            jaxbCxt = JAXBContext.newInstance(MeveoModuleDto.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
    
    public MeveoModuleApi() {
    	super(MeveoModule.class, MeveoModuleDto.class);
    }

    public MeveoModule create(MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(moduleDto.getCode())) {
            missingParameters.add("code");
        }
        if (StringUtils.isBlank(moduleDto.getDescription())) {
            missingParameters.add("description");
        }
        if (StringUtils.isBlank(moduleDto.getLicense())) {
            missingParameters.add("license");
        }

        if (moduleDto.getScript() != null) {
            // If script was passed code is needed if script source was not passed.
            if (StringUtils.isBlank(moduleDto.getScript().getCode()) && StringUtils.isBlank(moduleDto.getScript().getScript())) {
                missingParameters.add("script.code");

                // Otherwise code is calculated from script source by combining package and classname
            } else if (!StringUtils.isBlank(moduleDto.getScript().getScript())) {
                String fullClassname = ScriptInstanceService.getFullClassname(moduleDto.getScript().getScript());
                if (!StringUtils.isBlank(moduleDto.getScript().getCode()) && !moduleDto.getScript().getCode().equals(fullClassname)) {
                    throw new BusinessApiException("The code and the canonical script class name must be identical");
                }
                moduleDto.getScript().setCode(fullClassname);
            }
        }

        handleMissingParameters();

        if (meveoModuleService.findByCode(moduleDto.getCode()) != null) {
            throw new EntityAlreadyExistsException(MeveoModule.class, moduleDto.getCode());
        }
        MeveoModule meveoModule = new MeveoModule();
        parseModuleInfoOnlyFromDto(meveoModule, moduleDto);
        meveoModuleService.create(meveoModule);
        return meveoModule;
    }

    public MeveoModule update(MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(moduleDto.getCode())) {
            missingParameters.add("module code is null");
        }
        if (StringUtils.isBlank(moduleDto.getDescription())) {
            missingParameters.add("description");
        }
        if (StringUtils.isBlank(moduleDto.getLicense())) {
            missingParameters.add("module license is null");
        }

        if (moduleDto.getScript() != null) {
            // If script was passed code is needed if script source was not passed.
            if (StringUtils.isBlank(moduleDto.getScript().getCode()) && StringUtils.isBlank(moduleDto.getScript().getScript())) {
                missingParameters.add("script.code");

                // Otherwise code is calculated from script source by combining package and classname
            } else if (!StringUtils.isBlank(moduleDto.getScript().getScript())) {
                String fullClassname = ScriptInstanceService.getFullClassname(moduleDto.getScript().getScript());
                if (!StringUtils.isBlank(moduleDto.getScript().getCode()) && !moduleDto.getScript().getCode().equals(fullClassname)) {
                    throw new BusinessApiException("The code and the canonical script class name must be identical");
                }
                moduleDto.getScript().setCode(fullClassname);
            }
        }

        handleMissingParameters();

        MeveoModule meveoModule = meveoModuleService.findByCode(moduleDto.getCode());
        if (meveoModule == null) {
            throw new EntityDoesNotExistsException(MeveoModule.class, moduleDto.getCode());
        }

        if (!meveoModule.isDownloaded()) {
            throw new ActionForbiddenException(meveoModule.getClass(), moduleDto.getCode(), "install",
                "Module with the same code is being developped locally, can not overwrite it.");
        }

        if (meveoModule.getModuleItems() != null) {
            Iterator<MeveoModuleItem> itr = meveoModule.getModuleItems().iterator();
            while (itr.hasNext()) {
                MeveoModuleItem i = itr.next();
                i.setMeveoModule(null);
                itr.remove();
            }
        }
        parseModuleInfoOnlyFromDto(meveoModule, moduleDto);
        meveoModule = meveoModuleService.update(meveoModule);
        return meveoModule;
    }

    public void delete(String code) throws EntityDoesNotExistsException, BusinessException {

        MeveoModule meveoModule = meveoModuleService.findByCode(code);
        if (meveoModule == null) {
            throw new EntityDoesNotExistsException(MeveoModule.class, code);
        }
        String logoPicture = meveoModule.getLogoPicture();
        meveoModuleService.remove(meveoModule);
        removeModulePicture(logoPicture);

    }

    public List<MeveoModuleDto> list(Class<? extends MeveoModule> clazz) throws BusinessException {

        List<MeveoModule> meveoModules;

        if (clazz == null) {
            meveoModules = meveoModuleService.list();

        } else {
            Map<String, Object> filters = new HashMap<>();
            filters.put(PersistenceService.SEARCH_ATTR_TYPE_CLASS, clazz);

            meveoModules = meveoModuleService.list(new PaginationConfiguration(filters));
        }

        List<MeveoModuleDto> result = new ArrayList<>();
        MeveoModuleDto moduleDto;
        for (MeveoModule meveoModule : meveoModules) {
            try {
                moduleDto = moduleToDto(meveoModule);
                result.add(moduleDto);
            } catch (MeveoApiException e) {
                // Dont care, it was logged earlier in moduleToDto()
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.meveo.api.ApiService#find(java.lang.String)
     */
    @Override
    public MeveoModuleDto find(String code) throws MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code [BOM: businessOfferModelCode, BSM: businessServiceModelCode, BAM: businessAccountModelCode]");
            handleMissingParameters();
        }

        MeveoModule meveoModule = meveoModuleService.findByCode(code);
        if (meveoModule == null) {
            throw new EntityDoesNotExistsException(MeveoModule.class, code);
        }
        return moduleToDto(meveoModule);
    }

    @Override
    public MeveoModule createOrUpdate(MeveoModuleDto postData) throws MeveoApiException, BusinessException {
        MeveoModule meveoModule = meveoModuleService.findByCode(postData.getCode());
        if (meveoModule == null) {
            // create
            return create(postData);
        } else {
            // update
            return update(postData);
        }
    }

    public MeveoModule install(MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(moduleDto.getCode())) {
            missingParameters.add("code");
        }

        handleMissingParameters();

        MeveoModule meveoModule = meveoModuleService.findByCode(moduleDto.getCode());
        boolean installed = false;
        if (meveoModule == null) {
            create(moduleDto);
            meveoModule = meveoModuleService.findByCode(moduleDto.getCode());

        } else {
            if (!meveoModule.isDownloaded()) {
                throw new ActionForbiddenException(meveoModule.getClass(), moduleDto.getCode(), "install", "Module with the same code is being developped locally, can not overwrite it.");
            }

            if (meveoModule.isInstalled()) {
                // throw new ActionForbiddenException(meveoModule.getClass(), moduleDto.getCode(), "install", "Module is already installed");
                installed = true;

            } else {
                try {
                    moduleDto = MeveoModuleUtils.moduleSourceToDto(meveoModule);
                } catch (JAXBException e) {
                    log.error("Failed to parse module {} source", meveoModule.getCode(), e);
                    throw new BusinessException("Failed to parse module source", e);
                }
            }
        }

        if (!installed) {
            ModuleScriptInterface moduleScript = null;
            if (meveoModule.getScript() != null) {
                moduleScript = moduleScriptService.preInstallModule(meveoModule.getScript().getCode(), meveoModule);
            }

            unpackAndInstallModuleItems(meveoModule, moduleDto);

            meveoModule.setInstalled(true);
            meveoModule = meveoModuleService.update(meveoModule);

            if (moduleScript != null) {
                moduleScriptService.postInstallModule(moduleScript, meveoModule);
            }
        }

        return meveoModule;
    }

    public void uninstall(String code, Class<? extends MeveoModule> moduleClass) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }

        if (moduleClass == null) {
            moduleClass = MeveoModule.class;
        }

        MeveoModule meveoModule = meveoModuleService.findByCode(code);
        if (meveoModule == null) {
            throw new EntityDoesNotExistsException(moduleClass, code);
        }

        if (!meveoModule.isInstalled()) {
            throw new ActionForbiddenException(meveoModule.getClass(), code, "uninstall", "Module is not installed or already enabled");
        }
        meveoModuleService.uninstall(meveoModule);
    }


    private void parseModuleInfoOnlyFromDtoBSM(BusinessServiceModel bsm, BusinessServiceModelDto bsmDto) {

        bsm.setDuplicatePricePlan(bsmDto.isDuplicatePricePlan());
        bsm.setDuplicateService(bsmDto.isDuplicateService());
    }

    private void unpackAndInstallBSMItems(BusinessServiceModel bsm, BusinessServiceModelDto bsmDto) {
        ServiceTemplate serviceTemplate = serviceTemplateService.findByCode(bsmDto.getServiceTemplate().getCode());
        bsm.setServiceTemplate(serviceTemplate);
    }

    public void parseModuleInfoOnlyFromDto(MeveoModule meveoModule, MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {
        meveoModule.setCode(StringUtils.isBlank(moduleDto.getUpdatedCode()) ? moduleDto.getCode() : moduleDto.getUpdatedCode());
        meveoModule.setDescription(moduleDto.getDescription());
        meveoModule.setLicense(moduleDto.getLicense());
        meveoModule.setLogoPicture(moduleDto.getLogoPicture());
        if (!StringUtils.isBlank(moduleDto.getLogoPicture()) && moduleDto.getLogoPictureFile() != null) {
            writeModulePicture(moduleDto.getLogoPicture(), moduleDto.getLogoPictureFile());
        }
        if (meveoModule.isTransient()) {
            meveoModule.setInstalled(false);
        }

        // Converting subclasses of MeveoModuleDto class
        if (moduleDto instanceof BusinessServiceModelDto) {
            parseModuleInfoOnlyFromDtoBSM((BusinessServiceModel) meveoModule, (BusinessServiceModelDto) moduleDto);

        }

        // Extract module script used for installation and module activation
        ScriptInstance scriptInstance = null;
        // Should create it or update script only if it has full information only
        if (moduleDto.getScript() != null) {
            if (!moduleDto.getScript().isCodeOnly()) {
                scriptInstanceApi.createOrUpdate(moduleDto.getScript());
            }

            scriptInstance = scriptInstanceService.findByCode(moduleDto.getScript().getCode());
            if (scriptInstance == null) {
                throw new EntityDoesNotExistsException(ScriptInstance.class, moduleDto.getScript().getCode());
            }
        }
        meveoModule.setScript(scriptInstance);

        // Store module DTO into DB to be used later for installation
        meveoModule.setModuleSource(JacksonUtil.toString(moduleDto));
    }

    @SuppressWarnings({ "rawtypes"})
    private void unpackAndInstallModuleItems(MeveoModule meveoModule, MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {

        if (moduleDto.getModuleItems() != null) {

            meveoModule.getModuleItems().clear();

            for (MeveoModuleItemDto moduleItemDto : moduleDto.getModuleItems()) {
            	
            	Class<? extends BaseEntityDto> dtoClass;
				try {
					dtoClass = (Class<? extends BaseEntityDto>) Class.forName(moduleItemDto.getDtoClassName());
					BaseEntityDto dto = JacksonUtil.convert(moduleItemDto.getDtoData(), dtoClass);
            	
	                try {
	
	
	                    if (dto instanceof MeveoModuleDto) {
	                        install((MeveoModuleDto) dto);
	
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
	
	                        String entityClassName = dto.getClass().getSimpleName().substring(0, dto.getClass().getSimpleName().lastIndexOf("Dto"));
	                        Class<?> entityClass = ReflectionUtils.getClassBySimpleNameAndAnnotation(entityClassName, ModuleItem.class, "");
	                        if (entityClass == null) {
	                            throw new RuntimeException("No entity class or @ModuleItem annotation found for " + entityClassName);
	                        }
	
	                        if (entityClass.isAnnotationPresent(VersionedEntity.class)) {
	                            ApiVersionedService apiService = getApiVersionedService(entityClass, true);
	                            apiService.createOrUpdate(dto);
	                        } else {
	                            ApiService apiService = getApiService(entityClass, true);
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
				}
            }

        }

        // Converting subclasses of MeveoModuleDto class
        if (moduleDto instanceof BusinessServiceModelDto) {
            unpackAndInstallBSMItems((BusinessServiceModel) meveoModule, (BusinessServiceModelDto) moduleDto);

        }
    }

    private void writeModulePicture(String filename, byte[] fileData) {
        try {
            ModuleUtil.writeModulePicture(currentUser.getProviderCode(), filename, fileData);
        } catch (Exception e) {
            log.error("error when export module picture {}, info {}", filename, e.getMessage(), e);
        }
    }

    private void removeModulePicture(String filename) {
        try {
            ModuleUtil.removeModulePicture(currentUser.getProviderCode(), filename);
        } catch (Exception e) {
            log.error("error when delete module picture {}, info {}", filename, (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
        }
    }

    public void enable(String code, Class<? extends MeveoModule> moduleClass) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }

        if (moduleClass == null) {
            moduleClass = MeveoModule.class;
        }

        MeveoModule meveoModule = meveoModuleService.findByCode(code);
        if (meveoModule == null) {
            throw new EntityDoesNotExistsException(moduleClass, code);
        }

        if (!meveoModule.isInstalled() || meveoModule.isActive()) {
            throw new ActionForbiddenException(meveoModule.getClass(), code, "enable", "Module is not installed or already enabled");
        }
        meveoModuleService.enable(meveoModule);
    }

    public void disable(String code, Class<? extends MeveoModule> moduleClass) throws MeveoApiException, BusinessException {

        if (StringUtils.isBlank(code)) {
            missingParameters.add("code");
            handleMissingParameters();
        }

        if (moduleClass == null) {
            moduleClass = MeveoModule.class;
        }

        MeveoModule meveoModule = meveoModuleService.findByCode(code);
        if (meveoModule == null) {
            throw new EntityDoesNotExistsException(moduleClass, code);
        }

        if (!meveoModule.isInstalled() || meveoModule.isDisabled()) {
            throw new ActionForbiddenException(meveoModule.getClass(), code, "disable", "Module is not installed or already disabled");
        }

        meveoModuleService.disable(meveoModule);
    }

    /**
     * Convert MeveoModule or its subclass object to DTO representation.
     * 
     * @param module Module object
     * @return MeveoModuleDto object
     * @throws MeveoApiException meveo api exception.
     */
    @SuppressWarnings({ "rawtypes"})
    public MeveoModuleDto moduleToDto(MeveoModule module) throws MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {

        if (module.isDownloaded() && !module.isInstalled()) {
            try {
                return MeveoModuleUtils.moduleSourceToDto(module);
            } catch (Exception e) {
                log.error("Failed to load module source {}", module.getCode(), e);
                throw new MeveoApiException("Failed to load module source");
            }
        }

        Class<? extends MeveoModuleDto> dtoClass = MeveoModuleDto.class;
        if (module instanceof BusinessServiceModel) {
            dtoClass = BusinessServiceModelDto.class;
        }

        MeveoModuleDto moduleDto;
        try {
            moduleDto = dtoClass.getConstructor(MeveoModule.class).newInstance(module);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            log.error("Failed to instantiate Module Dto. No reason for it to happen. ", e);
            throw new RuntimeException("Failed to instantiate Module Dto. No reason for it to happen. ", e);
        }

        if (!StringUtils.isBlank(module.getLogoPicture())) {
            try {
                moduleDto.setLogoPictureFile(ModuleUtil.readModulePicture(currentUser.getProviderCode(), module.getLogoPicture()));
            } catch (Exception e) {
                log.error("Failed to read module files {}, info {}", module.getLogoPicture(), e.getMessage(), e);
            }
        }

        List<MeveoModuleItem> moduleItems = module.getModuleItems();
        if (moduleItems != null) {
            for (MeveoModuleItem item : moduleItems) {

                try {
                    BaseEntityDto itemDto;

                    if (item.getItemClass().equals(CustomFieldTemplate.class.getName())) {
                        itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());

                    } else if (item.getItemClass().equals(EntityCustomAction.class.getName())) {
                        itemDto = entityCustomActionApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());

                    } else {
                        Class clazz = Class.forName(item.getItemClass());
                        if (clazz.isAnnotationPresent(VersionedEntity.class)) {
                            ApiVersionedService apiService = getApiVersionedService(item.getItemClass(), true);
                            itemDto = apiService.findIgnoreNotFound(item.getItemCode(), item.getValidity() != null ? item.getValidity().getFrom() : null,
                                item.getValidity() != null ? item.getValidity().getTo() : null);

                        } else {
                            ApiService apiService = getApiService(clazz, true);
                            itemDto = apiService.findIgnoreNotFound(item.getItemCode());
                        }
                    }
                    if (itemDto != null) {
                        moduleDto.addModuleItem(itemDto);
                    } else {
                        log.warn("Failed to find a module item {}", item);
                    }

                } catch (ClassNotFoundException e) {
                    log.error("Failed to find a class", e);
                    throw new MeveoApiException("Failed to access field value in DTO: " + e.getMessage());

                } catch (MeveoApiException e) {
                    log.error("Failed to transform module item to DTO. Module item {}", item, e);
                    throw e;
                }
            }
        }

        // Finish converting subclasses of MeveoModule class
        if (module instanceof BusinessServiceModel) {
            businessServiceModelToDto((BusinessServiceModel) module, (BusinessServiceModelDto) moduleDto);

        }

        return moduleDto;
    }


    /**
     * Finish converting BusinessServiceModel object to DTO representation
     * 
     * @param bsm BusinessServiceModel object to convert
     * @param dto BusinessServiceModel object DTO representation (as result of base MeveoModule object conversion)
     */
    private void businessServiceModelToDto(BusinessServiceModel bsm, BusinessServiceModelDto dto) {

        if (bsm.getServiceTemplate() != null) {
            dto.setServiceTemplate(new ServiceTemplateDto(bsm.getServiceTemplate(), entityToDtoConverter.getCustomFieldsDTO(bsm.getServiceTemplate(), true)));
        }
        dto.setDuplicateService(bsm.isDuplicateService());
        dto.setDuplicatePricePlan(bsm.isDuplicatePricePlan());

    }

	@Override
	public MeveoModuleDto toDto(MeveoModule entity) {
        try {
            return moduleToDto(entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public MeveoModule fromDto(MeveoModuleDto dto) throws org.meveo.exceptions.EntityDoesNotExistsException {
        try {
            MeveoModule meveoModule = new MeveoModule();
            parseModuleInfoOnlyFromDto(meveoModule, dto);
            return meveoModule;
        } catch (MeveoApiException | BusinessException e) {
            throw new RuntimeException(e);
        }
    }

	@Override
	public IPersistenceService<MeveoModule> getPersistenceService() {
		return meveoModuleService;
	}

	@Override
	public boolean exists(MeveoModuleDto dto) {
        try {
            return find(dto.getCode()) != null;
        } catch (Exception e) {
            return false;
        }
	}
}