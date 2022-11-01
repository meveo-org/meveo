package org.meveo.service.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.elresolver.ValueExpressionWrapper;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.BinaryProvider;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.model.storage.IStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.PersistenceActionResult;
import org.meveo.persistence.StorageImpl;
import org.meveo.persistence.StorageQuery;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author Clément Bareth
 */
@Stateless
public class FileSystemService implements StorageImpl {

	@Inject
	private ParamBeanFactory paramBeanFactory;

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;
	
	@Inject
	private RepositoryService repositoryService;
	
	@Inject
	private Logger log;
	
	/**
	 * Remove the folders corresponding to a given field
	 * 
	 * @param cetCode Name of root folder inside the storage
	 * @param cft Field to remove binaries
	 * @throws IOException if binaries removal failed
	 */
	public void removeBinaries(String cetCode, CustomFieldTemplate cft) throws IOException {
		for (Repository repository : repositoryService.list()) {
			BinaryStoragePathParam cetFolderPath = new BinaryStoragePathParam();
			cetFolderPath.setCetCode(cetCode);
			cetFolderPath.setCft(cft);
			cetFolderPath.setRepository(repository);
			
			File cetFolder = new File(getStoragePath(cetFolderPath, null));
			if (cetFolder.listFiles() != null) {
				for (File instanceFolder : cetFolder.listFiles()) {
					File cftFolder = new File(instanceFolder, cft.getCode());
					if(cftFolder.exists() && cftFolder.isDirectory()) {
						org.apache.commons.io.FileUtils.deleteDirectory(cftFolder);
					}
					
					// Delete instance folder if empty
					if (instanceFolder.list() == null || instanceFolder.list().length == 0) {
						instanceFolder.delete();
					}
				}
			}
			
			// Delete cet folder if empty
			if (cetFolder.list() == null || cetFolder.list().length == 0) {
				cetFolder.delete();
			}
		}
	}
	
	/**
	 * Move files from / to the file explorer for every storage configurations
	 * 
	 * @param cetCode        Base folder of the binaries
	 * @param cftCode        Specific folder to move
	 * @param toFileExplorer Whether to move from / to file explorer
	 * @return a summary explaining where the binaries where move from / to for each entity instance
	 * @throws IOException if the move failed
	 */
	public Map<EntityRef, List<File>> moveBinaries(String cetCode, String cftCode, boolean toFileExplorer) throws IOException {
		Map<EntityRef, List<File>> movedFilesByUUID = new HashMap<>();
		
		for (Repository repository : repositoryService.list()) {
			BinaryStoragePathParam previousPathParams = new BinaryStoragePathParam();
			previousPathParams.setCetCode(cetCode);
			previousPathParams.setShowOnExplorer(!toFileExplorer);
			previousPathParams.setRepository(repository);

			String previousPath = getStoragePath(previousPathParams, null);

			BinaryStoragePathParam nextPathParams = new BinaryStoragePathParam();
			nextPathParams.setCetCode(cetCode);
			nextPathParams.setShowOnExplorer(toFileExplorer);
			nextPathParams.setRepository(repository);

			String nextPath = getStoragePath(nextPathParams, null);
			File destCetFolder = new File(nextPath);
			if (!destCetFolder.exists()) {
				destCetFolder.mkdirs();
			}

			// Go through each existing folder
			File cetFolder = new File(previousPath);
			if (cetFolder.listFiles() != null) {
				for (File instanceFolder : cetFolder.listFiles()) {
					File cftFolder = new File(instanceFolder, cftCode);
					if (!cftFolder.isDirectory() || !cftFolder.exists() || cftFolder.listFiles() == null || cftFolder.listFiles().length == 0 ) {
						continue;
					}
					
					// Create instance and cft folder
					File newInstanceFolder = new File(destCetFolder, instanceFolder.getName());
					if (!newInstanceFolder.exists()) {
						newInstanceFolder.mkdirs();
					}

					File newCftFolder = new File(newInstanceFolder, cftCode);

					// Move old directory to new location
					Files.move(cftFolder.toPath(), newCftFolder.toPath());
					
					// Build diff list
					List<File> locationDiffs = new ArrayList<>();
					for(File f : newCftFolder.listFiles()) {
						locationDiffs.add(f);
					}
					
					EntityRef entityRef = new EntityRef(instanceFolder.getName(), cetCode);
					entityRef.setRepository(repository);
					
					movedFilesByUUID.put(entityRef, locationDiffs);

					// Delete instance folder if empty
					if (instanceFolder.list() == null || instanceFolder.list().length == 0) {
						instanceFolder.delete();
					}
				}
			}

			// Delete cet folder if empty
			if (cetFolder.list() == null || cetFolder.list().length == 0) {
				cetFolder.delete();
			}

		}
		
		return movedFilesByUUID;

	}

