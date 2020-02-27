package org.meveo.api.dto.config;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * Maven configuration information.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
public class MavenConfigurationDto implements Serializable {

	private static final long serialVersionUID = -7629326572073747356L;

	@ApiModelProperty("Maven executable path")
	private String mavenExecutablePath;

	@ApiModelProperty("M2 folder path")
	private String m2FolderPath;

	@ApiModelProperty("List of maven repositories")
	private List<String> mavenRepositories;

	public String getMavenExecutablePath() {
		return mavenExecutablePath;
	}

	public void setMavenExecutablePath(String mavenExecutablePath) {
		this.mavenExecutablePath = mavenExecutablePath;
	}

	public List<String> getMavenRepositories() {
		return mavenRepositories;
	}

	public void setMavenRepositories(List<String> mavenRepositories) {
		this.mavenRepositories = mavenRepositories;
	}
}
