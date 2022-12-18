package org.meveo.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.elresolver.ValueExpressionWrapper;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.scheduler.EntityRef;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author Clément Bareth
 */
@Stateless
public class FileSystemService {

	@Inject
	private ParamBeanFactory paramBeanFactory;

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;
	
	@Inject
	private RepositoryService repositoryService;
	
	private static Logger log = LoggerFactory.getLogger(FileSystemService.class);
	
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

		File file = new File(storage);
		try(InputStream is = params.getContents()) {
			if (file.exists() && is instanceof FileInputStream) {
				try {
					Files.copy(is, file.toPath(),  StandardCopyOption.REPLACE_EXISTING);
				} catch (FileSystemException e) {
					//NOOP - the file we are trying to write is the same file
				}
			} else {
				Files.copy(is, file.toPath(),  StandardCopyOption.REPLACE_EXISTING);
			}
		}

		return file.getPath();
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
	
	public List<File> findBinaries(BinaryStorageConfiguration repository, CustomEntityTemplate cet, String uuid, CustomFieldTemplate cft, Map<String, Object> values) throws BusinessApiException {
		// The file path expression contains an EL, so we need to retrieve the entity to get the evaluated file path
		if(BinaryStorageUtils.filePathContainsEL(cft)){
			return findBinariesDynamicPath(values, cft.getCode());

		// The file path expression does not contains an EL, so we can re-build the path to the desired binary
		} else {
			return findBinariesStaticPath(repository, cet.getCode(), uuid, cft);
		}
	}
	
	public List<File> findBinariesDynamicPath(Map<String, Object> values, String cftCode) {
		String filePath;

		Object filePathOrfilePaths = values.get(cftCode);
		if(filePathOrfilePaths instanceof String){
			List.of(new File((String) filePathOrfilePaths));
		} else if(filePathOrfilePaths instanceof Collection){
			return ((Collection<String>) filePathOrfilePaths).stream()
						.map(File::new)
						.collect(Collectors.toList());
		} else {
			return List.of();
		}
		
		return List.of();
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
			return Collections.emptyList();
		}

		if(!directory.isDirectory()){
			throw new BusinessApiException(directory.getPath() + " is not a directory");
		}

		final File[] files = directory.listFiles();
		if(files == null || files.length == 0){
			return Collections.emptyList();
		}

		return Arrays.asList(files);
	}
	
	public File findBinaryStaticPath(BinaryStorageConfiguration repository, String cetCode, String uuid, CustomFieldTemplate cft, Integer index) throws BusinessApiException {
		int i = index == null ? 0 : index;
		return findBinariesStaticPath(repository, cetCode, uuid, cft).get(i);
	}
}
