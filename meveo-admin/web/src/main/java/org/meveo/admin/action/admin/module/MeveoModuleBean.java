/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
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
package org.meveo.admin.action.admin.module;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.action.admin.ViewBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.ModuleDependencyDto;
import org.meveo.api.dto.module.ModuleReleaseDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.export.ExportFormat;
import org.meveo.api.module.MeveoModuleApi;
import org.meveo.api.module.MeveoModulePatchApi;
import org.meveo.api.module.ModuleReleaseApi;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModulePatch;
import org.meveo.model.module.ModuleRelease;
import org.meveo.model.storage.Repository;
import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.MeveoModulePatchService;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.util.view.MessagesHelper;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Meveo module bean
 *
 * @author Tyshan Shi(tyshan@manaty.net)
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Named
@ViewScoped
@ViewBean
public class MeveoModuleBean extends GenericModuleBean<MeveoModule> {

	private static final long serialVersionUID = 1L;

	@Inject
	private MeveoModuleApi meveoModuleApi;
	
	@Inject
	private MeveoModuleService meveoModuleService;

	@Inject
	private ModuleReleaseApi moduleReleaseApi;

	@Inject
	private MeveoModulePatchService meveoModulePatchService;

	@Inject
	private MeveoModulePatchApi meveoModulePatchApi;
	
	@Inject
	private ScriptInstanceService scriptService;

	private String moduleCode;
	private String releaseVersion;
	private List<MeveoModule> meveoModules;
	private ModuleRelease moduleReleaseExport;
	private transient UploadedFile patchFile;
	private transient MeveoModulePatch newMeveoModulePatch;
	private List<File> fileList;
	private String selectedFolder;
	private String selectedFileName;
	private boolean currentDirEmpty;
	private boolean removeDataSchema;
	private String selectedRepository;

	/**
	 * Constructor. Invokes super constructor and provides class type of this bean
	 * for {@link BaseBean}.
	 */
	public MeveoModuleBean() {
		super(MeveoModule.class);
	}
	
	
	/**
	 * @return the {@link #selectedRepository}
	 */
	public String getSelectedRepository() {
		return selectedRepository;
	}

	/**
	 * @param selectedRepository the selectedRepository to set
	 */
	public void setSelectedRepository(String selectedRepository) {
		this.selectedRepository = selectedRepository;
	}

	/**
	 * @return the {@link #removeDataSchema}
	 */
	public boolean isRemoveDataSchema() {
		return removeDataSchema;
	}

	/**
	 * @param removeDataSchema the removeDataSchema to set
	 */
	public void setRemoveDataSchema(boolean removeDataSchema) {
		this.removeDataSchema = removeDataSchema;
	}



	@Override
	public BaseCrudApi<MeveoModule, MeveoModuleDto> getBaseCrudApi() {
		return meveoModuleApi;
	}

	/**
	 * @see BaseBean#getPersistenceService()
	 */
	@Override
	protected IPersistenceService<MeveoModule> getPersistenceService() {
		return meveoModuleService;
	}

	/**
	 * initialize Modules
	 */
	public void initializeModules() {
		MeveoModuleFilters filters = new MeveoModuleFilters();
		filters.setActive(true);
		filters.setCode(moduleCode);
		meveoModules = meveoModuleService.list(filters);
	}

	public String getModuleCode() {
		return moduleCode;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	public String getReleaseVersion() {
		return releaseVersion;
	}

	public void setReleaseVersion(String releaseVersion) {
		this.releaseVersion = releaseVersion;
	}

	public List<MeveoModule> getMeveoModules() {
		return meveoModules;
	}

	public void setMeveoModules(List<MeveoModule> meveoModules) {
		this.meveoModules = meveoModules;
	}

	public List<File> getFileList() {
		return fileList;
	}

	public void setFileList(List<File> fileList) {
		this.fileList = fileList;
	}

	public String getSelectedFolder() {
		return selectedFolder;
	}

	public void setSelectedFolder(String selectedFolder) {
		setSelectedFileName(null);
		if (selectedFolder == null) {
			this.selectedFolder = null;
		} else if ("..".equals(selectedFolder)) {
			if (this.selectedFolder.lastIndexOf(File.separator) > 0) {
				this.selectedFolder = this.selectedFolder.substring(0, this.selectedFolder.lastIndexOf(File.separator));
			} else {
				this.selectedFolder = null;
			}
		} else {
			if (this.selectedFolder == null) {
				this.selectedFolder = File.separator + selectedFolder;
			} else {
				this.selectedFolder += File.separator + selectedFolder;
			}
		}
		buildFileList();
	}

	private String getFilePath() {
		return paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());

	}

