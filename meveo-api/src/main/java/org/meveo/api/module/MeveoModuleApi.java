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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipOutputStream;

import javax.ejb.EJB;
import javax.ejb.EJBTransactionRolledbackException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;
import org.meveo.admin.exception.BusinessEntityException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.admin.util.ModuleUtil;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.ApiService;
import org.meveo.api.ApiUtils;
import org.meveo.api.ApiVersionedService;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.CustomEntityInstanceApi;
import org.meveo.api.CustomFieldTemplateApi;
import org.meveo.api.EntityCustomActionApi;
import org.meveo.api.ScriptInstanceApi;
import org.meveo.api.admin.FilesApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomEntityInstanceDto;
import org.meveo.api.dto.CustomFieldTemplateDto;
import org.meveo.api.dto.EntityCustomActionDto;
import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.api.dto.git.GitRepositoryDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.MeveoModuleItemDto;
import org.meveo.api.dto.module.ModuleDependencyDto;
import org.meveo.api.dto.module.ModuleReleaseDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingModuleException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.api.exceptions.ModuleInstallFail;
import org.meveo.api.export.ExportFormat;
import org.meveo.api.git.GitRepositoryApi;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.git.CommitEvent;
import org.meveo.event.qualifier.git.CommitReceived;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.ModuleItem;
import org.meveo.model.VersionedEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.module.ModuleRelease;
import org.meveo.model.module.ModuleReleaseItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.persistence.sql.SqlConfigurationService;
import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.MeveoModuleUtils;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.service.admin.impl.ModuleUninstall;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.GitRepositoryService;
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
    
    @Inject
    private CustomEntityInstanceApi ceiApi;
    
    @Inject
    private SqlConfigurationService sqlConfigurationService;
    
    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;
    
    @Inject
    private ModuleInstallationContext moduleCtx;
    
    @Inject
    private GitClient gitClient;
    
    @Inject
    private GitRepositoryApi gitRepositoryApi;
    
    @Inject
    private GitRepositoryService gitRepositoryService;
    
	public MeveoModuleApi() {
		super(MeveoModule.class, MeveoModuleDto.class);
		if (!initalized) {
			registerModulePackage("org.meveo.model");
			initalized = true;
		}
	}

	public ModuleInstallResult install(List<String> repositories, GitRepository repo) throws BusinessException, MeveoApiException {
		
		ModuleInstallResult result = null;
		
		File repoDir = GitHelper.getRepositoryDir(null, repo.getCode());
		
		MeveoModuleDto moduleDto = parseModuleJsonFile(repo, repoDir);
		moduleDto = buildMeveoModuleFromDirectory(repoDir, moduleDto);
		
		result = install(repositories, moduleDto, OnDuplicate.SKIP);
		
		// Copy module files to file explorer
		try {
			importFileFromModule(List.of(moduleDto), GitHelper.getRepositoryDir(currentUser, moduleDto.getCode()));
		} catch (FileNotFoundException e) {
			throw new BusinessException("Failed to copy module files", e);
		}
		
		return result;
	}
	
	public void installData(MeveoModule module, Repository repository) throws BusinessException {
		List<CustomModelObject> templates = sqlConfigurationService.initializeModuleDatabase(module.getCode(), repository.getSqlConfigurationCode());
		for (var template : templates) {
			meveoModuleService.getEntityManager().refresh(template);
			template.getRepositories().add(repository);
			if (template instanceof CustomEntityTemplate) {
				customEntityTemplateService.update((CustomEntityTemplate) template);
			} else {
				customRelationshipTemplateService.update((CustomRelationshipTemplate) template);
			}
		}
		
		// TODO: Insert the CEIs using cross storage api
		
		module.getRepositories().add(repository);
		meveoModuleService.update(module);
	}
	
	/**
	 * @param repositories Code of the repositories where to install the module data
	 * @param moduleDto	 Serialiazed module
	 * @param onDuplicate Action to realize on execution
	 * @return the installation summary
	 */
	public ModuleInstallResult install(List<String> repositories, MeveoModuleDto moduleDto, OnDuplicate onDuplicate) throws MeveoApiException, BusinessException {
		MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(moduleDto.getCode());
		
		List<ModuleDependencyDto> missingModules = checkModuleDependencies(moduleDto);
		if (!missingModules.isEmpty()) {
			throw new MissingModuleException(missingModules);
		}
		
		List<Repository> storageRepositories = new ArrayList<>();
		if (repositories == null || repositories.isEmpty()) {
			storageRepositories.add(repositoryService.findDefaultRepository());
		} else {
			repositories.forEach(repository -> {
				var storageRepo = repositoryService.findByCode(repository);
				if (storageRepo != null) {
					storageRepositories.add(storageRepo);
				} else {
					throw new IllegalArgumentException("Can't install module " + moduleDto.getCode() + " on non-existant repositories" + repository);
				}
			});
		}

		meveoModule = meveoModuleApi.createOrUpdate(moduleDto);
		
		// Update installed repositories
		meveoModule.setRepositories(storageRepositories);
		
		try {
			var installResult =  meveoModuleItemInstaller.install(meveoModule, moduleDto, onDuplicate);
			
			return installResult;
		} catch (ModuleInstallFail e) {
    		throw e.getException();
		}
	}
	
	public MeveoModuleDto parseModuleJsonFile(GitRepository repo, File repoDir) throws BusinessException {
		MeveoModuleDto moduleDto;
		try {
			moduleDto = JacksonUtil.read(new File(repoDir, "module.json"), MeveoModuleDto.class);
			moduleDto.setCode(repo.getCode());
		} catch (IOException e1) {
			throw new BusinessException("Can't read module descriptor", e1);
		}
		return moduleDto;
	}
	
	public LinkedHashMap<GitRepository, ModuleDependencyDto> retrieveModuleDependencies(List<ModuleDependencyDto> dependencies, String username, String password) throws MeveoApiException, BusinessException {
		LinkedHashMap<GitRepository, ModuleDependencyDto> result = new LinkedHashMap<>();
		for (ModuleDependencyDto dependency : dependencies) {
			GitRepositoryDto gitRepositoryDto = new GitRepositoryDto();
			gitRepositoryDto.setCode(dependency.getCode());
			gitRepositoryDto.setDefaultBranch(dependency.getGitBranch());
			gitRepositoryDto.setRemoteOrigin(dependency.getGitUrl());

			GitRepository repo = gitRepositoryService.findByCode(gitRepositoryDto.getCode());
			if (repo == null) {
				repo = gitRepositoryApi.create(gitRepositoryDto, false, username, password);
			}
			
			if (!result.containsKey(repo)) {
				File repoDir = GitHelper.getRepositoryDir(null, repo.getCode());
				MeveoModuleDto moduleDto = parseModuleJsonFile(repo, repoDir);
				List<ModuleDependencyDto> missingDependencies = checkModuleDependencies(moduleDto);
				result.putAll(retrieveModuleDependencies(missingDependencies, username, password));
				
				result.put(repo, dependency);
			}
		}
		
		return result;
	}

	@SuppressWarnings("rawtypes")
	private MeveoModuleDto buildMeveoModuleFromDirectory(File repoDir, MeveoModuleDto moduleDto) throws BusinessException, MeveoApiException {
		Map<String, String> entityDtoNamebyPath = getEntitiesPathsMapping();
		
		for (File directory : repoDir.listFiles()) {
			if (!directory.isDirectory()) {
				continue;
			}
			String directoryName = directory.getName();
			String dtoClassName = entityDtoNamebyPath.get(directoryName);
			if (dtoClassName == null) {
				continue;
			}
			
			//TODO: Custom action special case
			if(directoryName.equals("entityCustomActions")) {
				entityCustomActionApi.readEcas(directory)
					.stream()
					.map(ecaDto -> new MeveoModuleItemDto(ecaDto.getClass().getName(), ecaDto))
					.forEach(moduleDto.getModuleItems()::add);
				
			} else if (directoryName.equals("customFieldTemplates")) {
				customFieldTemplateApi.readCfts(directory)
					.stream()
					.map(cftDto -> new MeveoModuleItemDto(CustomFieldTemplateDto.class.getName(), cftDto))
					.forEach(moduleDto.getModuleItems()::add);
			
			} else if (directoryName.equals("customEntityInstances")) {
				ceiApi.readCeis(directory)
					.stream()
					.map(ceiDto ->  new MeveoModuleItemDto(CustomEntityInstanceDto.class.getName(), ceiDto))
					.forEach(moduleDto.getModuleItems()::add);

			}  else {
				
				// Retrieve API corresponding to class
				var api = ApiUtils.getApiService(getItemClassByPath(directoryName), false);
				if (api instanceof BaseCrudApi) {
					BaseCrudApi<?,?> baseCrudApi = (BaseCrudApi) api;
					List<MeveoModuleItemDto> items = baseCrudApi.readModuleItems(directory, dtoClassName);
					moduleDto.getModuleItems().addAll(items);
				} else {
					log.warn("Can't install item of type {} : api is not a BaseCrudApi", dtoClassName);
				}

			}
		}
		
		// Parse installation script from items
		if (moduleDto.getScript() != null && StringUtils.isBlank(moduleDto.getScript().getScript())) {
			moduleDto.getModuleItems().stream()
				.filter(item -> item.getDtoClassName().equals(ScriptInstanceDto.class.getName()))
				.map(item -> JacksonUtil.convert(item.getDtoData(), ScriptInstanceDto.class))
				.filter(scriptDto -> scriptDto.getCode().equals(moduleDto.getScript().getCode()))
				.findFirst()
				.ifPresent(moduleDto::setScript);
		}
		
		return moduleDto;
	}
	
	private Class<?> getItemClassByPath(String directoryName) {
		return MeveoModuleItemInstaller.MODULE_ITEM_TYPES.values()
				.stream()
				.filter(itemType -> itemType.getAnnotation(ModuleItem.class).path().equals(directoryName))
				.findFirst()
				.orElse(null);
	}
	
	private String getItemTypeByPath(String directoryName) {
		return MeveoModuleItemInstaller.MODULE_ITEM_TYPES.values()
				.stream()
				.map(itemType -> itemType.getAnnotation(ModuleItem.class))
				.filter(itemType -> itemType.path().equals(directoryName))
				.map(ModuleItem::value)
				.findFirst()
				.orElse(null);
	}
	
	private static Stream<BaseCrudApi> baseCrudApis() {
		return MeveoModuleItemInstaller.MODULE_ITEM_TYPES.values()
			.stream()
			.map(clazz -> ApiUtils.getApiService(clazz, false))
			.map(BaseCrudApi.class::cast);
	}

	protected Map<String, String> getEntitiesPathsMapping() {
		Map<String, String> entityDtoNamebyPath = new HashMap<String, String>();
		
		MeveoModuleItemInstaller.MODULE_ITEM_TYPES.values().forEach(clazz -> {
			ModuleItem item = clazz.getAnnotation(ModuleItem.class);
			try {
				if (clazz == CustomFieldTemplate.class) {
					entityDtoNamebyPath.put(item.path(), CustomFieldTemplateDto.class.getName());
				} else if (clazz == EntityCustomAction.class) {
					entityDtoNamebyPath.put(item.path(), EntityCustomActionDto.class.getName());
				} else {
					BaseCrudApi api = (BaseCrudApi)ApiUtils.getApiService(clazz, false);
					if (api != null) {
						entityDtoNamebyPath.put(item.path(), api.getDtoClass().getName());
					}
				}
			} catch (Exception e) {
				log.error("Can't retrieve dto class for {}",clazz.getName(), e);
			}
		});
		return entityDtoNamebyPath;
	}
	
	public MeveoModuleItem getExistingItemFromFile(File directory, String fileName) {
		if (fileName.endsWith(".json")) {
			String[] paths = fileName.split("/");
			String directoryName = paths[0];
			String itemCode = FilenameUtils.getBaseName(fileName);
			
			Class<?> itemClass = getItemClassByPath(directoryName);
			if (itemClass == null) {
				return null;
			}
			
			String appliesTo = null;
			if (paths.length > 2) {
				appliesTo = paths[1];
			}
			
			return meveoModuleService.findModuleItem(itemCode, itemClass.getName(), appliesTo)
					.stream()
					.findFirst()
					.orElse(null);
		} else {
			File entityFile = new File(directory, fileName);
			
			AtomicReference<MeveoModuleItem> item = new AtomicReference<>();
			baseCrudApis().takeWhile(i -> item.get() == null)
				.filter(Objects::nonNull)
				.forEach(api -> {
					if (item.get() == null) {
						item.set(api.getExistingItem(entityFile));
					}
				});
			return item.get();
		}
	}
	
	public MeveoModuleItemDto getItemDtoFromFile(File directory, String fileName, Set<MeveoModuleItemDto> alreadyParseItems, String gitRepo) {
		
		String[] paths = fileName.split("/");
		String directoryName = paths[0];
		
		File entityFile = new File(directory, fileName);
		AtomicReference<MeveoModuleItemDto> item = new AtomicReference<>();
		
		baseCrudApis().takeWhile(i -> item.get() == null)
			.filter(Objects::nonNull)
			.forEach(api -> {
				if (item.get() == null) {
					item.set(api.parseModuleItem(entityFile, directoryName, alreadyParseItems, gitRepo));
				}
			});
		
		return item.get();
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
		MeveoModule meveoModuleBackup = meveoModuleService.findByCodeWithFetchEntities(moduleDto.getCode());
		MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(moduleDto.getCode());
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

		MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(code);
		if (meveoModule == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, code);
		}
		String logoPicture = meveoModule.getLogoPicture();
		
		if (meveoModule.isInstalled()) {
			try {
				uninstall(MeveoModule.class, ModuleUninstall.of(meveoModule));
			} catch (MeveoApiException e) {
				throw new BusinessException(e);
			}
		}
		
		meveoModuleService.remove(meveoModule);
		removeModulePicture(logoPicture);

	}

	public void delete(String code, ModuleUninstall moduleUninstall) throws EntityDoesNotExistsException, BusinessException, IOException {

		MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(code);
		
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
		
		if (meveoModule.isInstalled()) {
			try {
				uninstall(MeveoModule.class, moduleUninstall.withModule(meveoModule));
			} catch (MeveoApiException e) {
				throw new BusinessException(e);
			}
		}
		
		meveoModuleService.remove(meveoModule);
		if (CollectionUtils.isNotEmpty(moduleFiles) && moduleUninstall.removeFiles()) {
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
		MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(postData.getCode());
		if (meveoModule == null) {
			// create
			return create(postData, false);
			
		} else {
			// update
			return update(postData);
		}
	}

	public List<MeveoModule> uninstall(Class<? extends MeveoModule> moduleClass, ModuleUninstall uninstall) throws MeveoApiException, BusinessException {
		String code = uninstall.moduleCode();
		if (StringUtils.isBlank(code)) {
			missingParameters.add("code");
			handleMissingParameters();
		}

		if (moduleClass == null) {
			moduleClass = MeveoModule.class;
		}

		MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(code);
		if (meveoModule == null) {
			throw new EntityDoesNotExistsException(moduleClass, code);
		}

		if (!meveoModule.isInstalled()) {
			throw new ActionForbiddenException(meveoModule.getClass(), code, "uninstall", "Module is not installed or already enabled");
		}
		
		if(meveoModuleService.isDependencyOfOtherModule(meveoModule)) {
			throw new BusinessException("Unable to uninstall a referenced module.");
		}
		
		List<MeveoModule> uninstalledModules = new ArrayList<>();
		
		RevCommit headCommitBefore = null;
		try {
			headCommitBefore = gitClient.getHeadCommit(meveoModule.getGitRepository());
		} catch (BusinessException e) {
			log.error("Failed to retrieve head commit", e);
		}
		
		try {
			MeveoModule uninstalledModule = meveoModuleItemInstaller.uninstall(uninstall.withModule(meveoModule));
			uninstalledModules.add(uninstalledModule);
			if (uninstall.withDependencies()) {
				for (var dependency : uninstalledModule.getModuleDependencies()) {
					MeveoModule moduleDependency = meveoModuleService.findByCode(dependency.getCode());
					if (!meveoModuleService.isDependencyOfOtherModule(moduleDependency)) {
						ModuleUninstall options = ModuleUninstall.builder(uninstall)
								.module(moduleDependency)
								.build();
						uninstalledModules.addAll(uninstall(MeveoModule.class, options));
					}
				}
			}
			return uninstalledModules;
		} finally {
			if (headCommitBefore != null) {
				gitClient.reset(meveoModule.getGitRepository(), headCommitBefore);
			}
		}

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

		MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(code);
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

		if (module.isDownloaded() && !module.isInstalled() && module.getModuleSource() != null) {
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
		
		Set<MeveoModuleItem> moduleItems = module.getModuleItems();
		if (moduleItems != null) {
			for (MeveoModuleItem item : moduleItems) {

				try {
					BaseEntityDto itemDto = getEntityDto(moduleItems, item);
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
		
		Stream.ofNullable(module.getModuleDependencies())
			.flatMap(Collection::stream)
			.map(MeveoModuleDependency::getCode)
			.map(code -> meveoModuleService.findByCode(code, List.of("gitRepository")))
			.forEach(moduleDto::addDependency);

		return moduleDto;
	}

	/**
	 * @param moduleItems
	 * @param item
	 * @return
	 * @throws MissingParameterException
	 * @throws InvalidParameterException
	 * @throws ClassNotFoundException
	 * @throws MeveoApiException
	 * @throws EntityDoesNotExistsException
	 */
	protected BaseEntityDto getEntityDto(Set<MeveoModuleItem> moduleItems, MeveoModuleItem item) throws MissingParameterException, InvalidParameterException, ClassNotFoundException, MeveoApiException, org.meveo.exceptions.EntityDoesNotExistsException {
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
		return itemDto;
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
		} catch (BusinessException e) {
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
		final MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(code);
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

		meveoModuleService.update(module);

		return toDto(module);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MeveoModuleDto removeFromModule(String code, String itemCode, String itemType, String appliesTo) throws EntityDoesNotExistsException, BusinessException {
		final MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(code);
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
		module.removeItem(moduleItem);
		meveoModuleService.update(module);
		return toDto(module);
	}

	public MeveoModuleDto addFileToModule(String code, String path) throws EntityDoesNotExistsException, BusinessException {
		MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(code);
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
		final MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(code);
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
		MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(moduleCode);

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
			// Import files contained in each modules
			if(!getFileImport().isEmpty()) {
				File parentDir = getFileImport().iterator().next().getParentFile();
				importFileFromModule(modules, parentDir);
			}

			importEntities(modules, overwrite);
		} catch (EntityDoesNotExistsException e) {
			throw new EntityDoesNotExistsException(e.getMessage());
		}
	}

	public void importJSON(List<MeveoModuleDto> modules, boolean overwrite) throws BusinessException, IOException, MeveoApiException {

		// Import files contained in each modules
		if(!getFileImport().isEmpty()) {
			File parentDir = getFileImport().iterator().next().getParentFile();
			importFileFromModule(modules, parentDir);
		}

		importEntities(modules, overwrite);

		for (MeveoModuleDto moduleDto : modules) {
			if (!moduleDto.isInDraft()) {
				MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(moduleDto.getCode());
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

	public void importFileFromModule(List<MeveoModuleDto> modules, File parentDir) throws FileNotFoundException {

		for (MeveoModuleDto module : modules) {
			List<String> moduleFiles = module.getModuleFiles();
			if (moduleFiles == null) {
				continue;
			}

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
				MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(code);
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

	private List<ModuleDependencyDto> checkModuleDependencies(MeveoModuleDto meveoModuleDto) {
		List<MeveoModule> meveoModules = meveoModuleService.list();
		List<String> modulesCodes = new ArrayList<>();
		List<ModuleDependencyDto> missingModules = new ArrayList<>();
		
		if (!meveoModules.isEmpty()) {
			for (MeveoModule meveoModule : meveoModules) {
				if (meveoModule.isInstalled()) {
					modulesCodes.add(meveoModule.getCode());
				}
			}
		}
		
		if (meveoModuleDto.getModuleDependencies() != null && !meveoModuleDto.getModuleDependencies().isEmpty()) {
			for (ModuleDependencyDto dependencyDto : meveoModuleDto.getModuleDependencies()) {
				if (!modulesCodes.contains(dependencyDto.getCode())) {
					missingModules.add(dependencyDto);
				} else {
					MeveoModule meveoModule = meveoModuleService.findByCode(dependencyDto.getCode(),Arrays.asList("releases"));
					List<String> versions = new ArrayList<>();
					versions.add(meveoModule.getCurrentVersion());
					
					if (!meveoModule.getReleases().isEmpty()) {
						for (ModuleRelease moduleRelease : meveoModule.getReleases()) {
							versions.add(moduleRelease.getCurrentVersion());
						}
					}
					
					if (!versions.contains(dependencyDto.getCurrentVersion())) {
						missingModules.add(dependencyDto);
					}
				}
			}
		}
		
		return missingModules;
	}
	

	@Override
	public void remove(MeveoModuleDto dto) throws MeveoApiException, BusinessException {
		uninstall(MeveoModule.class, ModuleUninstall.of(dto.getCode()));
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void updateModuleOnCommitReceived(@Observes @CommitReceived CommitEvent event) throws IllegalArgumentException, MeveoApiException, BusinessException, Exception {
		// Make sure the git repo is linked to a module
		MeveoModule module = this.meveoModuleService.findByCodeWithFetchEntities(event.getGitRepository().getCode());
		if (module == null) {
			return;
		}
		
		Set<MeveoModuleItemDto> installItems = new HashSet<>();
		Set<MeveoModuleItemDto> updateItems = new HashSet<>();
		Set<MeveoModuleItem> deleteItems = new HashSet<>();
		
		File directory = GitHelper.getRepositoryDir(null, module.getCode());
		
		for (DiffEntry diff : event.getDiffs()) {

			switch (diff.getChangeType()) {
			case ADD:
				installItems.removeIf(Objects::isNull);
				installItems.add(getItemDtoFromFile(directory, diff.getNewPath(), installItems, module.getCode()));
				break;
				
			case COPY:
				//NOOP
				break;
				
			case DELETE:
				deleteItems.add(getExistingItemFromFile(directory, diff.getOldPath()));
				break;
				
			case MODIFY:
				updateItems.removeIf(Objects::isNull);
				updateItems.add(getItemDtoFromFile(directory, diff.getNewPath(), updateItems, module.getCode()));
				break;
				
			case RENAME:
				installItems.removeIf(Objects::isNull);
				deleteItems.add(getExistingItemFromFile(directory, diff.getOldPath()));
				installItems.add(getItemDtoFromFile(directory, diff.getNewPath(), installItems, module.getCode()));
				break;
				
			default:
				break;
				
			}
		}
		
		installItems.removeIf(Objects::isNull);
		deleteItems.removeIf(Objects::isNull);
		updateItems.removeIf(Objects::isNull);
		
		moduleCtx.begin(module);
		moduleCtx.setRepositories(module.getRepositories());
		
		// Start with removing items
		ModuleUninstall options = ModuleUninstall.builder()
				.module(module)
				.removeData(true)
				.removeItems(true)
				.removeFiles(true)
				.build();
		
		// Add new items
		for (var itemDto : meveoModuleItemInstaller.getSortedModuleItems(installItems)) {
			meveoModuleItemInstaller.unpackAndInstallModuleItem(module, itemDto, OnDuplicate.FAIL);
		}
		
		// Update existing items
		for (var itemDto : meveoModuleItemInstaller.getSortedModuleItems(updateItems)) {
			meveoModuleItemInstaller.unpackAndInstallModuleItem(module, itemDto, OnDuplicate.OVERWRITE);
		}
		
		for(var item : meveoModuleService.getSortedModuleItemsForUninstall(deleteItems)) {
			module.removeItem(item);
			meveoModuleItemInstaller.uninstallItem(options, null, item);
		}
		
		moduleCtx.end();
		
		meveoModuleService.update(module);
	}
	
	public void deleteModuleBeforeRepository(@Observes @Removed GitRepository repository) throws MeveoApiException, BusinessException {
		MeveoModule module = meveoModuleService.findByCode(repository.getCode());
		if (module != null) {
			ModuleUninstall uninstallParams = ModuleUninstall.builder()
					.module(module)
					.removeData(true)
					.removeFiles(true)
					.removeItems(true)
					.withDependencies(false)
					.build();
					
			for (MeveoModule uninstalledModule : uninstall(module.getClass(), uninstallParams)) {
				meveoModuleService.remove(uninstalledModule);
			}
			
		}
	}
}