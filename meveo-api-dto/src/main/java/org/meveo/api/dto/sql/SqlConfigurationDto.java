package org.meveo.api.dto.sql;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.sql.SqlConfiguration;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @version 6.6.0
 * @version 6.6.0
 */
public class SqlConfigurationDto extends BusinessEntityDto {

	private static final long serialVersionUID = -3116379097059429095L;

	private String driverClass;
	private String url;
	private String username;
	private String password;
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
