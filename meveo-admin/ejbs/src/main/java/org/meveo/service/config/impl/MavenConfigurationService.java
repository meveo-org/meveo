package org.meveo.service.config.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.commons.utils.ParamBean;

/**
 * Manage the maven configuration.
 * 
 * <pre>
 * mavenExecutablePath - path to the local maven executable
 * m2FolderPath - path to the local .m2 folder
 * mavenRepositories - list of maven repositories
 * </pre>
 * 
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.5.0
 */
public class MavenConfigurationService implements Serializable {

	private static final long serialVersionUID = 7814020640577283116L;

	public String getM2FolderPath() {
		return ParamBean.getInstance().getProperty("maven.path.m2", null);
	}

	public void setM2FolderPath(String m2FolderPath) {
		ParamBean.getInstance().setProperty("maven.path.m2", m2FolderPath);
	}

	public List<String> getMavenRepositories() {
		return ParamBean.getInstance().getListProperty("maven.path.repositories", new ArrayList<String>());
	}

	public void setMavenRepositories(List<String> mavenRepositories) {
		ParamBean.getInstance().setListProperty("maven.path.repositories", mavenRepositories);
	}

	public void saveConfiguration() {
		ParamBean.getInstance().saveProperties();
	}

	public void saveConfiguration(MavenConfigurationDto mavenConfiguration) {

		setM2FolderPath(mavenConfiguration.getM2FolderPath());
		setMavenRepositories(mavenConfiguration.getMavenRepositories());

		saveConfiguration();
	}

	public MavenConfigurationDto loadConfig() {

		MavenConfigurationDto result = new MavenConfigurationDto();
		result.setM2FolderPath(getM2FolderPath());
		result.setMavenRepositories(getMavenRepositories());

		return result;
	}
}
