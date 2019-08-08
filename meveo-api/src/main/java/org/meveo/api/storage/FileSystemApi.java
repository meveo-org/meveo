package org.meveo.api.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.meveo.api.BaseApi;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityInstanceService;
import org.meveo.service.storage.BinaryStoragePathParam;
import org.meveo.service.storage.FileSystemService;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
@Stateless
public class FileSystemApi extends BaseApi {
	@Inject
	private FileSystemService fileSystemService;

	@Inject
	private CustomFieldTemplateService cftService;

	@Inject
	private CustomFieldInstanceService cfiService;

	@Inject
	private CustomEntityInstanceService customEntityInstanceService;

	public void findBinary(Boolean showOnExplorer, String repositoryCode, String cetCode, String uuid, String cftCode, HttpServletResponse response)
			throws EntityDoesNotExistsException, BusinessApiException, IOException {

		CustomFieldTemplate cft = cftService.findByCode(cftCode);

		if (cft == null) {
			throw new EntityDoesNotExistsException(CustomFieldTemplate.class, cftCode);
		}

		// get the entity
		CustomEntityInstance cei = customEntityInstanceService.findByUuid(cetCode, uuid);

		String cfValue = (String) cfiService.getCFValue(cei, cftCode);
		String filename = FilenameUtils.getName(cfValue);

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