	public String getProviderPath() {
		return paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
	}
	
	public String getRootPath(boolean showOnExplorer, String binaryStorageConfigurationRootPath) {

		StringBuilder rootPath = new StringBuilder(
				showOnExplorer ? getProviderPath() : paramBeanFactory.getInstance().getProperty("binary.storage.path", "/tmp/meveo/binary/storage")
        );

		if(binaryStorageConfigurationRootPath!=null){
        	rootPath.append(File.separator);
        	rootPath.append(binaryStorageConfigurationRootPath);
		}
        return rootPath.toString();
	}

	public String getStoragePath(BinaryStoragePathParam params, Map<String, Object> values) {

		StringBuilder path = new StringBuilder(getRootPath(params.isShowOnExplorer(), params.getRootPath()))
				.append(File.separator).append(params.getCetCode());
				
		if(!StringUtils.isBlank(params.getUuid())) {
			path.append(File.separator).append(params.getUuid());
			
			if(!StringUtils.isBlank(params.getCftCode())) {
	            path.append(File.separator).append(params.getCftCode());

	            if (!StringUtils.isBlank(params.getFilePath())) {

	                try {
	                    Object evaluatedExpr = ValueExpressionWrapper.evaluateExpression(params.getFilePath(), new HashMap<>(values), String.class);
	                    path.append(File.separator).append(evaluatedExpr);
	                } catch (ELException e) {
	                    throw new RuntimeException(e);
	                }

	            }
	        }
		}

		return path.toString();
	}

    /**
     * Remove every file related to a given entity
     *
     * @param repository Repository where the entity is stored
     * @param cet        Template of the entity
     * @param uuid       UUID of the entity
     */
    public void delete(Repository repository, CustomEntityTemplate cet, String uuid){
	    // Delete all files that are on file explorer for the given uuid
        BinaryStoragePathParam binaryStoragePathParamOnExplorer = new BinaryStoragePathParam();
        binaryStoragePathParamOnExplorer.setUuid(uuid);
        binaryStoragePathParamOnExplorer.setCetCode(cet.getCode());
        binaryStoragePathParamOnExplorer.setRepository(repository);
        binaryStoragePathParamOnExplorer.setShowOnExplorer(true);
        delete(binaryStoragePathParamOnExplorer, null);

        // Delete all files that are not on file explorer for the given uuid
        BinaryStoragePathParam binaryStoragePathParamNotOnExplorer = new BinaryStoragePathParam();
        binaryStoragePathParamNotOnExplorer.setUuid(uuid);
        binaryStoragePathParamNotOnExplorer.setCetCode(cet.getCode());
        binaryStoragePathParamNotOnExplorer.setRepository(repository);
        binaryStoragePathParamNotOnExplorer.setShowOnExplorer(false);
        delete(binaryStoragePathParamNotOnExplorer, null);
    }
	
	public void delete(BinaryStoragePathParam params, Map<String, Object> values) {
		if(values == null) {
			values = Collections.emptyMap();
		}

		String storage = getStoragePath(params, values);
		File directory = new File(storage);
		if(directory.exists() && directory.isDirectory()) {
			try {
				org.apache.commons.io.FileUtils.deleteDirectory(directory);
			} catch (IOException e) {
				log.error("Failed to delete dir", e);
			}
		}
	}

	public String persists(BinaryStoragePathParam params) throws IOException {
		return persists(params, Collections.emptyMap());
	}

