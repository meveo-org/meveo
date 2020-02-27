package org.meveo.api.dto.sql;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.sql.SqlConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Sql configuration that is use to connect to an external datasource. Tested on
 * PostgreSQL.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 6.6.0
 */
@ApiModel
public class SqlConfigurationDto extends BusinessEntityDto {

	private static final long serialVersionUID = -3116379097059429095L;

	/**
	 * The driver class use to load this datasource
	 */
	@ApiModelProperty("The driver class use to load this datasource")
	private String driverClass;

	/**
	 * The url of this datasource. jdbc:postgresql://localhost:5432/meveo.
	 */
	@ApiModelProperty("The url of this datasource. jdbc:postgresql://localhost:5432/meveo.")
	private String url;

	/**
	 * This datasource's username
	 */
	@ApiModelProperty("This datasource's username")
	private String username;

	/**
	 * This datasource's password
	 */
	@ApiModelProperty("This datasource's password")
	private String password;

	/**
	 * The dialect, if supported
	 */
	@ApiModelProperty("The dialect, if supported")
	private String dialect;

	public SqlConfigurationDto() {

	}

	public SqlConfigurationDto(SqlConfiguration e) {

		setDriverClass(e.getDriverClass());
		setUrl(e.getUrl());
		setUsername(e.getUsername());
		setPassword(e.getPassword());
		setDialect(e.getDialect());
	}

	public String getDriverClass() {
		return driverClass;
	}

	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}
}
