package org.meveo.api.dto.response.sql;

import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.dto.sql.SqlConfigurationDto;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 */
public class SqlConfigurationResponseDto extends BaseResponse {

	private static final long serialVersionUID = 4793891394377399797L;

	private SqlConfigurationDto sqlConfiguration;

	public SqlConfigurationDto getSqlConfiguration() {
		return sqlConfiguration;
	}

	public void setSqlConfiguration(SqlConfigurationDto sqlConfiguration) {
		this.sqlConfiguration = sqlConfiguration;
	}
}