	public String persists(BinaryStoragePathParam params, Map<String, Object> values) throws IOException {
		// checks for extension and type
		if (params.getFileExtensions() != null && !params.getFileExtensions().isEmpty() && !params.isValidFileExtension()) {
			throw new IllegalArgumentException("Invalid file extension : " + FilenameUtils.getExtension(params.getFilename()));
		}

		if (params.getContentTypes() != null && !params.getContentTypes().isEmpty() && !params.isValidContentTypes()) {
			throw new IllegalArgumentException("Invalid content type : " + params.getContentType());
		}

		if (!params.isValidFilesize()) {
			throw new IllegalArgumentException("Invalid file size, should be lower than " + params.getMaxFileSizeAllowedInKb() + " kb");
		}

		String storage = getStoragePath(params, values);

		File dir = new File(storage);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		storage = storage + File.separator + params.getFilename();

		try(InputStream is = params.getContents()){
			FileUtils.copyFile(storage, is);
		}

		return new File(storage).getPath();
	}

	@SuppressWarnings("unchecked")
	public File findBinaryDynamicPath(Map<String, Object> values, String cftCode, Integer index) {
		String filePath;

		Object filePathOrfilePaths = values.get(cftCode);
		if(filePathOrfilePaths instanceof String){
			filePath = (String) filePathOrfilePaths;
		} else if(filePathOrfilePaths instanceof Collection){
			if(index == null) {
				filePath = ((Collection<String>) filePathOrfilePaths).iterator().next();
			} else {
				filePath = new ArrayList<>((Collection<String>) filePathOrfilePaths).get(index);
			}
		} else {
			return null;
		}

		return new File(filePath);
	}
	
	
	public List<File> findBinariesStaticPath(BinaryStorageConfiguration repository, String cetCode, String uuid, CustomFieldTemplate cft) throws BusinessApiException {
		BinaryStoragePathParam params = new BinaryStoragePathParam();
		if(repository != null) {
			params.setRootPath(repository.getRootPath());
		}
		params.setCetCode(cetCode);
		params.setUuid(uuid);
		params.setCftCode(cft.getCode());
		params.setFilePath(cft.getFilePath());
		params.setShowOnExplorer(cft.isSaveOnExplorer());

		String fullPath = getStoragePath(params, Collections.emptyMap());

		File directory = new File(fullPath);
		if (!directory.exists()) {
			throw new BusinessApiException("Directory does not exists: " + directory.getPath());
		}

		if(!directory.isDirectory()){
			throw new BusinessApiException(directory.getPath() + " is not a directory");
		}

		final File[] files = directory.listFiles();
		if(files == null || files.length == 0){
			return null;
		}

		return Arrays.asList(files);
	}
	
	public File findBinaryStaticPath(BinaryStorageConfiguration repository, String cetCode, String uuid, CustomFieldTemplate cft, Integer index) throws BusinessApiException {
		int i = index == null ? 0 : index;
		return findBinariesStaticPath(repository, cetCode, uuid, cft).get(i);
	}

	@Override
	public boolean exists(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid) {
		return false;
	}

	@Override
	public String findEntityIdByValues(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) {
		return null;
	}

	@Override
	public Map<String, Object> findById(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid, Map<String, CustomFieldTemplate> cfts, Collection<String> fetchFields, boolean withEntityReferences) {
		Map<String, Object> result = new HashMap<>();
		
		cfts.values()
			.stream()
			.filter(cft -> cft.getFieldType() == CustomFieldTypeEnum.BINARY)
			.filter(cft -> fetchFields.contains(cft.getCode()))
			.forEach(cft -> {
				try {
					List<BinaryProvider> binaries = this.findBinariesStaticPath((BinaryStorageConfiguration) repository, cet.getCode(), uuid, cft)
							.stream()
							.filter(File::exists)
							.map(BinaryProvider::new)
							.collect(Collectors.toList());
					
					result.put(cft.getCode(), binaries);
				} catch (BusinessApiException e) {
					log.error("Failed to fetch binaries", e);
				}
			});
		
		return null;
	}

	@Override
	public List<Map<String, Object>> find(StorageQuery query) throws EntityDoesNotExistsException {
		// Never eagerly load files
		return new ArrayList<>();
	}

