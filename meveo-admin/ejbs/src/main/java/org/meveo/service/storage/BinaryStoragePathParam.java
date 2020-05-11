package org.meveo.service.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.storage.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
public class BinaryStoragePathParam {

	private static Logger logger = LoggerFactory.getLogger(BinaryStoragePathParam.class);

	private boolean showOnExplorer;
	private String rootPath;
	private String cetCode;
	private String uuid;
	private String cftCode;
	private String filename;
	private String contentType;
	private InputStream is;
	private File file;
	private String filePath;
	private long fileSizeInBytes;

	// constraints
	private List<String> fileExtensions;
	private List<String> contentTypes;
	private Long maxFileSizeAllowedInKb;

	public BinaryStoragePathParam() {

	}

	public boolean isValidFileExtension() {
		final String ext = FilenameUtils.getExtension(getFilename());
		return (getFileExtensions().contains("*") || getFileExtensions().contains(ext));
	}

	public boolean isValidContentTypes() {
		return (getContentTypes().contains("*") || getContentTypes().contains(getContentType()));
	}

	public boolean isValidFilesize() {
		final long fileSizeInKB = fileSizeInBytes / 1024;
		return maxFileSizeAllowedInKb == null || (maxFileSizeAllowedInKb != 0 && fileSizeInKB <= maxFileSizeAllowedInKb);
	}

	public boolean isShowOnExplorer() {
		return showOnExplorer;
	}

	public void setShowOnExplorer(boolean showOnExplorer) {
		this.showOnExplorer = showOnExplorer;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getCetCode() {
		return cetCode;
	}

	public void setCetCode(String cetCode) {
		this.cetCode = cetCode;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getCftCode() {
		return cftCode;
	}

	public void setCftCode(String cftCode) {
		this.cftCode = cftCode;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public InputStream getContents() {
		if (file != null) {
			
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return null;
			}
			
		} else {
			return is;
		}
	}

	public void setInputStream(InputStream is) {
		this.is = is;
	}

	public List<String> getFileExtensions() {
		return fileExtensions;
	}

	public void setFileExtensions(List<String> fileExtensions) {
		this.fileExtensions = fileExtensions;
	}

	public List<String> getContentTypes() {
		return contentTypes;
	}

	public void setContentTypes(List<String> contentTypes) {
		this.contentTypes = contentTypes;
	}

	public InputStream getIs() {
		return is;
	}

	public void setIs(InputStream is) {
		this.is = is;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;

		if(this.contentType == null) {
			try {
				Tika tika = new Tika();
				String mimeType = tika.detect(file);
				this.setContentType(mimeType);
			} catch (IOException e) {
				logger.warn("Cannot determine content type", e);
			}
		}
	}

	public void setCft(CustomFieldTemplate customFieldTemplate){
		this.cftCode = customFieldTemplate.getCode();
		this.maxFileSizeAllowedInKb = customFieldTemplate.getMaxFileSizeAllowedInKb();
		this.filePath = customFieldTemplate.getFilePath();
		this.contentTypes = customFieldTemplate.getContentTypes();
		this.fileExtensions = customFieldTemplate.getFileExtensions();
		this.showOnExplorer = customFieldTemplate.isSaveOnExplorer();
	}

	public void setRepository(Repository repository){
		if(repository.getBinaryStorageConfiguration() != null) {
			this.rootPath = repository.getBinaryStorageConfiguration().getRootPath();
		}
	}

	public Long getMaxFileSizeAllowedInKb() {
		return maxFileSizeAllowedInKb;
	}

	public void setMaxFileSizeAllowedInKb(Long maxFileSizeAllowedInKb) {
		this.maxFileSizeAllowedInKb = maxFileSizeAllowedInKb;
	}

	public long getFileSizeInBytes() {
		return fileSizeInBytes;
	}

	public void setFileSizeInBytes(long fileSizeInBytes) {
		this.fileSizeInBytes = fileSizeInBytes;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public String toString() {
		return "BinaryStoragePathParam [showOnExplorer=" + showOnExplorer + ", rootPath=" + rootPath + ", cetCode=" + cetCode + ", uuid=" + uuid + ", cftCode=" + cftCode
				+ ", filename=" + filename + ", contentType=" + contentType + ", is=" + is + ", file=" + file + ", filePath=" + filePath + ", fileSizeInBytes=" + fileSizeInBytes
				+ ", fileExtensions=" + fileExtensions + ", contentTypes=" + contentTypes + ", maxFileSizeAllowedInKb=" + maxFileSizeAllowedInKb + "]";
	}
}
