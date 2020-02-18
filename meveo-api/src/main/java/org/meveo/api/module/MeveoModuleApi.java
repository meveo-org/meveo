/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.api.module;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessEntityException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ModuleUtil;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.ApiService;
import org.meveo.api.ApiUtils;
import org.meveo.api.ApiVersionedService;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.CustomFieldTemplateApi;
import org.meveo.api.EntityCustomActionApi;
import org.meveo.api.ScriptInstanceApi;
import org.meveo.api.admin.FilesApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.catalog.BusinessServiceModelDto;
import org.meveo.api.dto.catalog.ServiceTemplateDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.ModuleItem;
import org.meveo.model.VersionedEntity;
import org.meveo.model.catalog.BusinessServiceModel;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.util.EntityCustomizationUtils;
import org.reflections.Reflections;

/**
 * @author Clément Bareth
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author Wassim Drira
 * @lastModifiedVersion 6.3.0
 */
@Stateless
public class MeveoModuleApi extends BaseCrudApi<MeveoModule, MeveoModuleDto> {

    private static boolean initalized = false;
    
    @Inject
    private MeveoModuleService meveoModuleService;

    @Inject
    private CustomFieldTemplateApi customFieldTemplateApi;

    @Inject
    private EntityCustomActionApi entityCustomActionApi;

    @Inject
    private ScriptInstanceApi scriptInstanceApi;

    @Inject
    private FilesApi filesApi;

    @Inject
    private ScriptInstanceService scriptInstanceService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;
    
    @Inject
    private MeveoModuleItemInstaller meveoModuleItemInstaller;

    public MeveoModuleApi() {
    	super(MeveoModule.class, MeveoModuleDto.class);
    	if(!initalized) {
    		registerModulePackage("org.meveo.model");
    		initalized = true;
    	}
    }
    
