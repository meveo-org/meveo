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

import static org.meveo.commons.utils.FileUtils.addDirectoryToZip;
import static org.meveo.commons.utils.FileUtils.addToZipFile;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessEntityException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
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
import org.meveo.api.dto.CustomEntityInstanceDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.EntityCustomActionDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.ModuleDependencyDto;
import org.meveo.api.dto.module.ModuleReleaseDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exceptions.ModuleInstallFail;
import org.meveo.api.export.ExportFormat;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.ModuleItem;
import org.meveo.model.VersionedEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.module.ModuleLicenseEnum;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.model.module.ModuleRelease;
import org.meveo.model.module.ModuleReleaseItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.typereferences.GenericTypeReferences;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.git.GitHelper;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.storage.RepositoryService;
import org.meveo.util.EntityCustomizationUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * @author Clément Bareth
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10
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
	
	@Inject
	private MeveoModulePatchApi modulePatchApi;
	
	@EJB
	private MeveoModuleApi meveoModuleApi;
	
    @Inject
    private RepositoryService repositoryService;
    
    @Inject
    private CrossStorageService crossStorageService;

	public MeveoModuleApi() {
		super(MeveoModule.class, MeveoModuleDto.class);
		if (!initalized) {
			registerModulePackage("org.meveo.model");
			initalized = true;
		}
	}

	public ModuleInstallResult install(GitRepository repo) throws BusinessException, MeveoApiException {
		
		ModuleInstallResult result = null;
		
		MeveoModuleDto moduleDto = buildMeveoModuleFromDirectory(repo);
		result = install(moduleDto, OnDuplicate.FAIL);
		
		return result;
	}
	
	public ModuleInstallResult install(MeveoModuleDto moduleDto, OnDuplicate onDuplicate) throws MeveoApiException, BusinessException {
		MeveoModule meveoModule = meveoModuleService.findByCode(moduleDto.getCode(), meveoModuleService.getLazyLoadedProperties());
		if (meveoModule == null) {
			meveoModule = meveoModuleApi.createOrUpdate(moduleDto);
		}
		
		try {
			return meveoModuleItemInstaller.install(meveoModule, moduleDto, onDuplicate);
		} catch (ModuleInstallFail e) {
    		log.warn("Failed to install module {}, uninstalling items", meveoModule);
    		
//    		for(MeveoModuleItemDto item : e.getResult().getInstalledItems()) {
//    			meveoModuleItemInstaller.uninstallItemDto(meveoModule, item);
//    		}
//    		
    		throw e.getException();
		}
	}

	@SuppressWarnings("rawtypes")
	public MeveoModuleDto buildMeveoModuleFromDirectory(GitRepository repo) {
		MeveoModuleDto moduleDto = new MeveoModuleDto();
		moduleDto.setCode(repo.getCode());
		moduleDto.setModuleItems(new ArrayList<>());
		moduleDto.setDescription(repo.getCode());
		moduleDto.setLicense(ModuleLicenseEnum.APACHE);
		
		File repoDir = GitHelper.getRepositoryDir(null, moduleDto.getCode());
		Map<String, String> entityDtoNamebyPath = new HashMap<String, String>();
		Map<String, Function<String, Boolean>> validationMatrix = new HashMap<String, Function<String, Boolean>>();
		
		MeveoModuleItemInstaller.MODULE_ITEM_TYPES.values().forEach(clazz -> {
			ModuleItem item = clazz.getAnnotation(ModuleItem.class);
			try {
				if (clazz == CustomFieldTemplate.class) {
					entityDtoNamebyPath.put(item.path(), CustomFieldTemplateDto.class.getName());
					validationMatrix.put(item.path(), fileName -> true);
				} else if (clazz == EntityCustomAction.class) {
					entityDtoNamebyPath.put(item.path(), EntityCustomActionDto.class.getName());
					validationMatrix.put(item.path(), fileName -> true);
				} else {
					BaseCrudApi api = (BaseCrudApi)ApiUtils.getApiService(clazz, false);
					if (api != null) {
						entityDtoNamebyPath.put(item.path(), api.getDtoClass().getName());
						validationMatrix.put(item.path(), fileName -> api.validateModuleFileName(fileName));
					}
				}
			} catch (Exception e) {
				log.error("Can't retrieve dto class for {}",clazz.getName(), e);
			}
		});
		
		for (File file : repoDir.listFiles()) {
			if (!file.isDirectory()) {
				continue;
			}
			String entityName = file.getName();
			String dtoClassName = entityDtoNamebyPath.get(entityName);
			if (dtoClassName == null) {
				continue;
			}
			for (File DirectoryFile : file.listFiles()) {
				if (!DirectoryFile.isDirectory()) {
					continue;
				}
				for (File entityFile : DirectoryFile.listFiles()) {
					try {
						if (!entityFile.getName().endsWith(".json")) {
							continue;
						}
						if (!validationMatrix.get(entityName).apply(entityFile.getName())) {
							continue;
						}
						String fileToString = org.apache.commons.io.FileUtils.readFileToString(entityFile, StandardCharsets.UTF_8);
						Map<String, Object> data = JacksonUtil.fromString(fileToString, GenericTypeReferences.MAP_STRING_OBJECT);
						MeveoModuleItemDto moduleItemDto = new MeveoModuleItemDto(dtoClassName, data);
						moduleDto.getModuleItems().add(moduleItemDto);
					} catch (IOException e) {
						log.error("Can't read entityFile", e);
					}
				}
			}
		}
		
		return moduleDto;
	}
	
	public void registerModulePackage(String packageName) {
		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> moduleItemClasses = reflections.getTypesAnnotatedWith(ModuleItem.class);

		for (Class<?> aClass : moduleItemClasses) {
			MeveoModuleItemInstaller.MODULE_ITEM_TYPES.put(aClass.getSimpleName(), aClass);
			log.debug("Registering module item type {} from class {}", aClass.getSimpleName(), aClass);
		}
	}
	
    public List<Class<?>> getModuleItemClasses() {
    	return new ArrayList<>(MeveoModuleItemInstaller.MODULE_ITEM_TYPES.values());
    }

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public MeveoModule createInNewTx(MeveoModuleDto moduleDto, boolean development) throws MeveoApiException, BusinessException {
		return create(moduleDto, development);
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

		if (development) {
			meveoModule.setModuleSource(null);
		}

		meveoModuleService.create(meveoModule);

		modulePatchApi.postCreateOrUpdate(meveoModule, moduleDto);
		
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
		MeveoModule meveoModuleBackup = meveoModuleService.findByCode(moduleDto.getCode());
		MeveoModule meveoModule = meveoModuleService.findByCode(moduleDto.getCode());
		if (meveoModule == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, moduleDto.getCode());
		}

		if (!meveoModule.isDownloaded()) {
			throw new ActionForbiddenException(meveoModule.getClass(), moduleDto.getCode(), "install", "Module with the same code is being developped locally, can not overwrite it.");
		}

		if (meveoModule.getModuleItems() != null) {
			Iterator<MeveoModuleItem> itr = meveoModule.getModuleItems().iterator();
			while (itr.hasNext()) {
				MeveoModuleItem i = itr.next();
				i.setMeveoModule(null);
				itr.remove();
			}
		}

		if (meveoModule.getModuleDependencies() != null) {
			Iterator<MeveoModuleDependency> iterator = meveoModule.getModuleDependencies().iterator();
			while (iterator.hasNext()) {
				MeveoModuleDependency d = iterator.next();
				d.setMeveoModule(null);
				iterator.remove();
			}
		}

		parseModuleInfoOnlyFromDto(meveoModule, moduleDto);

		meveoModuleService.update(meveoModule);

		meveoModule = meveoModuleService.findById(meveoModule.getId());

		meveoModuleService.flush();

		modulePatchApi.postCreateOrUpdate(meveoModule, moduleDto);

		meveoModule = meveoModuleService.findById(meveoModule.getId());

		if (meveoModule.getScript() != null) {
			boolean checkRelease = meveoModuleService.checkTestSuites(meveoModule.getScript().getCode());
			if (!checkRelease) {
				meveoModule = meveoModuleService.update(meveoModuleBackup);
				throw new EJBTransactionRolledbackException("Test suit is failed, automatic rollback");
			}
		}
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

	public void delete(String code, boolean deleteFiles) throws EntityDoesNotExistsException, BusinessException, IOException {

		MeveoModule meveoModule = meveoModuleService.findByCode(code);
		if (meveoModule == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, code);
		}
		List<String> moduleFiles = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(meveoModule.getModuleFiles())) {
			for (String moduleFile : meveoModule.getModuleFiles()) {
				moduleFiles.add(moduleFile);
			}
		}
		String logoPicture = meveoModule.getLogoPicture();
		meveoModuleService.remove(meveoModule);
		if (CollectionUtils.isNotEmpty(moduleFiles) && deleteFiles) {
			meveoModuleService.removeFilesIfModuleIsDeleted(moduleFiles);
		}
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
		if (filters.getItemType() != null) {
			filters.setItemClass(MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(filters.getItemType()).getName());
		}

		return meveoModuleService.list(filters).stream().map(this::toDto).collect(Collectors.toList());
	}

	public List<String> listCodesOnly(MeveoModuleFilters filters) {
		if (filters.getItemType() != null) {
			try {
				filters.setItemClass(MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(filters.getItemType()).getName());
			} catch (NullPointerException e) {
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
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@JpaAmpNewTx
	public MeveoModule createOrUpdateInNewTx(MeveoModuleDto postData) throws MeveoApiException, BusinessException {
		return createOrUpdate(postData);
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

	public MeveoModule uninstall(String code, Class<? extends MeveoModule> moduleClass, boolean remove) throws MeveoApiException, BusinessException {

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
		
		if(meveoModuleService.hasDependencies(meveoModule)) {
			throw new BusinessException("Unable to uninstall a referenced module.");
		}

		return meveoModuleItemInstaller.uninstall(meveoModule, remove);
	}

	

	public void parseModuleInfoOnlyFromDto(MeveoModule meveoModule, MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {

		meveoModule.setCode(StringUtils.isBlank(moduleDto.getUpdatedCode()) ? moduleDto.getCode() : moduleDto.getUpdatedCode());
		meveoModule.setDescription(moduleDto.getDescription());
		meveoModule.setLicense(moduleDto.getLicense());
		if (!StringUtils.isBlank(moduleDto.getCurrentVersion())) {
			meveoModule.setCurrentVersion(moduleDto.getCurrentVersion());
		}
		meveoModule.setLogoPicture(moduleDto.getLogoPicture());
		meveoModule.setIsInDraft(moduleDto.isInDraft());
		meveoModule.setMeveoVersionBase(moduleDto.getMeveoVersionBase());
		meveoModule.setMeveoVersionCeiling(moduleDto.getMeveoVersionCeiling());
		if (!StringUtils.isBlank(moduleDto.getLogoPicture()) && moduleDto.getLogoPictureFile() != null) {
			writeModulePicture(moduleDto.getLogoPicture(), moduleDto.getLogoPictureFile());
		}
		if (meveoModule.isTransient()) {
			meveoModule.setInstalled(false);
		}

		meveoModule.getModuleFiles().clear();
		if (CollectionUtils.isNotEmpty(moduleDto.getModuleFiles())) {
			for (String moduleFile : moduleDto.getModuleFiles()) {
				meveoModule.addModuleFile(moduleFile);
			}
		}
		if (CollectionUtils.isNotEmpty(moduleDto.getModuleDependencies())) {
			meveoModule.getModuleDependencies().clear();
			for (ModuleDependencyDto dependencyDto : moduleDto.getModuleDependencies()) {
				MeveoModuleDependency moduleDependency = new MeveoModuleDependency(dependencyDto.getCode(), dependencyDto.getDescription(), dependencyDto.getCurrentVersion());
				meveoModule.addModuleDependency(moduleDependency);
			}
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MeveoModuleDto moduleToDto(MeveoModule module) throws MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {

		if (module.isDownloaded() && !module.isInstalled()) {
			try {
				MeveoModuleDto moduleDto = MeveoModuleUtils.moduleSourceToDto(module);

				moduleDto.setCurrentVersion(module.getCurrentVersion());
				
				if (module.getPatches() != null && !module.getPatches().isEmpty()) {
					moduleDto.setPatches(module.getPatches().stream().map(e -> modulePatchApi.toDto(e)).collect(Collectors.toList()));
				}
				
				return moduleDto;
				
			} catch (Exception e) {
				log.error("Failed to load module source {}", module.getCode(), e);
				throw new MeveoApiException("Failed to load module source");
			}
		}

		Class<? extends MeveoModuleDto> dtoClass = MeveoModuleDto.class;

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

		if (module.getMeveoVersionBase() != null) {
			moduleDto.setMeveoVersionBase(module.getMeveoVersionBase());
		}

		if (module.getMeveoVersionCeiling() != null) {
			moduleDto.setMeveoVersionCeiling(module.getMeveoVersionCeiling());
		}

		Set<String> moduleFiles = module.getModuleFiles();
		if (moduleFiles != null) {
			for (String moduleFile : moduleFiles) {
				moduleDto.addModuleFile(moduleFile);
			}
		}
		
		Set<MeveoModuleDependency> moduleDependencies = module.getModuleDependencies();
		if (moduleDependencies != null) {
			for (MeveoModuleDependency dependency : moduleDependencies) {
				moduleDto.addModuleDependency(dependency);
			}
		}

		Set<MeveoModuleItem> moduleItems = module.getModuleItems();
		if (moduleItems != null) {
			for (MeveoModuleItem item : moduleItems) {

				try {
					BaseEntityDto itemDto = null;

					if (item.getItemClass().equals(CustomFieldTemplate.class.getName())) {
						// we will only add a cft if it's not a field of a cet contained in the module
						if (!StringUtils.isBlank(item.getAppliesTo())) {
							String cetCode = EntityCustomizationUtils.getEntityCode(item.getAppliesTo());
							
							boolean isCetInModule = moduleItems.stream()
									.filter(moduleItem -> moduleItem.getItemClass().equals(CustomEntityTemplate.class.getName()))
									.anyMatch(moduleItem -> moduleItem.getItemCode().equals(cetCode));
							
							if (!isCetInModule) {
								itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
							}

						} else {
							itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
						}

					} else if (item.getItemClass().equals(EntityCustomAction.class.getName())) {
						EntityCustomActionDto entityCustomActionDto = entityCustomActionApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
						itemDto = entityCustomActionDto;

					} else if (item.getItemClass().equals(CustomEntityInstance.class.getName()) && item.getAppliesTo() != null) {
						try {
				            CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(item.getAppliesTo());

							Map<String, Object> ceiTable;
				        	try {
				        		ceiTable = crossStorageService.find(
				    				repositoryService.findDefaultRepository(), // XXX: Maybe we will need to parameterize this or search in all repositories ?
				    				customEntityTemplate,
				    				item.getItemCode(),
				    				false	// XXX: Maybe it should also be a parameter
				    			);
				        	} catch (EntityDoesNotExistsException e) {
				        		ceiTable = null;
				        	}
							
							//Map<String, Object> ceiTable = customTableService.findById(SqlConfiguration.DEFAULT_SQL_CONNECTION, item.getAppliesTo(), item.getItemCode());
							CustomEntityInstance customEntityInstance = new CustomEntityInstance();
							customEntityInstance.setUuid((String) ceiTable.get("uuid"));
							customEntityInstance.setCode((String) ceiTable.get("uuid"));
							customEntityInstance.setCetCode(item.getAppliesTo());
							customEntityInstance.setCet(customEntityTemplateService.findByCode(item.getAppliesTo()));
							customFieldInstanceService.setCfValues(customEntityInstance, item.getAppliesTo(), ceiTable);
							itemDto = CustomEntityInstanceDto.toDTO(customEntityInstance, entityToDtoConverter.getCustomFieldsDTO(customEntityInstance, true));
						} catch (BusinessException e) {
							log.error(e.getMessage());
						}
					} else {
						Class clazz = Class.forName(item.getItemClass());
						if (clazz.isAnnotationPresent(VersionedEntity.class)) {
							ApiVersionedService apiService = ApiUtils.getApiVersionedService(item.getItemClass(), true);
							itemDto = apiService.findIgnoreNotFound(
									item.getItemCode(), 
									item.getValidity() != null ? item.getValidity().getFrom() : null,
									item.getValidity() != null ? item.getValidity().getTo() : null
								);

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

		
		if (module.getPatches() != null && !module.getPatches().isEmpty()) {
			moduleDto.setPatches(module.getPatches().stream().map(e -> modulePatchApi.toDto(e)).collect(Collectors.toList()));
		}

		return moduleDto;
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
	public MeveoModule fromDto(MeveoModuleDto dto) throws MeveoApiException {
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

	public MeveoModuleDto addToModule(String code, String itemCode, String itemType, String appliesTo) throws EntityDoesNotExistsException, BusinessException {
		final MeveoModule module = meveoModuleService.findByCode(code);
		if (module == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, code);
		}

		final String itemClassName = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(itemType).getName();

		MeveoModuleItem moduleItem = new MeveoModuleItem();
		moduleItem.setMeveoModule(module);
		moduleItem.setItemCode(itemCode);
		moduleItem.setItemClass(itemClassName);
		moduleItem.setAppliesTo(appliesTo);
		meveoModuleService.loadModuleItem(moduleItem);
		
		if(moduleItem.getItemEntity() == null) {
			throw new BusinessException("Failed to load entity for module item " + moduleItem);
		}

		meveoModuleService.addModuleItem(moduleItem, module);

        var api = ApiUtils.getApiService(moduleItem.getItemEntity().getClass(), true);
        if(api instanceof BaseCrudApi) {
        	BaseCrudApi crudApi = (BaseCrudApi) api;
        	crudApi.addToModule(moduleItem.getItemEntity(), module);
        }
		
		meveoModuleService.update(module);

		return toDto(module);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MeveoModuleDto removeFromModule(String code, String itemCode, String itemType, String appliesTo) throws EntityDoesNotExistsException, BusinessException {
		final MeveoModule module = meveoModuleService.findByCode(code);
		if (module == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, code);
		}

		final String itemClassName = MeveoModuleItemInstaller.MODULE_ITEM_TYPES.get(itemType).getName();
		if(itemClassName == null) {
			throw new IllegalArgumentException(itemType + " is not a module item type");
		}
		
		MeveoModuleItem moduleItem = new MeveoModuleItem();
		moduleItem.setMeveoModule(module);
		moduleItem.setItemCode(itemCode);
		moduleItem.setItemClass(itemClassName);
		moduleItem.setAppliesTo(appliesTo);
		meveoModuleService.loadModuleItem(moduleItem);
		
		module.removeItem(moduleItem);
		
        var api = ApiUtils.getApiService(moduleItem.getItemEntity().getClass(), true);
        if(api instanceof BaseCrudApi) {
        	BaseCrudApi crudApi = (BaseCrudApi) api;
        	crudApi.removeFromModule(moduleItem.getItemEntity(), module);
        }
        
		meveoModuleService.update(module);


		return toDto(module);
	}

	public MeveoModuleDto addFileToModule(String code, String path) throws EntityDoesNotExistsException, BusinessException {
		MeveoModule module = meveoModuleService.findByCode(code);
		if (module == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, code);
		}
		module = meveoModuleService.findById(module.getId());
		if (filesApi.checkFile(path)) {
			module.addModuleFile(path);
		}
		meveoModuleService.flush();
		meveoModuleService.update(module);

		return toDto(module);
	}

	public MeveoModuleDto removeFileFromModule(String code, String path) throws EntityDoesNotExistsException, BusinessException {
		final MeveoModule module = meveoModuleService.findByCode(code);
		if (module == null) {
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

//		MeveoModuleDto moduleDto = MeveoModuleUtils.moduleSourceToDto(module);
//
//		module = install(moduleDto);
//
//		module.setModuleSource(null);
		
		module.setIsInDraft(true);
	}

	@Override
	public void importJSON(InputStream json, boolean overwrite) throws BusinessException, IOException, MeveoApiException {
		List<MeveoModuleDto> modules = getModules(json);

		try {
			checkModuleDependencies(modules);
			// Import files contained in each modules
			importFileFromModule(modules);

			importEntities(modules, overwrite);
		} catch (EntityDoesNotExistsException e) {
			throw new EntityDoesNotExistsException(e.getMessage());
		}
	}

	public void importJSON(List<MeveoModuleDto> modules, boolean overwrite) throws BusinessException, IOException, MeveoApiException {

		// Import files contained in each modules
		importFileFromModule(modules);

		importEntities(modules, overwrite);

		for (MeveoModuleDto moduleDto : modules) {
			if (!moduleDto.isInDraft()) {
				MeveoModule module = meveoModuleService.findByCode(moduleDto.getCode());
				ModuleRelease moduleRelease = new ModuleRelease();
				moduleRelease.setCode(module.getCode());
				moduleRelease.setDescription(module.getDescription());
				moduleRelease.setLicense(module.getLicense());
				moduleRelease.setLogoPicture(module.getLogoPicture());
				moduleRelease.setScript(module.getScript());
				moduleRelease.setCurrentVersion(module.getCurrentVersion());
				moduleRelease.setMeveoVersionBase(module.getMeveoVersionBase());
				moduleRelease.setMeveoVersionCeiling(module.getMeveoVersionCeiling());
				moduleRelease.setModuleSource(module.getModuleSource());
				if (CollectionUtils.isNotEmpty(module.getModuleFiles())) {
					Set<String> moduleFiles = new HashSet<>();
					for (String moduleFile : module.getModuleFiles()) {
						moduleFiles.add(moduleFile);
					}
					moduleRelease.setModuleFiles(moduleFiles);
				}
				if (CollectionUtils.isNotEmpty(module.getModuleDependencies())) {
					List<MeveoModuleDependency> dependencies = new ArrayList<>();
					for (MeveoModuleDependency moduleDependency : module.getModuleDependencies()) {
						dependencies.add(moduleDependency);
					}
					moduleRelease.setModuleDependencies(dependencies);
				}
				if (CollectionUtils.isNotEmpty(module.getModuleItems())) {
					List<ModuleReleaseItem> moduleReleaseItems = new ArrayList<>();
					for (MeveoModuleItem meveoModuleItem : module.getModuleItems()) {
						ModuleReleaseItem moduleReleaseItem = new ModuleReleaseItem();
						moduleReleaseItem.setAppliesTo(meveoModuleItem.getAppliesTo());
						moduleReleaseItem.setItemClass(meveoModuleItem.getItemClass());
						moduleReleaseItem.setItemEntity(meveoModuleItem.getItemEntity());
						moduleReleaseItem.setItemCode(meveoModuleItem.getItemCode());
						moduleReleaseItem.setModuleRelease(moduleRelease);
						moduleReleaseItems.add(moduleReleaseItem);
					}
					moduleRelease.setModuleItems(moduleReleaseItems);
				} else if (!StringUtils.isBlank(module.getModuleSource())) {
					ModuleReleaseDto moduleReleaseDto = JacksonUtil.fromString(module.getModuleSource(), ModuleReleaseDto.class);
					moduleReleaseDto.setCurrentVersion(module.getCurrentVersion());

					if (CollectionUtils.isNotEmpty(moduleReleaseDto.getModuleFiles())) {
						moduleReleaseDto.getModuleFiles().clear();
					}
					if (CollectionUtils.isNotEmpty(module.getModuleFiles())) {
						for (String moduleFile : module.getModuleFiles()) {
							moduleReleaseDto.getModuleFiles().add(moduleFile);
						}
					}

					if (CollectionUtils.isNotEmpty(moduleReleaseDto.getModuleDependencies())) {
						moduleReleaseDto.getModuleDependencies().clear();
					}
					if (CollectionUtils.isNotEmpty(module.getModuleDependencies())) {
						for (MeveoModuleDependency dependency : module.getModuleDependencies()) {
							moduleReleaseDto.addModuleDependency(dependency);
						}
					}
					moduleRelease.setModuleSource(JacksonUtil.toString(moduleReleaseDto));
				}
				moduleRelease.setMeveoModule(module);
				module.getReleases().add(moduleRelease);
			}
		}

	}

	public List<MeveoModuleDto> getModules(InputStream json) throws IOException {
		ObjectMapper jsonMapper = new ObjectMapper();
		return jsonMapper.readValue(json, new TypeReference<List<MeveoModuleDto>>() {
		});
	}

	public void importFileFromModule(List<MeveoModuleDto> modules) throws FileNotFoundException {

		for (MeveoModuleDto module : modules) {
			List<String> moduleFiles = module.getModuleFiles();
			if (moduleFiles == null) {
				continue;
			}

			if (!getFileImport().isEmpty()) {
				File parentDir = getFileImport().iterator().next().getParentFile();

				for (String moduleFile : moduleFiles) {
					File fileToImport = new File(parentDir, moduleFile);

					String chrootDir = paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
					File targetFile = new File(chrootDir , moduleFile);
					if (!targetFile.exists() && fileToImport.isDirectory()) {
						targetFile.mkdirs();
					}

					if (fileToImport.isDirectory()) {
						copyFileFromFolder(targetFile, fileToImport);
					} else {
						FileInputStream inputStream = new FileInputStream(fileToImport);
						copyFile(targetFile, inputStream);
					}
				}
			}
		}
	}


	public void copyFileFromFolder(File targetFolder, File file) throws FileNotFoundException {
		File[] files = file.listFiles();
		if (files != null) {
			for (File fileFromFolder : files) {
				String name = fileFromFolder.getName();
				String nameFileFromZip = name.split(".zip")[0];
				File targetFile = new File( targetFolder , nameFileFromZip);
				if (!fileFromFolder.isDirectory()) {
					FileInputStream inputStream = new FileInputStream(fileFromFolder);
					copyFile(targetFile, inputStream);
				} else {
					if (!targetFile.exists()) {
						targetFile.mkdir();
					}
					copyFileFromFolder(targetFile, fileFromFolder);
				}
			}
		}
	}

	@Override
	public void importZip(String fileName, InputStream inputStream, boolean overwrite) throws EntityDoesNotExistsException {
		super.importZip(fileName, inputStream, overwrite);
	}

	private void copyFile(File targetFile, InputStream in) {
		try {

			// write the inputStream to a FileOutputStream
			OutputStream out = new FileOutputStream(targetFile);

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}

			in.close();
			out.flush();
			out.close();

			log.debug("New file created!");
		} catch (Exception e) {
			log.error("Failed saving file. ", e);
		}
	}

	/**
	 * Compress module and its files into byte array.
	 *
	 * @param exportFile   file to export
	 * @param meveoModules list of meveo modules
	 * @return zip file as byte array
	 * @throws Exception exception.
	 */
	public byte[] createZipFile(String exportFile, List<MeveoModule> meveoModules) throws Exception {

		Logger log = LoggerFactory.getLogger(FileUtils.class);
		log.info("Creating zip file for {}", exportFile);
		try(
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			CheckedOutputStream cos = new CheckedOutputStream(baos, new CRC32());
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(cos))) {

			// Add modules defintion file
			File sourceFile = new File(exportFile);
			addToZipFile(sourceFile, zos, null);

			// Add files contained in modules
			for (MeveoModule meveoModule : meveoModules) {
				for (String pathFile : meveoModule.getModuleFiles()) {
					String path = pathFile.startsWith("/") ? pathFile.substring(1) : pathFile;
					int lastIndexOf = path.lastIndexOf("/");
					
					// Handle windows-like paths
					if(lastIndexOf == -1) {
						path = pathFile.startsWith("\\") ? pathFile.substring(1) : pathFile;
						lastIndexOf = path.lastIndexOf("\\");
					}
					
					String baseDir = lastIndexOf > -1 ? path.substring(0, lastIndexOf) : null;
					String chrootDir = paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
					File file = new File(chrootDir, pathFile);
					if (!file.exists()) {
						log.error("File does not exists {}", file);
						continue;
					}

					if (file.isDirectory()) {
						addDirectoryToZip(file, zos, baseDir);
					} else {
						addToZipFile(file, zos, baseDir);
					}
				}
			}

			zos.flush();
			zos.close();
			return baos.toByteArray();

		} 
	}

	public File exportModules(List<String> modulesCode, ExportFormat exportFormat) throws Exception {

		List<MeveoModule> meveoModules = new ArrayList<>();
		if (modulesCode != null) {
			for (String code : modulesCode) {
				MeveoModule meveoModule = meveoModuleService.findByCode(code);
				if (meveoModule != null) {
					meveoModules.add(meveoModule);
				}
			}
		}

		File exportFile = exportEntities(exportFormat, meveoModules);
		OutputStream opStream = null;
		File fileZip = null;
		String exportName = exportFile.getName();
		String[] data = exportName.split("\\.");
		String fileName = data[0];

		// Write data as zip if one module contains files
		boolean hasFiles = meveoModules.stream().anyMatch(module -> CollectionUtils.isNotEmpty(module.getModuleFiles()));
		if (hasFiles) {
			byte[] filedata = createZipFile(exportFile.getAbsolutePath(), meveoModules);
			fileZip = new File(fileName + ".zip");
			opStream = new FileOutputStream(fileZip);
			opStream.write(filedata);
		}

		if (fileZip != null) {
			return fileZip;
			/*
			 * is = new FileInputStream(fileZip); httpServletResponse.setContentType(Files.probeContentType(fileZip.toPath()));
			 */
		} else {
			return exportFile;
			/*
			 * is = new FileInputStream(exportFile); httpServletResponse.setContentType(Files.probeContentType(exportFile.toPath()));
			 */
		}
		/*
		 * httpServletResponse.addHeader("Content-disposition", "attachment;filename=\"" + fileName + "\""); IOUtils.copy(is,
		 * httpServletResponse.getOutputStream()); httpServletResponse.flushBuffer();
		 */
	}

	@Override
	public File exportDtos(ExportFormat format, List<MeveoModuleDto> dtos) throws IOException {
		if (format == null) {
			throw new IllegalArgumentException("Format must be provided");
		}

		File exportFile = null;
		if (dtos.size() > 1) {
			exportFile = new File("MeveoModule_" + dtos.size() + "_" + System.currentTimeMillis() + "." + format.getFormat());
		} else {
			MeveoModuleDto meveoModuleDto = dtos.get(0);
			exportFile = new File(meveoModuleDto.getCode() + "_version-" + meveoModuleDto.getCurrentVersion().replace(".", "_") + "." + format.getFormat());
		}

		switch (format) {

			case JSON:
				new ObjectMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT)
						.writeValue(exportFile, dtos);
				break;

			case XML:
				new XmlMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT)
						.writeValue(exportFile, dtos);
				break;

			case CSV:
				CsvMapper csvMapper = (CsvMapper) new CsvMapper()
						.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
						.enable(SerializationFeature.INDENT_OUTPUT);

				CsvSchema schema = csvMapper.schemaFor(MeveoModuleDto.class).withColumnSeparator(';');
				ObjectWriter myObjectWriter = csvMapper.writer(schema);
				myObjectWriter.writeValue(exportFile, dtos);
				break;
		}

		return exportFile;
	}

	public void release(String moduleCode, String nextVersion) throws MeveoApiException, BusinessException {
		MeveoModule module = meveoModuleService.findByCode(moduleCode);

		if (module == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, moduleCode);
		}
		Integer version = Integer.parseInt(nextVersion.replace(".", ""));
		Integer versionModule = Integer.parseInt(module.getCurrentVersion().replace(".", ""));
		if (version > versionModule) {
			if (module.getScript() != null) {
				boolean checkRelease = meveoModuleService.checkTestSuites(module.getScript().getCode());
				if (!checkRelease) {
					throw new ValidationException("There some test suits failed", "meveoModule.checkTestSuitsReleaseFailed");
				}
			}
			meveoModuleService.releaseModule(module, nextVersion);
		} else {
			throw new ValidationException("Failed to release module. Next version is less than the current version " + module.getCurrentVersion());
		}
	}

	private void checkModuleDependencies(List<MeveoModuleDto> modules) throws EntityDoesNotExistsException {
		List<MeveoModule> meveoModules = meveoModuleService.list();
		List<String> moduleCode = new ArrayList<>();
		if (!meveoModules.isEmpty()) {
			for (MeveoModule meveoModule : meveoModules) {
				moduleCode.add(meveoModule.getCode());
			}
		}
		for (MeveoModuleDto meveoModuleDto : modules) {
			if (meveoModuleDto.getModuleDependencies() != null && !meveoModuleDto.getModuleDependencies().isEmpty()) {
				if (!moduleCode.isEmpty()) {
					for (ModuleDependencyDto dependencyDto : meveoModuleDto.getModuleDependencies()) {
						if (!moduleCode.contains(dependencyDto.getCode())) {
							throw new EntityDoesNotExistsException("The module file cannot be imported because module dependency "+ dependencyDto.getCode() +" doesn't exists locally.");
						} else {
							MeveoModule meveoModule = meveoModuleService.findByCode(dependencyDto.getCode());
							List<String> versions = new ArrayList<>();
							versions.add(meveoModule.getCurrentVersion());
							if (!meveoModule.getReleases().isEmpty()) {
								for (ModuleRelease moduleRelease : meveoModule.getReleases()) {
									versions.add(moduleRelease.getCurrentVersion());
								}
							}
							if (!versions.contains(dependencyDto.getCurrentVersion())) {
								throw new EntityDoesNotExistsException("The module file cannot be imported because module dependency "+ dependencyDto.getCode() + " with version " + dependencyDto.getCurrentVersion() +" doesn't exists locally.");
							}
						}
					}
				} else {
					throw new EntityDoesNotExistsException("The module file cannot be imported because module dependency doesn't exists locally.");
				}
			}
		}
	}

	@Override
	public void remove(MeveoModuleDto dto) throws MeveoApiException, BusinessException {
		uninstall(dto.getCode(), MeveoModule.class, true);
	}
}