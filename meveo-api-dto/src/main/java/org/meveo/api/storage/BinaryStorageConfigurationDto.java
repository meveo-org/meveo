package org.meveo.api.storage;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.storage.BinaryStorageConfiguration;

/**
 * @author Edward P. Legaspi
 */
public class BinaryStorageConfigurationDto extends BaseEntityDto {

	private static final long serialVersionUID = 4380769845743134706L;

	@NotNull
	@Size(max = 255)
	@ApiModelProperty("Code of the binary storage")
	private String code;
	
	@NotNull
	@Size(max = 255)
	@ApiModelProperty("Root path")
	private String rootPath;
	
	public BinaryStorageConfigurationDto() {
		
	}

	public BinaryStorageConfigurationDto(BinaryStorageConfiguration entity) {
		code = entity.getCode();
		rootPath = entity.getRootPath();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}
}
