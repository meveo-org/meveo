package org.meveo.model.storage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.BusinessCFEntity;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.admin.MvCredential;
import org.meveo.model.persistence.DBStorageType;

/**
 * Class holding information to connect to a data storage.
 * 
 * @author Clément Bareth
 * @version 7.2.0
 * @since 7.2.0
 */
@Entity
@Table(name = "storage_configuration", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "increment")
@CustomFieldEntity(cftCodePrefix = "STORAGE")
@ModuleItem(value = "StorageConfiguration", path = "storages")
public class StorageConfiguration extends BusinessCFEntity implements IStorageConfiguration {

	private static final long serialVersionUID = -93688572926121511L;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "credential_id")
	private MvCredential credential;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "storage_type")
	private DBStorageType dbStorageType;
	
	@Column(name = "protocol")
	private String protocol;
	
	@Column(name = "hostname")
	private String hostname;
	
	@Column(name = "port")
	private Integer port;

	/**
	 * @return the {@link #credential}
	 */
	public MvCredential getCredential() {
		return credential;
	}

	/**
	 * @param credential the credential to set
	 */
	public void setCredential(MvCredential credential) {
		this.credential = credential;
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
	
	/**
	 * @return the {@link #dbStorageType}
	 */
	public DBStorageType getDbStorageType() {
		return dbStorageType;
	}

	/**
	 * @param dbStorageType the dbStorageType to set
	 */
	public void setDbStorageType(DBStorageType dbStorageType) {
		this.dbStorageType = dbStorageType;
	}

	public String getConnectionUri() {
		String uri = "";
		if (protocol != null) {
			uri += protocol + "://";
		}
		if (hostname != null) {
			uri += hostname;
		}
		if (port != null) {
			uri += ":" + port;
		}
		return uri;
	}

}