package org.meveo.api.dto.response.storage;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.storage.BinaryStorageConfigurationDto;

/**
 * @author Edward P. Legaspi
 */
public class BinaryStorageConfigurationResponseDto extends BaseResponse {

	private static final long serialVersionUID = 1796260917417235587L;

	@ApiModelProperty("Binary storage configuration information")
	private BinaryStorageConfigurationDto binaryStorageConfiguration;

	public BinaryStorageConfigurationDto getBinaryStorageConfiguration() {
		return binaryStorageConfiguration;
	}

	public void setBinaryStorageConfiguration(BinaryStorageConfigurationDto binaryStorageConfiguration) {
		this.binaryStorageConfiguration = binaryStorageConfiguration;
	}

}
