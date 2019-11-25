package org.meveo.api.dto.response.sql;

import java.util.List;

import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.dto.sql.SqlConfigurationDto;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 */
public class SqlConfigurationsResponseDto extends BaseResponse {

	private static final long serialVersionUID = 8553130963787895311L;

	private List<SqlConfigurationDto> sqlConfigurations;

	public List<SqlConfigurationDto> getSqlConfigurations() {
		return sqlConfigurations;
	}

	public void setSqlConfigurations(List<SqlConfigurationDto> sqlConfigurations) {
		this.sqlConfigurations = sqlConfigurations;
	}
}
