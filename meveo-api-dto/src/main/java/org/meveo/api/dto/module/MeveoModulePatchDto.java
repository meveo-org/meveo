package org.meveo.api.dto.module;

import java.io.Serializable;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.model.module.MeveoModulePatch;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since
 * @version
 */
public class MeveoModulePatchDto implements Comparable<MeveoModulePatchDto>, Serializable {

	private static final long serialVersionUID = -8359195486911778412L;

	@NotNull
	private String moduleCode;

	@NotNull
	private ScriptInstanceDto scriptInstance = new ScriptInstanceDto();

	@NotNull
	@Pattern(regexp = "^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$")
	private String sourceVersion;

	@NotNull
	@Pattern(regexp = "^(\\d+\\.)?(\\d+\\.)?(\\*|\\d+)$")
	private String targetVersion;

	public MeveoModulePatchDto() {

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
