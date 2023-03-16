/**
 * 
 */
package org.meveo.model.customEntities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class BinaryProvider implements Serializable {

	private static final long serialVersionUID = 656893414063747937L;

	@JsonValue
	private String fileName;
	
	private String contentType;
	
	private Long fileSize;
	
	@JsonIgnore
	private transient Supplier<InputStream> provider;
	
	@JsonIgnore
	private boolean overwrite;
	
	public BinaryProvider(String fileName, Supplier<InputStream> provider, boolean overwrite) {
		super();
		this.fileName = fileName;
		this.provider = provider;
		this.overwrite = overwrite;
	}
	
	public BinaryProvider(File file) {
		if (!file.exists()) {
			throw new RuntimeException(new FileNotFoundException(file.getAbsolutePath() + " does not exists"));
		}
		
		this.fileName = file.getName();
		this.provider = () -> {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return null;
			}
		};
		this.overwrite = true;
		
		try {
			this.contentType = Files.probeContentType(file.toPath());
			this.fileSize = Files.size(file.toPath());
		} catch (Exception e) {
			// NOOP
		}
		
		
	}
	
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * @param fileSize the fileSize to set
	 */
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the {@link #fileName}
	 */
	public String getFileName () {
		return fileName;
	}

	/**
	 * @return the {@link #provider}
	 */
	@JsonIgnore
	public InputStream getBinary() {
		return provider.get();
	}
	
	/**
	 * @return the {@link #overwrite}
	 */
	public boolean isOverwrite() {
		return overwrite;
	}
	
	/**
	 * @return the {@link #contentType}
	 */
	public String getContentType() {
		return contentType;
	}
	
	/**
	 * @return the {@link #fileSize}
	 */
	public Long getFileSize() {
		return fileSize;
	}
	
}
