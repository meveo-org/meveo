package org.meveo.api.dto.response.sql;

import java.util.List;

import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.dto.sql.SqlConfigurationDto;

import io.swagger.annotations.ApiModel;

/**
 * A wrapper to a list of {@link SqlConfigurationDto} response.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 6.6.0
 */
@ApiModel
public class SqlConfigurationsResponseDto extends BaseResponse {

	private static final long serialVersionUID = 8553130963787895311L;

	/**
	 * List of sql configuration information
	 */
	private List<SqlConfigurationDto> sqlConfigurations;

	public List<SqlConfigurationDto> getSqlConfigurations() {
		return sqlConfigurations;
	}

	public void setSqlConfigurations(List<SqlConfigurationDto> sqlConfigurations) {
		this.sqlConfigurations = sqlConfigurations;
	}
}
