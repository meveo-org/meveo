package org.meveo.api.dto.module;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringUtils;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.ModuleLicenseEnum;
import org.meveo.model.module.ModuleRelease;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A module is a collection of objects that can be classes, scripts,
 * notifications, custom fields, etc that can be exported and imported on
 * another meveo instance.
 * 
 * @author andrius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "ModuleRelease")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("ModuleReleaseDto")
public class ModuleReleaseDto {

	/** The code. */
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The module release code")
	protected String code;
	
	@XmlAttribute(required = false)
	@ApiModelProperty(required = false, value = "The module release description")
	protected String description;

	/** The license. */
	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The license type")
	private ModuleLicenseEnum license;

	/** The logo picture. */
	@ApiModelProperty("The logo picture")
	private String logoPicture;

	/** The logo picture file. */
	@ApiModelProperty("The logo picture file")
	private byte[] logoPictureFile;

	/** The script. */
	@ApiModelProperty("Script instance information")
	private ScriptInstanceDto script;

	/** The module items. */
	@ApiModelProperty("List of module items information")
	@XmlElementWrapper(name = "moduleItems")
	@XmlElement(name = "moduleItem")
	@JsonProperty("moduleItems")
	private List<MeveoModuleItemDto> moduleItems;

	/** The module files. */
	@XmlElementWrapper(name = "List of module files information")
	@XmlElement(name = "moduleFiles")
	@ApiModelProperty("The module files")
	private List<String> moduleFiles;

	/** The release version. */
	@ApiModelProperty("The release version")
	private String currentVersion;

	/** is in draft */
	@ApiModelProperty("is in draft")
	private boolean isInDraft = true;

	/** The meveo version base. */
	@ApiModelProperty("The meveo version base")
	@Pattern(regexp = "^(?<major>\\d+\\.)?(?<minor>\\d+\\.)?(?<patch>\\*|\\d+)$")
	private String meveoVersionBase;

	/** The meveo version ceiling. */
	@ApiModelProperty("The meveo version ceiling")
	@Pattern(regexp = "^(?<major>\\d+\\.)?(?<minor>\\d+\\.)?(?<patch>\\*|\\d+)$")
	private String meveoVersionCeiling;

	/** The module dependencies. */
	@ApiModelProperty("List of module dependencies information")
	@XmlElementWrapper(name = "moduleDependencies")
	@XmlElement(name = "moduleDependencies")
	@JsonProperty("moduleDependencies")
	private List<ModuleDependencyDto> moduleDependencies = new ArrayList<>();

	private List<MeveoModulePatchDto> patches = new ArrayList<>();

	/**
	 * Instantiates a new meveo module dto.
	 */
	public ModuleReleaseDto() {
		super();
	}

	/**
	 * Instantiates a new meveo module dto.
	 *
	 * @param moduleRelease the module release
	 */
	public ModuleReleaseDto(ModuleRelease moduleRelease) {
		this.license = moduleRelease.getLicense();
		this.logoPicture = moduleRelease.getLogoPicture();
		this.moduleItems = new ArrayList<>();
		this.moduleFiles = new ArrayList<>();
		this.moduleDependencies=new ArrayList<>();
		if (moduleRelease.getScript() != null) {
			this.setScript(new ScriptInstanceDto(moduleRelease.getScript(), moduleRelease.getScript().getScript()));
		}
		
		if (moduleRelease.getMeveoModule().getPatches() != null && !moduleRelease.getMeveoModule().getPatches().isEmpty()) {
			patches = moduleRelease.getMeveoModule().getPatches().stream().map(e -> new MeveoModulePatchDto(e)).collect(Collectors.toList());
		}
	}

	/**
	 * Gets the code.
	 *
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * Sets the code.
	 *
	 * @param code the new code
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * Gets the license.
	 *
	 * @return the license
	 */
	public ModuleLicenseEnum getLicense() {
		return license;
	}

	/**
	 * Sets the license.
	 *
	 * @param license the new license
	 */
	public void setLicense(ModuleLicenseEnum license) {
		this.license = license;
	}

	/**
	 * Gets the logo picture.
	 *
	 * @return the logo picture
	 */
	public String getLogoPicture() {
		return logoPicture;
	}

	/**
	 * Sets the logo picture.
	 *
	 * @param logoPicture the new logo picture
	 */
	public void setLogoPicture(String logoPicture) {
		this.logoPicture = logoPicture;
	}

	/**
	 * Gets the logo picture file.
	 *
	 * @return the logo picture file
	 */
	public byte[] getLogoPictureFile() {
		return logoPictureFile;
	}

	/**
	 * Sets the logo picture file.
	 *
	 * @param logoPictureFile the new logo picture file
	 */
	public void setLogoPictureFile(byte[] logoPictureFile) {
		this.logoPictureFile = logoPictureFile;
	}

