package org.meveo.service.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.StringUtils;
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

		StringBuilder path = new StringBuilder(getRootPath(params.isShowOnExplorer(), params.getRootPath()));
		path = path.append(File.separator);
		path = path.append(params.getCetCode());
		path = path.append(File.separator);
		path = path.append(params.getUuid());
		path = path.append(File.separator);
		path = path.append(params.getCftCode());

		if (!StringUtils.isBlank(params.getFilePath())) {
			path = path.append(File.separator);
			path = path.append(params.getFilePath());
		}

		return path.toString();
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

		InputStream is = new ByteArrayInputStream(params.getContents());
		FileUtils.copyFile(storage, is);

		return storage;
	}
}
