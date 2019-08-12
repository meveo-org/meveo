package org.meveo.api.storage;

import org.apache.commons.io.IOUtils;
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
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
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

	public void findBinary(Boolean showOnExplorer, String repositoryCode, String cetCode, String uuid, String cftCode, HttpServletResponse response) throws EntityDoesNotExistsException, BusinessApiException, IOException {

		// Retrieve CET
		CustomEntityTemplate cet = cache.getCustomEntityTemplate(cetCode);
		if (cet == null) {
			throw new EntityDoesNotExistsException(CustomEntityTemplate.class, cetCode);
		}

		// Retrieve CFT
		CustomFieldTemplate cft = cache.getCustomFieldTemplate(cftCode, cet.getAppliesTo());
		if (cft == null) {
			throw new EntityDoesNotExistsException(CustomFieldTemplate.class, cftCode);
		}

		// Retrieve repository
		Repository repository = repositoryService.findByCode(repositoryCode);
		if(repository == null){
			throw new EntityDoesNotExistsException(Repository.class, repositoryCode);
		}

		// Retrieve the entity
		Map<String, Object> values = crossStorageService.find(repository, cet, uuid);
		if(values == null){
			throw new EntityDoesNotExistsException("EntityInstance", uuid);
		}

		String filename = null;

		Object filePathOrfilePaths = values.get(cftCode);
		if(filePathOrfilePaths instanceof String){
			filename = (String) filePathOrfilePaths;
		} else if(filePathOrfilePaths instanceof Collection){
			filename = (String) ((Collection) filePathOrfilePaths).iterator().next();
		} else if(filePathOrfilePaths == null){
			// No file stored
			response.sendError(404, "No binary stored for entity " + uuid + "[" + cetCode + "." + cftCode + "]");
			return;
		}

		BinaryStoragePathParam params = new BinaryStoragePathParam();
		params.setRootPath(repositoryCode);
		params.setCetCode(cetCode);
		params.setUuid(uuid);
		params.setCftCode(cftCode);
		params.setFilePath(cft.getFilePath());
		params.setFilename(filename);
		params.setShowOnExplorer(showOnExplorer == null ? true : showOnExplorer);

		String fullPath = fileSystemService.getStoragePath(params) + File.separator + filename;

		File file = new File(fullPath);
		if (!file.exists()) {
			throw new BusinessApiException("File does not exists: " + file.getPath());
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			response.setContentType(Files.probeContentType(file.toPath()));
			response.setContentLength((int) file.length());
			response.addHeader("Content-disposition", "attachment;filename=\"" + file.getName() + "\"");
			IOUtils.copy(fis, response.getOutputStream());
			response.flushBuffer();
		}
	}
}
