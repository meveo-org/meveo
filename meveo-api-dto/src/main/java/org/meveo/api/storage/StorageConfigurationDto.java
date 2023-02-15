/**
 * 
 */
package org.meveo.api.storage;


import org.meveo.api.dto.CFBusinessEntityDto;
import org.meveo.model.storage.StorageConfiguration;

public class StorageConfigurationDto extends CFBusinessEntityDto {
	
	private static final long serialVersionUID = 1909924067205813595L;

	private String credential;
	
	private String dbStorageType;
	
	private String protocol;
	
	private String hostname;
	
	private Integer port;
	
	public StorageConfigurationDto() {
		
	}
	
	public StorageConfigurationDto(StorageConfiguration conf) {
		if (conf.getCredential() != null) {
			this.credential = conf.getCredential().getCode();
		}
		this.dbStorageType = conf.getDbStorageType().getCode();
		this.protocol = conf.getProtocol();
		this.hostname = conf.getHostname();
		this.port = conf.getPort();
		this.code = conf.getCode();
		this.description = conf.getDescription();
	}

	/**
	 * @return the {@link #credential}
	 */
	public String getCredential() {
		return credential;
	}

	/**
	 * @param credential the credential to set
	 */
	public void setCredential(String credential) {
		this.credential = credential;
	}

	/**
	 * @return the {@link #dbStorageType}
	 */
	public String getDbStorageType() {
		return dbStorageType;
	}

	/**
	 * @param dbStorageType the dbStorageType to set
	 */
	public void setDbStorageType(String dbStorageType) {
		this.dbStorageType = dbStorageType;
	}

	/**
	 * @return the {@link #protocol}
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the {@link #hostname}
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @return the {@link #port}
	 */
	public Integer getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}
	
	

}