	/**
	 * Gets the module items.
	 *
	 * @return the module items
	 */
	public List<MeveoModuleItemDto> getModuleItems() {
		return moduleItems;
	}

	/**
	 * Sets the module items.
	 *
	 * @param moduleItems the new module items
	 */
	public void setModuleItems(List<MeveoModuleItemDto> moduleItems) {
		this.moduleItems = moduleItems;
	}

	/**
	 * Adds the module item.
	 *
	 * @param item the item
	 */
	public void addModuleItem(BaseEntityDto item) {
		MeveoModuleItemDto meveoModuleItemDto = new MeveoModuleItemDto(item.getClass().getName(), item);
		if (!moduleItems.contains(meveoModuleItemDto)) {
			moduleItems.add(meveoModuleItemDto);
		}
	}

	/**
	 * Gets the module files.
	 *
	 * @return the module files
	 */
	public List<String> getModuleFiles() {
		return moduleFiles;
	}

	/**
	 * Sets the module files.
	 *
	 * @param moduleFiles the new module files
	 */
	public void setModuleFiles(List<String> moduleFiles) {
		this.moduleFiles = moduleFiles;
	}

	/**
	 * Adds the module file.
	 *
	 * @param path the path file/folder
	 */
	public void addModuleFile(String path) {
		if (!moduleFiles.contains(path)) {
			moduleFiles.add(path);
		}
	}

	/**
	 * Gets the current version.
	 *
	 * @return the current version
	 */
	public String getCurrentVersion() {
		return currentVersion;
	}

	/**
	 * Sets the current version.
	 *
	 * @param currentVersion the new current version
	 */
	public void setCurrentVersion(String currentVersion) {
		this.currentVersion = currentVersion;
	}

	/**
	 * Gets the meveo version base.
	 *
	 * @return the meveo version base
	 */
	public String getMeveoVersionBase() {
		return meveoVersionBase;
	}

	/**
	 * Sets the meveo version base.
	 *
	 * @param meveoVersionBase the new meveo version base
	 */
	public void setMeveoVersionBase(String meveoVersionBase) {
		this.meveoVersionBase = meveoVersionBase;
	}

	/**
	 * Gets the meveo version ceiling.
	 *
	 * @return the meveo version ceiling
	 */
	public String getMeveoVersionCeiling() {
		return meveoVersionCeiling;
	}

	/**
	 * Sets the meveo version ceiling.
	 *
	 * @param meveoVersionCeiling the new meveo version ceiling
	 */
	public void setMeveoVersionCeiling(String meveoVersionCeiling) {
		this.meveoVersionCeiling = meveoVersionCeiling;
	}

	/**
	 * Gets the script.
	 *
	 * @return the script
	 */
	public ScriptInstanceDto getScript() {
		return script;
	}

	/**
	 * Sets the script.
	 *
	 * @param script the new script
	 */
	public void setScript(ScriptInstanceDto script) {
		this.script = script;
	}

	public boolean isInDraft() {
		return isInDraft;
	}

	public void setInDraft(boolean inDraft) {
		isInDraft = inDraft;
	}

	public List<ModuleDependencyDto> getModuleDependencies() {
		return moduleDependencies;
	}

	public void setModuleDependencies(List<ModuleDependencyDto> moduleDependencies) {
		this.moduleDependencies = moduleDependencies;
	}

	/**
	 * Adds the module dependency.
	 */
	public void addModuleDependency(MeveoModuleDependency dependency) {
		ModuleDependencyDto moduleDependencyDto = new ModuleDependencyDto(dependency.getCode(),dependency.getDescription(),dependency.getCurrentVersion());
		if (!moduleDependencies.contains(moduleDependencyDto)) {
			moduleDependencies.add(moduleDependencyDto);
		}
	}


	/**
	 * Checks if is code only.
	 *
	 * @return true, if is code only
	 */
	public boolean isCodeOnly() {
		return license == null && StringUtils.isBlank(logoPicture) && logoPictureFile == null && script == null
				&& (moduleItems == null || moduleItems.isEmpty() && (moduleFiles == null || moduleFiles.isEmpty()) && currentVersion == null);
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return String.format("ModuleDto [code=%s, license=%s, description=%s, logoPicture=%s, logoPictureFile=%s, moduleItems=%s, script=%s, moduleFiles=%s]", getCode(), license,
				logoPicture, logoPictureFile, moduleItems != null ? moduleItems.subList(0, Math.min(moduleItems.size(), maxLen)) : null, script, moduleFiles);
	}

	public List<MeveoModulePatchDto> getPatches() {
		return patches;
	}

	public void setPatches(List<MeveoModulePatchDto> patches) {
		this.patches = patches;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}