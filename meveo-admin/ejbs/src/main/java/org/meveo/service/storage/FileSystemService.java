package org.meveo.service.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@Stateless
public class FileSystemService {

	@Inject
	private ParamBeanFactory paramBeanFactory;

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;

	public String getProviderPath() {
		return paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
	}

	public String getRootPath(boolean showOnExplorer, String binaryStorageConfigurationRootPath) {

		StringBuilder rootPath = new StringBuilder(
				showOnExplorer ? getProviderPath() : paramBeanFactory.getInstance().getProperty("binary.storage.path", "/tmp/meveo/binary/storage"));
		rootPath = rootPath.append(File.separator);
		rootPath = rootPath.append(binaryStorageConfigurationRootPath);
		return rootPath.toString();
	}

	public String getStoragePath(BinaryStoragePathParam params) {

		StringBuilder path = new StringBuilder(getRootPath(params.isShowOnExplorer(), params.getRootPath()))
				.append(File.separator).append(params.getCetCode())
				.append(File.separator).append(params.getUuid())
				.append(File.separator).append(params.getCftCode());

		if (!StringUtils.isBlank(params.getFilePath())) {
			path.append(File.separator).append(params.getFilePath());
		}

		return path.toString();
	}
	
	public void delete(BinaryStoragePathParam params) {
		String storage = getStoragePath(params);
		File dir = new File(storage);
		for(File file : dir.listFiles()) {
			file.delete();
		}
	}

	public String persists(BinaryStoragePathParam params) throws BusinessException, IOException {
		// checks for extension and type
		if (params.getFileExtensions() != null && !params.getFileExtensions().isEmpty() && !params.isValidFileExtension()) {
			throw new BusinessException("Invalid file extension");
		}

		if (params.getContentTypes() != null && !params.getContentTypes().isEmpty() && !params.isValidContentTypes()) {
			throw new BusinessException("Invalid content type");
		}

		if (!params.isValidFilesize()) {
			throw new BusinessException("Invalid file size");
		}

		String storage = getStoragePath(params);

		File dir = new File(storage);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		storage = storage + File.separator + params.getFilename();

		try(InputStream is = params.getContents()){
			FileUtils.copyFile(storage, is);
		}

		return storage;
	}
	
