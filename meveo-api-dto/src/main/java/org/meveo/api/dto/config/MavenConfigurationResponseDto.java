package org.meveo.api.dto.config;

import org.meveo.api.dto.response.BaseResponse;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
public class MavenConfigurationResponseDto extends BaseResponse {

	private static final long serialVersionUID = 4285496436832972796L;
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
