package org.meveo.api.dto.module;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Mbarek
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.9.0
 */
@XmlRootElement(name = "ModuleDependency")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("ModuleDependencyDto")
public class ModuleDependencyDto extends BaseEntityDto {

	private static final long serialVersionUID = -4414899106611457890L;

	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The module  code")
	protected String code;

	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The module description")
	protected String description;

	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The current version")
	protected String currentVersion;

	public ModuleDependencyDto() {
		super();
	}

	public ModuleDependencyDto(String code, String description, String currentVersion) {
		super();
		this.code = code;
		this.description = description;
		this.currentVersion = currentVersion;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

}