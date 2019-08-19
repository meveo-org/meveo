package org.meveo.service.storage;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;
import org.meveo.elresolver.ELException;
import org.meveo.elresolver.ValueExpressionWrapper;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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

	public String getProviderPath() {
		return paramBeanFactory.getInstance().getChrootDir(currentUser.getProviderCode());
	}

	public String getRootPath(boolean showOnExplorer, String binaryStorageConfigurationRootPath) {

		StringBuilder rootPath = new StringBuilder(
				showOnExplorer ? getProviderPath() : paramBeanFactory.getInstance().getProperty("binary.storage.path", "/tmp/meveo/binary/storage")
        );

        rootPath.append(File.separator);
        rootPath.append(binaryStorageConfigurationRootPath);
        return rootPath.toString();
	}

	public String getStoragePath(BinaryStoragePathParam params, Map<String, Object> values) {

		StringBuilder path = new StringBuilder(getRootPath(params.isShowOnExplorer(), params.getRootPath()))
				.append(File.separator).append(params.getCetCode())
				.append(File.separator).append(params.getUuid());

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

        // Delet all files that are not on fle explorer for the given uuid
        BinaryStoragePathParam binaryStoragePathParamNotOnExplorer = new BinaryStoragePathParam();
        binaryStoragePathParamNotOnExplorer.setUuid(uuid);
        binaryStoragePathParamNotOnExplorer.setCetCode(cet.getCode());
        binaryStoragePathParamNotOnExplorer.setRepository(repository);
        binaryStoragePathParamNotOnExplorer.setShowOnExplorer(false);
        delete(binaryStoragePathParamNotOnExplorer, null);
    }
	
	public void delete(BinaryStoragePathParam params, Map<String, Object> values) {
		if(values == null) {
			values = Collections.EMPTY_MAP;
		}

		String storage = getStoragePath(params, values);
		File directory = new File(storage);
		if(directory.exists() && directory.isDirectory()) {
			final File[] files = directory.listFiles();
			if(files != null) {
				for (File file : files) {
					file.delete();
				}
			}
		}
	}

	public String persists(BinaryStoragePathParam params) throws IOException {
		return persists(params, Collections.EMPTY_MAP);
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
    public Map<CustomFieldTemplate, Object> updateBinaries(Repository repository, String uuid, CustomEntityTemplate cet, Collection<CustomFieldTemplate> fields, Map<String, Object> values, Map<String, Object> previousValues) throws IOException, BusinessApiException {
        Map<CustomFieldTemplate, Object> binariesSaved = new HashMap<>();
        for (CustomFieldTemplate field : fields) {
        	
            if (field.getFieldType().equals(CustomFieldTypeEnum.BINARY)) {
                BinaryStoragePathParam binaryStoragePathParam = new BinaryStoragePathParam();
                binaryStoragePathParam.setCft(field);
                binaryStoragePathParam.setUuid(uuid);
                binaryStoragePathParam.setCetCode(cet.getCode());
                binaryStoragePathParam.setRepository(repository);
                binaryStoragePathParam.setShowOnExplorer(field.isSaveOnExplorer());
                binaryStoragePathParam.setFilePath(field.getFilePath());

            	// If key is present but is empty, remove the data and the files
            	if(values.containsKey(field.getCode()) && StringUtils.isBlank(values.get(field.getCode()))) {
                    delete(binaryStoragePathParam, previousValues);
                    binariesSaved.put(field, null);	// Null value indicate we remove those binaries
                    
            	} else if (field.getStorageType().equals(CustomFieldStorageTypeEnum.SINGLE) && values.get(field.getCode()) != null) {
                    File tempFile = (File) values.get(field.getCode());
                    binaryStoragePathParam.setFile(tempFile);
                    binaryStoragePathParam.setFilename(tempFile.getName());
					binaryStoragePathParam.setFileSizeInBytes(tempFile.length());

					try {
						final String persistedPath = persists(binaryStoragePathParam, values);
						values.put(field.getCode(), persistedPath);
						binariesSaved.put(field, persistedPath);
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException(e.getMessage() + " for field " + field.getCode());
					}

					// Remove old file
                    if (previousValues != null && previousValues.get(field.getCode()) != null) {
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
                    List<String> persistedPaths = new ArrayList<>();
                    if(previousValues != null && previousValues.get(field.getCode()) != null) {
                        persistedPaths = (List<String>) previousValues.get(field.getCode());
                    }

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
                        		file = findBinaryDynamicPath(values, field.getCode(), index);
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
                    	persistedPaths = filesToReorder.stream().map(File::getPath).collect(Collectors.toList());
                    }
                    
                    persistedPaths = persistedPaths.stream().distinct().collect(Collectors.toList());

                    values.put(field.getCode(), persistedPaths);
                    binariesSaved.put(field, persistedPaths);
                }
            }
        }

        return binariesSaved;
    }
    
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

		String fullPath = getStoragePath(params, Collections.EMPTY_MAP);

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
