package org.meveo.api.dto.module;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModulePatch;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.9.0
 */
@XmlRootElement(name = "MeveoModulePatch")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("MeveoModulePatchDto")
public class MeveoModulePatchDto implements Comparable<MeveoModulePatchDto>, Serializable {

	private static final long serialVersionUID = -8359195486911778412L;

	@NotNull
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The module  code")
	private String moduleCode;

	@NotNull
	@XmlElement(required = true)
	@ApiModelProperty(required = true, value = "Script executed before and after this patch is applied.")
	private ScriptInstanceDto scriptInstance = new ScriptInstanceDto();

	@NotNull
	@Pattern(regexp = MeveoModule.VERSION_PATTERN)
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "This patch can be apply from this version")
	private String sourceVersion;

	@NotNull
	@Pattern(regexp = MeveoModule.VERSION_PATTERN)
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "This patch can be apply up to this version")
	private String targetVersion;

	public MeveoModulePatchDto() {
		super();
	}

	public MeveoModulePatchDto(MeveoModulePatch e) {

		moduleCode = e.getMeveoModulePatchId().getMeveoModule().getCode();
		sourceVersion = e.getMeveoModulePatchId().getSourceVersion();
		targetVersion = e.getMeveoModulePatchId().getTargetVersion();
		if (e.getMeveoModulePatchId().getScriptInstance() != null) {
			scriptInstance.setCode(e.getMeveoModulePatchId().getScriptInstance().getCode());
		}
	}

	@JsonIgnore
	public int getSourceVersionAsInt() {
		return Integer.parseInt(sourceVersion.replace(".", ""));
	}

	@JsonIgnore
	public int getTargetVersionAsInt() {
		return Integer.parseInt(targetVersion.replace(".", ""));
	}

	@Override
	public int compareTo(MeveoModulePatchDto o) {
		return o.getSourceVersionAsInt() - getSourceVersionAsInt();
	}

	@Override
	public String toString() {
		return "MeveoModulePatchDto [moduleCode=" + moduleCode + ", scriptInstance=" + scriptInstance + ", sourceVersion=" + sourceVersion + ", targetVersion=" + targetVersion
				+ "]";
	}

	public String getModuleCode() {
		return moduleCode;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	public String getSourceVersion() {
		return sourceVersion;
	}

	public void setSourceVersion(String sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	public String getTargetVersion() {
		return targetVersion;
	}

	public void setTargetVersion(String targetVersion) {
		this.targetVersion = targetVersion;
	}

	public ScriptInstanceDto getScriptInstance() {
		return scriptInstance;
	}

	public void setScriptInstance(ScriptInstanceDto scriptInstance) {
		this.scriptInstance = scriptInstance;
	}
}