	private String getFilePath(String name) {
		String result = getFilePath() + File.separator + name;
		if (selectedFolder != null) {
			result = getFilePath() + File.separator + selectedFolder + File.separator + name;
		}
		return result;
	}

	private void buildFileList() {
		String folder = getFilePath() + File.separator + (this.selectedFolder == null ? "" : this.selectedFolder);
		File file = new File(folder);

		File[] files = file.listFiles();

		List<String> hiddenFolders = Arrays.asList("imports", "invoices", "jasper");

		fileList = files == null ? new ArrayList<>() : new ArrayList<>(Arrays.asList(files));
		fileList = fileList.stream().filter(e -> !e.isDirectory() || (e.isDirectory() && !hiddenFolders.contains(e.getName()))).collect(Collectors.toList());
		currentDirEmpty = !StringUtils.isBlank(this.selectedFolder) && fileList.isEmpty();
	}

	public boolean hasSelectedFolder() {
		return !StringUtils.isBlank(selectedFolder);
	}

	public String getSelectedFileName() {
		return selectedFileName;
	}

	public void setSelectedFileName(String selectedFileName) {
		this.selectedFileName = selectedFileName;
	}

	public boolean isCurrentDirEmpty() {
		return currentDirEmpty;
	}

	public void setCurrentDirEmpty(boolean currentDirEmpty) {
		this.currentDirEmpty = currentDirEmpty;
	}

	public String getFileType(String fileName) {
		if (fileName != null && fileName.endsWith(".zip")) {
			return "zip";
		}
		return "text";
	}

	/**
	 * Searching module by module code
	 */
	public void searchModules() {
		meveoModules = meveoModuleService.findLikeWithCode(moduleCode);
	}

//    public void fork() {
//    	install();
//    	entity.setModuleSource(null);
//    	init();
//    	initEntity();
//    }
	
	public DefaultStreamedContent exportJson() throws IOException {
		
		setExportFormat(ExportFormat.JSON);
		return export();
	}

	@Override
	public DefaultStreamedContent export() throws IOException {

		DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent();
		List<String> modulesCodes = getSelectedEntities().stream().map(MeveoModule::getCode).collect(Collectors.toList());

		try {
			File exportFile = meveoModuleApi.exportModules(modulesCodes, getExportFormat());
			defaultStreamedContent.setContentEncoding("UTF-8");
			defaultStreamedContent.setStream(new FileInputStream(exportFile));
			defaultStreamedContent.setName(exportFile.getName());
		} catch (Exception e) {
			log.error("Error exporting modules {}", modulesCodes);
		}

		return defaultStreamedContent;
	}

