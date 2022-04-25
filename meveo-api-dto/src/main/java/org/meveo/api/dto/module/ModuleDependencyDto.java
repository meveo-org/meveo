package org.meveo.api.dto.module;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
	private String code;

	@ApiModelProperty(value = "The module description")
	private String description;

	@XmlAttribute(required = true)
	@ApiModelProperty(required = true, value = "The module version")
	private String currentVersion;
	
	@ApiModelProperty(value = "The git url where the repository is stored")
	private String gitUrl;
	
	@ApiModelProperty(value = "The branch / tag of the git repository")
	private String gitBranch;
	
	@JsonIgnore
	private transient boolean installed;
	
	@JsonIgnore
	private transient boolean installing;
	
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

	/**
	 * @return the {@link #gitUrl}
	 */
	public String getGitUrl() {
		return gitUrl;
	}

	/**
	 * @param gitUrl the gitUrl to set
	 */
	public void setGitUrl(String gitUrl) {
		this.gitUrl = gitUrl;
	}

	/**
	 * @return the {@link #gitBranch}
	 */
	public String getGitBranch() {
		return gitBranch;
	}

	/**
	 * @param gitBranch the gitBranch to set
	 */
	public void setGitBranch(String gitBranch) {
		this.gitBranch = gitBranch;
	}

	/**
	 * @return the {@link #installed}
	 */
	public boolean isInstalled() {
		return installed;
	}

	/**
	 * @param installed the installed to set
	 */
	public void setInstalled(boolean installed) {
		this.installed = installed;
		if (installed) {
			this.installing = false;
		}
	}

	/**
	 * @return the {@link #installing}
	 */
	public boolean isInstalling() {
		return installing;
	}

	/**
	 * @param installing the installing to set
	 */
	public void setInstalling(boolean installing) {
		this.installing = installing;
	}

}