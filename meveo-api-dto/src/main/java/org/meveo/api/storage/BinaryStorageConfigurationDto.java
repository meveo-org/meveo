package org.meveo.api.storage;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.storage.BinaryStorageConfiguration;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * This class holds the configuration for binary storage. A binary storage
 * stores files in local directory.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@ApiModel
public class BinaryStorageConfigurationDto extends BaseEntityDto {

	private static final long serialVersionUID = 4380769845743134706L;

	/**
	 * Code of the binary storage
	 */
	@NotNull
	@Size(max = 255)
	@ApiModelProperty("Code of the binary storage")
	private String code;

	/**
	 * Root path
	 */
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