	public void releaseModule() {
		String version = this.getReleaseVersion();
		log.debug("release module {} on version {}", entity.getCode(), version);
		try {
			if (entity.getScript() != null) {
				boolean checkRelease = meveoModuleService.checkTestSuites(entity.getScript().getCode());
				if (!checkRelease) {
					messages.error(new BundleKey("messages", "meveoModule.checkTestSuitsReleaseFailed"));
					return;
				}
			}
			meveoModuleService.releaseModule(entity, version);
			meveoModuleService.flush();
			entity = meveoModuleService.findById(entity.getId(), getListFieldsToFetch());
			messages.info(new BundleKey("messages", "meveoModule.releaseSuccess"), entity.getCode(), version);
		} catch (Exception e) {
			log.error("Error when release module {} to {}", entity.getCode(), meveoInstance, e);
			messages.error(new BundleKey("messages", "meveoModule.releaseFailed"), entity.getCode(), version,
					(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
		}
		releaseVersion = null;
	}

	public void forkModule() {
		try {
			entity.setIsInDraft(true);
			meveoModuleService.update(entity);
			messages.info(new BundleKey("messages", "meveoModule.forkModule"), entity.getCode());
		} catch (Exception e) {
			log.error("Error when release module {} to {}", entity.getCode(), meveoInstance, e);
			messages.error(new BundleKey("messages", "meveoModule.forkModuleFailed"), entity.getCode(), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
		}
	}

	@ActionMethod
	public DefaultStreamedContent exportReleaseModule() throws BusinessException, IOException {
		if (meveoModuleApi == null) {
			throw new BusinessException(getClass().getSimpleName() + " is not using a base crud api");
		}

		DefaultStreamedContent defaultStreamedContent = new DefaultStreamedContent();
		List<ModuleRelease> moduleReleases = new ArrayList<>();
		moduleReleases.add(moduleReleaseExport);
		File exportFile = moduleReleaseApi.exportEntities(this.getExportFormat(), moduleReleases);

		if (CollectionUtils.isNotEmpty(moduleReleaseExport.getModuleFiles())) {
			try {
				String exportName = exportFile.getName();
				String[] moduleName = exportName.split("\\.");
				String fileName = moduleName[0];
				for (int i = 0; i < moduleReleases.size(); i++) {
					if (CollectionUtils.isNotEmpty(moduleReleases.get(i).getModuleFiles())) {
						byte[] filedata = moduleReleaseApi.createZipFile(exportFile.getAbsolutePath(), moduleReleases);
						InputStream is = new ByteArrayInputStream(filedata);
						return new DefaultStreamedContent(is, "application/octet-stream", fileName + ".zip");
					}
				}
			} catch (Exception e) {
				log.error("Error when create zip file {}", exportFile.getName());
			}
		} else {
			defaultStreamedContent.setContentEncoding("UTF-8");
			defaultStreamedContent.setStream(new FileInputStream(exportFile));
			defaultStreamedContent.setName(exportFile.getName());
		}

		entity = meveoModuleService.findById(entity.getId(), getListFieldsToFetch());
		return defaultStreamedContent;
	}

	public ModuleRelease getModuleReleaseExport() {
		return moduleReleaseExport;
	}

	public void setModuleReleaseExport(ModuleRelease moduleReleaseExport) {
		this.moduleReleaseExport = moduleReleaseExport;
	}

	public UploadedFile getPatchFile() {
		return patchFile;
	}

	public void setPatchFile(UploadedFile patchFile) {
		this.patchFile = patchFile;
	}

	@Override
	public List<String> getFormFieldsToFetch() {
		return meveoModuleService.getLazyLoadedProperties();
	}

	@Override
	protected List<String> getListFieldsToFetch() {
		return meveoModuleService.getLazyLoadedProperties();
	}

	@ActionMethod
	public void uploadAndApplyPatch() throws BusinessException {

		if (patchFile != null) {
			List<ModuleReleaseDto> moduleReleases;
			try {
				log.debug("Applying patch via uploaded file with name={}", patchFile.getFileName());
				moduleReleases = new ObjectMapper().configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true).enable(SerializationFeature.INDENT_OUTPUT)
						.readValue(patchFile.getContents(), new TypeReference<List<ModuleReleaseDto>>() {
						});

				meveoModulePatchApi.uploadAndApplyPatch(moduleReleases);
			} catch (IOException | MeveoApiException e) {
				throw new BusinessException("Patch application failed: " + e.getMessage());
			}
		}
	}

	/**
	 * Adds a patch to the selected module.
	 * 
	 * @throws BusinessException when adding of patch failed
	 */
	@ActionMethod
	public void addPatch() throws BusinessException {

		if (newMeveoModulePatch != null) {
			newMeveoModulePatch.getMeveoModulePatchId().setMeveoModule(entity);

			log.debug("Adding patch {}", newMeveoModulePatch);
			meveoModulePatchService.create(newMeveoModulePatch);

			messages.info(new BundleKey("messages", "meveoModule.patch.add.ok"));
		}

		initializePatch();
	}

	/**
	 * Initializes a patch object.
	 */
	public void initializePatch() {
		newMeveoModulePatch = new MeveoModulePatch();
	}

	/**
	 * Removes a selected patch.
	 * 
	 * @param e the patch to be remove
	 */
	public void removePatch(MeveoModulePatch e) {
		meveoModulePatchService.remove(e);
		entity.getPatches().remove(e);
	}

	@Override
	public void importData(FileUploadEvent event) throws IOException, BusinessException, MeveoApiException {

		String contentType = event.getFile().getContentType();
		InputStream inputStream = event.getFile().getInputstream();
		String fileName = event.getFile().getFileName();

		switch (contentType.trim()) {
			case "text/xml":
			case "application/xml":
				meveoModuleApi.importXML(inputStream, isOverride());
				break;

			case "application/json":
				try {
					List<MeveoModuleDto> meveoModuleDtos = meveoModuleApi.getModules(inputStream);
					if (CollectionUtils.isNotEmpty(meveoModuleDtos)) {
						boolean checkUpload = beforeUpload(meveoModuleDtos);
						if (!checkUpload) {
							return;
						}
					}
					meveoModuleApi.importJSON(meveoModuleDtos, isOverride());
				} catch (EntityDoesNotExistsException e) {
					messages.error(e.getMessage());
				}
				break;

			case "text/csv":
			case "application/vnd.ms-excel":
				meveoModuleApi.importCSV(inputStream, isOverride());
				break;

			case "application/octet-stream":
			case "application/x-zip-compressed":
				try {
					meveoModuleApi.importZip(fileName, inputStream, isOverride());
				} catch (EntityDoesNotExistsException e) {
					messages.error(e.getMessage());
				}
				break;
		}
	}

	public boolean beforeUpload(List<MeveoModuleDto> meveoModuleDtos) throws EntityDoesNotExistsException {

		for (MeveoModuleDto meveoModuleDto : meveoModuleDtos) {
			List<ModuleDependencyDto> dtos = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(meveoModuleDto.getModuleDependencies())) {
				for (ModuleDependencyDto dependencyDto : meveoModuleDto.getModuleDependencies()) {
					MeveoModule meveoModule = meveoModuleService.getMeveoModuleByVersionModule(dependencyDto.getCode(), dependencyDto.getCurrentVersion());
					if (meveoModule == null) {
						MeveoModule module = meveoModuleService.findByCode(dependencyDto.getCode(), Arrays.asList("releases"));
						Set<ModuleRelease> moduleReleases = module.getReleases();
						List<Integer> versionReleases = new ArrayList<>();
						for (ModuleRelease moduleRelease : moduleReleases) {
							Integer version = Integer.parseInt(moduleRelease.getCurrentVersion().replace(".", ""));
							versionReleases.add(version);
						}
						Integer versionModule = Integer.parseInt(dependencyDto.getCurrentVersion().replace(".", ""));
						Integer laterVersion = meveoModuleService.findLaterNearestVersion(versionReleases, versionModule);
						if (laterVersion != null) {
							String versionDependency = String.valueOf(laterVersion);
							String[] data = versionDependency.split("");
							String version = data[0] + "." + data[1] + "." + data[2];
							MeveoModule moduleDependency = meveoModuleService.getMeveoModuleByVersionModule(module.getCode(), version);
							if (moduleDependency.getScript() != null) {
								boolean checkTestResult = meveoModuleService.checkTestSuites(moduleDependency.getScript().getCode());
								if (!checkTestResult) {
									messages.error(new BundleKey("messages", "meveoModule.refuseTheUpload"), dependencyDto.getCode());
									return false;
								} else {
									ModuleDependencyDto dto = new ModuleDependencyDto(moduleDependency.getCode(), moduleDependency.getDescription(), version);
									dtos.add(dto);
								}
							} else {
								ModuleDependencyDto dto = new ModuleDependencyDto(moduleDependency.getCode(), moduleDependency.getDescription(), version);
								dtos.add(dto);
							}
						} else {
							Integer earlierVersion = meveoModuleService.findEarlierNearestVersion(versionReleases, versionModule);
							if (earlierVersion != null) {
								String versionDependency = String.valueOf(earlierVersion);
								String[] data = versionDependency.split("");
								String version = data[0] + "." + data[1] + "." + data[2];
								MeveoModule moduleDependency = meveoModuleService.getMeveoModuleByVersionModule(module.getCode(), version);
								if (moduleDependency.getScript() != null) {
									boolean checkTestResult = meveoModuleService.checkTestSuites(moduleDependency.getScript().getCode());
									if (!checkTestResult) {
										messages.error(new BundleKey("messages", "meveoModule.refuseTheUpload"), dependencyDto.getCode());
										return false;
									} else {
										ModuleDependencyDto dto = new ModuleDependencyDto(moduleDependency.getCode(), moduleDependency.getDescription(), version);
										dtos.add(dto);
									}
								} else {
									ModuleDependencyDto dto = new ModuleDependencyDto(moduleDependency.getCode(), moduleDependency.getDescription(), version);
									dtos.add(dto);
								}
							} else {
								messages.error(new BundleKey("messages", "meveoModule.refuseTheUpload"), dependencyDto.getCode());
								return false;
							}
						}
					} else {
						dtos.add(dependencyDto);
					}
				}
				meveoModuleDto.getModuleDependencies().clear();
				meveoModuleDto.getModuleDependencies().addAll(dtos);
			}
		}
		return true;
	}

	public void getFilesFromFileExplorer() {
		setSelectedFolder(null);
		buildFileList();
	}

	public void importFromInternalFile() {
		try {
			File file = new File(getFilePath(selectedFileName));
			InputStream inputStream = new FileInputStream(file);
			if (selectedFileName.endsWith(".json")) {
				meveoModuleApi.importJSON(inputStream, true);
			} else if (selectedFileName.endsWith(".zip")) {
				meveoModuleApi.importZip(selectedFileName, inputStream, true);
			}
			messages.info(new BundleKey("messages", "meveoModule.importFromFile.successful"), selectedFileName);
		} catch (Exception e) {
			messages.error(new BundleKey("messages", "meveoModule.importFromFile.error"), selectedFileName, e.getMessage());
		}
	}

	public MeveoModulePatch getNewMeveoModulePatch() {
		return newMeveoModulePatch;
	}

	public void setNewMeveoModulePatch(MeveoModulePatch newMeveoModulePatch) {
		this.newMeveoModulePatch = newMeveoModulePatch;
	}
	
	public void synchronizeLinkedItems() throws BusinessException, IOException {
		int nbItemsAdded = meveoModuleService.synchronizeLinkedItems(entity.getCode());
		log.info("synchronizeLinkedItems : {} items added to module {}", nbItemsAdded, entity.getCode());
	
        messages.info("Synchronization", nbItemsAdded + " items added. Reload to see changes.");

	}
	
	public List<String> availableRepositories() {
		List<Repository> availableRepos = listRepositories();
		if (entity.getRepositories() != null) {
			availableRepos.removeIf(entity.getRepositories()::contains);
		}
		return availableRepos.stream()
				.map(Repository::getCode)
				.collect(Collectors.toList());
	}
	
	@ActionMethod
	public String installOnRepo() throws BusinessException {
		meveoModuleApi.installData(entity, repositoryService.findByCode(selectedRepository));
		return "moduleDetail.xhtml?faces-redirect=true&meveoModuleId=" + entity.getId() + "&edit=true";
	}
	
	public String getRepositoriesCodes() {
		if (entity.getRepositories() == null) {
			return "";
		}
		
		return entity.getRepositories()
				.stream()
				.map(Repository::getCode)
				.collect(Collectors.joining(", "));
	}
	
	public void forceDelete() throws BusinessException {
		meveoModuleService.remove(entity);
	}
	
	public String recompileAll() {
		try {
			scriptService.reCompileAll(entity);
			messages.info("Recompiled scripts with success");
		} catch (Exception e) {
			MessagesHelper.error(messages, e);
		}
		
		return null;
	}
}