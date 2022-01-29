package org.meveo.model.sql;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.security.PasswordUtils;

/**
 * The Class SqlConfiguration.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 6.6.0
 */
@Entity
@Table(name = "sql_configuration", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
		@Parameter(name = "sequence_name", value = "sql_configuration_seq"), })
public class SqlConfiguration extends BusinessEntity {

	private static final long serialVersionUID = 6630494504219053910L;

	/** The Constant DEFAULT_SQL_CONNECTION. */
	public transient static final String DEFAULT_SQL_CONNECTION = "default";

	@NotNull
	@Column(name = "driver_class", nullable = false)
	private String driverClass;

	@NotNull
	@Column(name = "url", nullable = false)
	private String url;

	@NotNull
	@Column(name = "username", nullable = false)
	private String username;

	@NotNull
	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "dialect")
	private String dialect;
	
	/**
	 * Schema or tablespace to use if specified.
	 */
	@Column(name = "schema")
	private String schema;

	@Type(type = "numeric_boolean")
	@ColumnDefault("0")
	@Column(name = "initialized")
	private boolean initialized = false;
	
	@Transient
	private String clearPassword;
	
	@Transient
	private Boolean isXaResource;
	
	/**
	 * @return the {@link #clearPassword}
	 */
	public String getClearPassword() {
		return clearPassword;
	}

	/**
	 * @param clearPassword the clearPassword to set
	 */
	public void setClearPassword(String clearPassword) {
		if(clearPassword != null) {
			String salt = PasswordUtils.getSalt(getCode(), getUrl());
			this.password = PasswordUtils.encrypt(salt, clearPassword);
		}
	}

	/**
	 * Gets the schema or tablespace to use.
	 *
	 * @return the schema or tablespace to use
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * Sets the schema or tablespace to use.
	 *
	 * @param schema the new schema or tablespace to use.
	 */
	public void setSchema(String schema) {
		this.schema = schema;
	}

	/**
	 * Gets the driver class.
	 *
	 * @return the driver class
	 */
	public String getDriverClass() {
		return driverClass;
	}

	/**
	 * Sets the driver class.
	 *
	 * @param driverClass the new driver class
	 */
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	/**
	 * Gets the url.
	 *
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Sets the url.
	 *
	 * @param url the new url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets the username.
	 *
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Sets the username.
	 *
	 * @param username the new username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Gets the password.
	 *
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Sets the password.
	 *
	 * @param password the new password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Gets the dialect.
	 *
	 * @return the dialect
	 */
	public String getDialect() {
		return dialect;
	}

	/**
	 * Sets the dialect.
	 *
	 * @param dialect the new dialect
	 */
	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	/**
	 * Checks if is initialized.
	 *
	 * @return true, if is initialized
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/**
	 * Sets the initialized.
	 *
	 * @param initialized the new initialized
	 */
	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public boolean isXAResource() {
		if (isXaResource!= null)
			return isXaResource;
		if (DEFAULT_SQL_CONNECTION.equals(code))
			isXaResource=true;
		else if (url == null)
			return false;
		else 
			isXaResource = false;
		return isXaResource;
	}
	
	

}
