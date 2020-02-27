package org.meveo.api.dto.config;

import org.meveo.api.dto.response.BaseResponse;

import io.swagger.annotations.ApiModelProperty;

/**
 * Wrapper to maven configuration information.
 * 
 * @see MavenConfigurationDto
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
public class MavenConfigurationResponseDto extends BaseResponse {

	private static final long serialVersionUID = 4285496436832972796L;

	@ApiModelProperty("Maven configuration information")
	private MavenConfigurationDto mavenConfiguration;

	public MavenConfigurationDto getMavenConfiguration() {

		if (mavenConfiguration == null) {
			mavenConfiguration = new MavenConfigurationDto();
		}
		return mavenConfiguration;
	}

	public void setMavenConfiguration(MavenConfigurationDto mavenConfiguration) {
		this.mavenConfiguration = mavenConfiguration;
	}
}
