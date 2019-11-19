package org.meveo.service.config.impl;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.commons.utils.ParamBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
		locator.addService( TransporterFactory.class, FileTransporterFactory.class );
		locator.addService( TransporterFactory.class, HttpTransporterFactory.class );
		return locator.getService( RepositorySystem.class );
	}

	public RepositorySystemSession newRepositorySystemSession(RepositorySystem system) {
		DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		LocalRepository localRepo = new LocalRepository( getM2FolderPath() );
		session.setLocalRepositoryManager(system.newLocalRepositoryManager( session, localRepo ) );

		return session;
	}
}