    /**
     * Save the binaries values to the file system and replace them in the value map by the path where they are stored. <br>
     * In case of a single storage binary, remove the previous one from file system. <br>
     * In case of a list storage binary, add it to the current list
     * TODO: re-make documentation
     *
     * @param fields     Fields definition
     * @param values     Fields values
     * @param repository Repository where to store binaries
     * @return the persisted binaries by custom field templates
     */
    public Map<CustomFieldTemplate, Object> updateBinaries(Repository repository, String uuid, CustomEntityTemplate cet, Collection<CustomFieldTemplate> fields, Map<String, Object> values, Map<String, Object> previousValues) throws IOException, BusinessException, BusinessApiException {
        Map<CustomFieldTemplate, Object> binariesSaved = new HashMap<>();
        for (CustomFieldTemplate field : fields) {
        	
            if (field.getFieldType().equals(CustomFieldTypeEnum.BINARY)) {
                BinaryStoragePathParam binaryStoragePathParam = new BinaryStoragePathParam();
                binaryStoragePathParam.setCft(field);
                binaryStoragePathParam.setUuid(uuid);
                binaryStoragePathParam.setCetCode(cet.getCode());
                binaryStoragePathParam.setRepository(repository);
                binaryStoragePathParam.setShowOnExplorer(field.isSaveOnExplorer());
                
            	// If key is present but is empty, remove the data and the files
            	if(values.containsKey(field.getCode()) && StringUtils.isBlank(values.get(field.getCode()))) {
                    delete(binaryStoragePathParam);
                    binariesSaved.put(field, null);	// Null value indicate we remove those binaries
                    
            	} else if (field.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE) && values.get(field.getCode()) != null) {
                    File tempFile = (File) values.get(field.getCode());
                    binaryStoragePathParam.setFile(tempFile);
                    binaryStoragePathParam.setFilename(tempFile.getName());

                    final String persistedPath = persists(binaryStoragePathParam);
                    values.put(field.getCode(), persistedPath);
                    binariesSaved.put(field, persistedPath);

                    // Remove old file
                    if (previousValues.get(field.getCode()) != null) {
                        String oldFile = (String) previousValues.get(field.getCode());
                        new File(oldFile).delete();
                    }

                } else if (field.getStorageType().equals(CustomFieldStorageTypeEnum.LIST) && values.get(field.getCode()) != null) {
                	List<?> value = (List<?>) values.get(field.getCode());
                	
                    // Determine type of the list : String or File
                    Object firstItem = null;
                    if(!CollectionUtils.isEmpty(value)) {
                    	firstItem = value.get(0);
                    }

                    // Append new persisted files path to existing ones
                    List<String> persistedPaths = previousValues.get(field.getCode()) != null ? (List<String>) previousValues.get(field.getCode()) : new ArrayList<>();

                    // If list of File, concatenate to existing ones
                    if(firstItem instanceof File) {
                        List<File> tempFiles = (List<File>) value;
	                    for (File tempFile : new ArrayList<>(tempFiles)) {
	                        binaryStoragePathParam.setFile(tempFile);
	                        // Use list size to name the file
	                        binaryStoragePathParam.setFilename(tempFile.getName());
	
	                        final String persistedPath = persists(binaryStoragePathParam);
	                        if (!persistedPaths.contains(persistedPath)) {
	                            persistedPaths.add(persistedPath);
	                        }
	                    }
	                    
                    // If list of String, re-order and delete files
                    } else if (firstItem instanceof String && CollectionUtils.isNotEmpty(persistedPaths)){
                    	List<String> uris = (List<String>) value;
                    	
                    	List<File> existingFiles = persistedPaths.stream().map(File::new).collect(Collectors.toList());
                    	List<File> filesToReorder = new ArrayList<>();
                    	
                    	for(String uri : uris) {
                        	// We first need to retrieve the path from the URI
                        	File file;
                    		
                        	// Retrieve index
                    		Integer index = BinaryStorageUtils.getIndexFromURI(uri);
                    		
                        	// Retrieve file
                        	if(BinaryStorageUtils.filePathContainsEL(field)) {
                        		file = findBinaryDynamicPath(repository, cet, values, field.getCode(), index);
                        	} else {
                        		file = findBinaryStaticPath(repository, cet.getCode(), uuid, field, index);
                        	}
                        	
                        	filesToReorder.add(file);
                    	}
                    	
                    	// Delete file that should not be kept
                    	for(File existingFile : existingFiles) {
                    		if(!filesToReorder.contains(existingFile)) {
                    			existingFile.delete();
                    		}
                    	}
                    	
                    	// Save the paths in the same order as input
                    	persistedPaths = filesToReorder.stream().map(File::getAbsolutePath).collect(Collectors.toList());
                    }
                    
                    persistedPaths = persistedPaths.stream().distinct().collect(Collectors.toList());

                    values.put(field.getCode(), persistedPaths);
                    binariesSaved.put(field, persistedPaths);
                }
            }
        }

        return binariesSaved;
    }
    
	public File findBinaryDynamicPath(Repository repository, CustomEntityTemplate cet, Map<String, Object> values, String cftCode, Integer index) throws EntityDoesNotExistsException {
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
	
	public File findBinaryStaticPath(Repository repository, String cetCode, String uuid, CustomFieldTemplate cft, Integer index) throws BusinessApiException {
		int i = index == null ? 0 : index;

		BinaryStoragePathParam params = new BinaryStoragePathParam();
		if(repository.getBinaryStorageConfiguration() != null) {
			params.setRootPath(repository.getBinaryStorageConfiguration().getRootPath());
		}
		params.setCetCode(cetCode);
		params.setUuid(uuid);
		params.setCftCode(cft.getCode());
		params.setFilePath(cft.getFilePath());
		params.setShowOnExplorer(cft.isSaveOnExplorer());

		String fullPath = getStoragePath(params);

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

		return files[i];
	}
}
