package org.meveo.api.dto.response.sql;

import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.dto.sql.SqlConfigurationDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A response wrapper to {@link SqlConfigurationDto}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 6.6.0
 */
@ApiModel
public class SqlConfigurationResponseDto extends BaseResponse {

	private static final long serialVersionUID = 4793891394377399797L;

	/**
	 * The sql configuration dto.
	 */
	@ApiModelProperty("The sql configuration dto.")
	private SqlConfigurationDto sqlConfiguration;

	public SqlConfigurationDto getSqlConfiguration() {
		return sqlConfiguration;
	}

	public void setSqlConfiguration(SqlConfigurationDto sqlConfiguration) {
		this.sqlConfiguration = sqlConfiguration;
	}
}