	public MeveoModule install(MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {
		MeveoModule meveoModule = meveoModuleService.findByCode(moduleDto.getCode());
		if (meveoModule == null) {
			create(moduleDto, false);
			meveoModule = meveoModuleService.findByCode(moduleDto.getCode());
		}

		meveoModuleItemInstaller.install(meveoModule, moduleDto);
		return meveoModule;
	}
    
    public void registerModulePackage(String packageName) {
    	Reflections reflections = new Reflections(packageName);
    	Set<Class<?>> moduleItemClasses = reflections.getTypesAnnotatedWith(ModuleItem.class);

        for(Class<?> aClass : moduleItemClasses){
            String type = aClass.getAnnotation(ModuleItem.class).value();
            MeveoModuleItemInstaller.MODULE_ITEM_TYPES.put(type, aClass);
            log.debug("Registering module item type {} from class {}", type, aClass);
        }
    }

    public MeveoModule create(MeveoModuleDto moduleDto, boolean development) throws MeveoApiException, BusinessException {

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

        if(development){
            meveoModule.setModuleSource(null);
        }

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

        if (CollectionUtils.isEmpty(moduleDto.getModuleFiles())) {
            missingParameters.add("module files is null");
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

    public List<MeveoModuleDto> list(MeveoModuleFilters filters) {
        if(filters.getItemType() != null){
            filters.setItemClass(MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(filters.getItemType()).getName());
        }

        return meveoModuleService.list(filters)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<String> listCodesOnly(MeveoModuleFilters filters) {
        if(filters.getItemType() != null){
        	try {
        		filters.setItemClass(MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(filters.getItemType()).getName());
        	} catch(NullPointerException e) {
        		log.error("{} is not a module item type", filters.getItemType());
        	}
        }

        return meveoModuleService.listCodesOnly(filters);
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
            return create(postData, false);
        } else {
            // update
            return update(postData);
        }
    }

    public void uninstall(String code, Class<? extends MeveoModule> moduleClass, boolean remove) throws MeveoApiException, BusinessException {

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
        meveoModuleService.uninstall(meveoModule, remove);
    }


    private void parseModuleInfoOnlyFromDtoBSM(BusinessServiceModel bsm, BusinessServiceModelDto bsmDto) {

        bsm.setDuplicatePricePlan(bsmDto.isDuplicatePricePlan());
        bsm.setDuplicateService(bsmDto.isDuplicateService());
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
        if (CollectionUtils.isNotEmpty(moduleDto.getModuleFiles())) {
           for (String moduleFile : moduleDto.getModuleFiles()) {
               meveoModule.addModuleFile(moduleFile);
           }
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
    @SuppressWarnings({ "rawtypes", "unchecked"})
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

        Set<String> moduleFiles = module.getModuleFiles();
        if (moduleFiles != null) {
            for (String moduleFile : moduleFiles) {
                moduleDto.addModuleFile(moduleFile);
            }
        }

        List<MeveoModuleItem> moduleItems = module.getModuleItems();
        if (moduleItems != null) {
            for (MeveoModuleItem item : moduleItems) {

                try {
                    BaseEntityDto itemDto = null;

					if (item.getItemClass().equals(CustomFieldTemplate.class.getName())) {
						// we will only add a cft if it's not a field of a cet
						if (!StringUtils.isBlank(item.getAppliesTo())) {
							String cetCode = EntityCustomizationUtils.getEntityCode(item.getAppliesTo());
							if (customEntityTemplateService.findByCode(cetCode) == null) {
								itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
							}

						} else {
							itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
						}

                    } else if (item.getItemClass().equals(EntityCustomAction.class.getName())) {
                        itemDto = entityCustomActionApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());

                    } else {
                        Class clazz = Class.forName(item.getItemClass());
                        if (clazz.isAnnotationPresent(VersionedEntity.class)) {
                            ApiVersionedService apiService = ApiUtils.getApiVersionedService(item.getItemClass(), true);
                            itemDto = apiService.findIgnoreNotFound(item.getItemCode(), item.getValidity() != null ? item.getValidity().getFrom() : null,
                                item.getValidity() != null ? item.getValidity().getTo() : null);

                        } else {
                            ApiService apiService = ApiUtils.getApiService(clazz, true);
                            itemDto = apiService.findIgnoreNotFound(item.getItemCode());
                        }
                    }
                    if (itemDto != null) {
                        moduleDto.addModuleItem(itemDto);
                        
                    } else {
                        log.warn("Failed to find a module item or not added in case of CFT that is a field of CET {}", item);
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

	public MeveoModuleDto addToModule(String code, String itemCode, String itemType) throws EntityDoesNotExistsException, BusinessException {
        final MeveoModule module = meveoModuleService.findByCode(code);
        if(module == null){
            throw new EntityDoesNotExistsException(MeveoModule.class, code);
        }

        final String itemClassName = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(itemType).getName();

        MeveoModuleItem moduleItem = new MeveoModuleItem();
        moduleItem.setMeveoModule(module);
        moduleItem.setItemCode(itemCode);
        moduleItem.setItemClass(itemClassName);

        module.addModuleItem(moduleItem);
        meveoModuleService.update(module);

        return toDto(module);
    }

    public MeveoModuleDto removeFromModule(String code, String itemCode, String itemType) throws EntityDoesNotExistsException, BusinessException {
        final MeveoModule module = meveoModuleService.findByCode(code);
        if(module == null){
            throw new EntityDoesNotExistsException(MeveoModule.class, code);
        }

        final String itemClassName = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(itemType).getName();

        MeveoModuleItem moduleItem = new MeveoModuleItem();
        moduleItem.setMeveoModule(module);
        moduleItem.setItemCode(itemCode);
        moduleItem.setItemClass(itemClassName);

        module.removeItem(moduleItem);
        meveoModuleService.update(module);

        return toDto(module);
    }

    public MeveoModuleDto addFileToModule(String code, String path) throws EntityDoesNotExistsException, BusinessException {
        final MeveoModule module = meveoModuleService.findByCode(code);
        if (module == null) {
            throw new EntityDoesNotExistsException(MeveoModule.class, code);
        }
        if (filesApi.checkFile(path)) {
            module.addModuleFile(path);
        }

        meveoModuleService.update(module);

        return toDto(module);
    }

    public MeveoModuleDto removeFileFromModule(String code, String path) throws EntityDoesNotExistsException, BusinessException {
        final MeveoModule module = meveoModuleService.findByCode(code);
        if(module == null){
            throw new EntityDoesNotExistsException(MeveoModule.class, code);
        }

        if (module.getModuleFiles().contains(path)) {
            module.removeModuleFile(path);
        }
        meveoModuleService.update(module);

        return toDto(module);
    }

    public boolean isChildOfOtherActiveModule(String moduleItemCode, String itemType) {
        final String itemClassName = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(itemType).getName();
        return meveoModuleService.isChildOfOtherActiveModule(moduleItemCode, itemClassName);
    }

	public void fork(String moduleCode) throws MeveoApiException, BusinessException {
		MeveoModule module = meveoModuleService.findByCode(moduleCode);

		if (module == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, moduleCode);
		}

		if (!module.isDownloaded()) {
			throw new BusinessEntityException("Module must be downloaded");
		}

		MeveoModuleDto moduleDto = MeveoModuleUtils.moduleSourceToDto(module);

		module = install(moduleDto);
		
		module.setModuleSource(null);
	}
}