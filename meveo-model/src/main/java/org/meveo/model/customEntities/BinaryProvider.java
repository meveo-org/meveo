/**
 * 
 */
package org.meveo.model.customEntities;

import java.io.InputStream;
import java.util.function.Supplier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public class BinaryProvider {

	@JsonValue
	private String fileName;
	
	@JsonIgnore
	private Supplier<InputStream> provider;
	
	/**
	 * Instantiates a new BinaryProvider
	 *
	 * @param fieldName
	 * @param provider
	 */
	public BinaryProvider(String fieldName, Supplier<InputStream> provider) {
		super();
		this.fileName = fieldName;
		this.provider = provider;
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
	
}
