package org.meveo.api.dto.module;

import java.util.ArrayList;
import java.util.List;

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
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.ModuleLicenseEnum;

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
 * @version 6.9.0
 */
@XmlRootElement(name = "Module")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel("MeveoModuleDto")
public class MeveoModuleDto extends BaseDataModelDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

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
	
	/** The current version. */
	@ApiModelProperty("The current version")
	private String currentVersion = "1.0.0";
	
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

	/** The module items. */
	@ApiModelProperty("List of module items information")
	@XmlElementWrapper(name = "moduleItems")
	@XmlElement(name = "moduleItem")
	@JsonProperty("moduleItems")
	private List<MeveoModuleItemDto> moduleItems = new ArrayList<>();
		
	/** The module dependencies. */
	@ApiModelProperty("List of module dependencies information")
	@XmlElementWrapper(name = "moduleDependencies")
	@XmlElement(name = "moduleDependency")
	@JsonProperty("moduleDependencies")
	private List<ModuleDependencyDto> moduleDependencies;
	 
	@ApiModelProperty("List of patch linked to this module")
	@XmlElementWrapper(name = "patches")
	@XmlElement(name = "patch")
	@JsonProperty("patches")
	private List<MeveoModulePatchDto> patches;

	/** The module files. */
	@ApiModelProperty("The module files")
	@XmlElementWrapper(name = "moduleFiles")
	@XmlElement(name = "moduleFile")
	@JsonProperty("moduleFiles")
	private List<String> moduleFiles;
	
	private String repository;

	/**
	 * Instantiates a new meveo module dto.
	 */
	public MeveoModuleDto() {
		super();
	}

	/**
	 * Instantiates a new meveo module dto.
	 *
	 * @param meveoModule the meveo module
	 */
	public MeveoModuleDto(MeveoModule meveoModule) {
		super(meveoModule);
		this.license = meveoModule.getLicense();
		this.logoPicture = meveoModule.getLogoPicture();
		this.moduleItems = new ArrayList<>();
		this.moduleFiles = new ArrayList<>();
		this.moduleDependencies=new ArrayList<>();
		this.currentVersion = meveoModule.getCurrentVersion();
		this.isInDraft=meveoModule.getIsInDraft();
		if (meveoModule.getScript() != null) {
			this.setScript(new ScriptInstanceDto(meveoModule.getScript(), meveoModule.getScript().getScript()));
		}
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
	
	public void addDependency(MeveoModule module) {
		if (module == null) {
			return;
		}
		
		ModuleDependencyDto moduleDependencyDto = new ModuleDependencyDto(module.getCode(), module.getDescription(), module.getCurrentVersion());
		if (module.getGitRepository() != null && module.getGitRepository().isRemote()) {
			moduleDependencyDto.setGitUrl(module.getGitRepository().getRemoteOrigin());
			moduleDependencyDto.setGitBranch(module.getGitRepository().getDefaultBranch());
		}
		if (!moduleDependencies.contains(moduleDependencyDto)) {
			moduleDependencies.add(moduleDependencyDto);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.meveo.model.IEntity#isTransient()
	 */
	@Override
	public boolean isTransient() {
		return true;
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
 
	/**
	 * Checks if is code only.
	 *
	 * @return true, if is code only
	 */
	public boolean isCodeOnly() {
		return StringUtils.isBlank(getDescription()) && license == null && StringUtils.isBlank(logoPicture) && logoPictureFile == null && script == null
				&& (moduleItems == null || moduleItems.isEmpty() && (moduleFiles == null || moduleFiles.isEmpty()));
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		return String.format("ModuleDto [code=%s, license=%s, description=%s, logoPicture=%s, logoPictureFile=%s, isInDraft, moduleItems=%s, script=%s, moduleFiles=%s, moduleDependencies=%s]", getCode(), license, getDescription(),
				logoPicture, logoPictureFile,isInDraft, moduleItems != null ? moduleItems.subList(0, Math.min(moduleItems.size(), maxLen)) : null, script, moduleFiles,moduleDependencies);
	}

	public List<MeveoModulePatchDto> getPatches() {
		return patches;
	}

	public void setPatches(List<MeveoModulePatchDto> patches) {
		this.patches = patches;
	}

	public boolean isInDraft() {
		return isInDraft;
	}

	public void setInDraft(boolean isInDraft) {
		this.isInDraft = isInDraft;
	}

	/**
	 * @return the {@link #repository}
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
	
}