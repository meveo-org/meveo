/**
 * 
 */
package org.meveo.model.customEntities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class BinaryProvider implements Serializable {

	private static final long serialVersionUID = 656893414063747937L;

	@JsonValue
	private String fileName;
	
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
		this.fileName = file.getName();
		this.provider = () -> {
			try {
				return new FileInputStream(file);
			} catch (FileNotFoundException e) {
				return null;
			}
		};
		this.overwrite = true;
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
	
}