	@Override
	public PersistenceActionResult createOrUpdate(Repository repository, IStorageConfiguration storageConf, CustomEntityInstance cei, Map<String, CustomFieldTemplate> customFieldTemplates, String foundUuid) throws BusinessException {
		final String uuid = StringUtils.isBlank(foundUuid) ? cei.getUuid() : foundUuid;
		
		String rootPath = repository != null && repository.getBinaryStorageConfiguration() != null ? repository.getBinaryStorageConfiguration().getRootPath() : "";

		customFieldTemplates.values()
		.stream()
		.filter(cft -> cft.getFieldType() == CustomFieldTypeEnum.BINARY)
		.forEach(cft -> {
			var cfValue = cei.getCfValues().getCfValue(cft.getCode());
			Set<String> fileNames = cfValue.getFileNames();
			
			cfValue.getBinaries().forEach(binary -> {
				BinaryStoragePathParam params = new BinaryStoragePathParam();
				params.setShowOnExplorer(cft.isSaveOnExplorer());
				params.setRootPath(rootPath);
				params.setCetCode(cei.getCetCode());
				params.setUuid(uuid);
				params.setCftCode(cft.getCode());
				params.setFilePath(cft.getFilePath());
				// params.setContentType(file.getContentType());
				params.setFilename(binary.getFileName());
				params.setInputStream(binary.getBinary());
				// params.setFileSizeInBytes(file.getSize());
				params.setFileExtensions(cft.getFileExtensions());
				params.setContentTypes(cft.getContentTypes());
				params.setMaxFileSizeAllowedInKb(cft.getMaxFileSizeAllowedInKb());
				
				try {
					persists(params);
				} catch (IOException e) {
					log.error("Failed to persist binary", e);
				}
			});
			
			// Remove objects not present on the updated list
			try {
				findBinariesStaticPath(repository.getBinaryStorageConfiguration(), cei.getCetCode(), uuid, cft)
						.stream()
						.filter(file -> !fileNames.contains(file.getName()))
						.forEach(file -> {
							log.debug("Delete file {} from file system", file);
							file.delete();
						});
			} catch (BusinessApiException e) {
				log.error("Failed to read file system binaries", e);
			}
		});
		
		return new PersistenceActionResult(uuid);
	}

	@Override
	public PersistenceActionResult addCRTByUuids(IStorageConfiguration repository, CustomRelationshipTemplate crt, Map<String, Object> relationValues, String sourceUuid, String targetUuid) throws BusinessException {
		return null;
	}

	@Override
	public void update(Repository repository, IStorageConfiguration conf, CustomEntityInstance cei) throws BusinessException {
		createOrUpdate(repository, conf, cei, cei.getFieldTemplates(), cei.getUuid());
	}

	@Override
	public void setBinaries(IStorageConfiguration repository, CustomEntityTemplate cet, CustomFieldTemplate cft, String uuid, List<File> binaries) throws BusinessException {
	}

	@Override
	public void remove(IStorageConfiguration repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer count(IStorageConfiguration repository, CustomEntityTemplate cet, PaginationConfiguration paginationConfiguration) {
		return 0;
	}

	@Override
	public void cetCreated(CustomEntityTemplate cet) {
	}

	@Override
	public void crtCreated(CustomRelationshipTemplate crt) throws BusinessException {
	}

	@Override
	public void cftCreated(CustomModelObject template, CustomFieldTemplate cft) {
	}

	@Override
	public void cetUpdated(CustomEntityTemplate oldCet, CustomEntityTemplate cet) {
	}

	@Override
	public void crtUpdated(CustomRelationshipTemplate cet) throws BusinessException {
	}

	@Override
	public void cftUpdated(CustomModelObject template, CustomFieldTemplate oldCft, CustomFieldTemplate cft) {
	}

	@Override
	public void removeCft(CustomModelObject template, CustomFieldTemplate cft) {
	}

	@Override
	public void removeCet(CustomEntityTemplate cet) {
	}

	@Override
	public void removeCrt(CustomRelationshipTemplate crt) {
	}

	@Override
	public void init() {
	}

	@Override
	public <T> T beginTransaction(IStorageConfiguration repository, int stackedCalls) {
		return null;
	}

	@Override
	public void commitTransaction(IStorageConfiguration repository) {
	}

	@Override
	public void rollbackTransaction(int stackedCalls) {
	}

	@Override
	public void destroy() {
	}
}
