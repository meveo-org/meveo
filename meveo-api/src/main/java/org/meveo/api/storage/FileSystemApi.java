package org.meveo.api.storage;

import org.meveo.api.BaseApi;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.storage.BinaryStoragePathParam;
import org.meveo.service.storage.FileSystemService;
import org.meveo.service.storage.RepositoryService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author clement.bareth
 */
@Stateless
public class FileSystemApi extends BaseApi {

	@Inject
	private FileSystemService fileSystemService;

	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private CrossStorageService crossStorageService;

	public File findBinary(Boolean showOnExplorer, String repositoryCode, String cetCode, String uuid, String cftCode, Integer index) throws EntityDoesNotExistsException, BusinessApiException, IOException {

		// Retrieve repository
		Repository repository = repositoryService.findByCode(repositoryCode);
		if(repository == null){
			throw new EntityDoesNotExistsException(Repository.class, repositoryCode);
		}
		
		// Retrieve CFT
		CustomFieldTemplate cft = cache.getCustomFieldTemplate(cftCode, CustomEntityTemplate.getAppliesTo(cetCode));
		if (cft == null) {
			throw new EntityDoesNotExistsException(CustomFieldTemplate.class, cftCode);
		}

		final String filePath = cft.getFilePath();

		// The file path expression contains an EL, so we need to retrieve the entity to get the evaluated file path
		if(filePath != null && (filePath.contains("#{") || filePath.contains("${")) && filePath.contains("}")){
			return findBinaryDynamicPath(repository, cetCode, uuid, cftCode, index);

		// The file path expression does not contains an EL, so we can re-build the path to the desired binary
		} else {
			return findBinaryStaticPath(showOnExplorer, repository, cetCode, uuid, cft, index);
		}

	}

	private File findBinaryDynamicPath(Repository repository, String cetCode, String uuid, String cftCode, Integer index) throws EntityDoesNotExistsException {
		// Retrieve CET
		CustomEntityTemplate cet = cache.getCustomEntityTemplate(cetCode);
		if (cet == null) {
			throw new EntityDoesNotExistsException(CustomEntityTemplate.class, cetCode);
		}


		// Retrieve the entity
		Map<String, Object> values = crossStorageService.find(repository, cet, uuid, Collections.singletonList(cftCode));
		if(values == null){
			throw new EntityDoesNotExistsException("EntityInstance", uuid);
		}

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

	private File findBinaryStaticPath(Boolean showOnExplorer, Repository repository, String cetCode, String uuid, CustomFieldTemplate cft, Integer index) throws BusinessApiException {
		int i = index == null ? 0 : index;

		BinaryStoragePathParam params = new BinaryStoragePathParam();
		if(repository.getBinaryStorageConfiguration() != null) {
			params.setRootPath(repository.getBinaryStorageConfiguration().getRootPath());
		}
		params.setCetCode(cetCode);
		params.setUuid(uuid);
		params.setCftCode(cft.getCode());
		params.setFilePath(cft.getFilePath());
		params.setShowOnExplorer(showOnExplorer == null ? false : showOnExplorer);

		String fullPath = fileSystemService.getStoragePath(params);

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
