package org.meveo.api.dto.response.storage;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.storage.BinaryStorageConfigurationDto;

/**
 * @author Edward P. Legaspi
 */
public class BinaryStorageConfigurationsResponseDto extends BaseResponse {

	private static final long serialVersionUID = 1796260917417235587L;

	@ApiModelProperty("List of binary storage configurations information")
	private List<BinaryStorageConfigurationDto> binaryStorageConfigurations;

	public List<BinaryStorageConfigurationDto> getBinaryStorageConfigurations() {
		return binaryStorageConfigurations;
	}

	public void setBinaryStorageConfigurations(List<BinaryStorageConfigurationDto> binaryStorageConfigurations) {
		this.binaryStorageConfigurations = binaryStorageConfigurations;
	}

}
