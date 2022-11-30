/**
 * 
 */
package org.meveo.commons.utils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.meveo.model.storage.RemoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author ClementBareth
 * @since 
 * @version
 */
public class MavenUtils {
	private static Logger LOG = LoggerFactory.getLogger(MavenUtils.class);
	
	public static Model readModel(File pomFile) {
		if (!pomFile.exists()) {
			return new Model();
		}
		
		MavenXpp3Reader mavenreader = new MavenXpp3Reader();
		try (FileReader reader = new FileReader(pomFile)) {
			Model model = mavenreader.read(reader);
			model.setPomFile(pomFile);
			return model;
		} catch (IOException | XmlPullParserException e2) {
			LOG.error("Failed to pom file", e2);
			return new Model();
		}
	}
	
	public static void addOrUpdateDependency(Model model, Dependency dependency) {
		model.getDependencies().stream()
			.filter(d -> d.getGroupId().equals(dependency.getGroupId()))
			.filter(d -> d.getArtifactId().equals(dependency.getArtifactId()))
			.findFirst()
			.ifPresentOrElse(d -> d.setVersion(dependency.getVersion()), () -> model.addDependency(dependency));
	}
	
	public static void addOrUpdateDependency(DependencyManagement model, Dependency dependency) {
		model.getDependencies().stream()
			.filter(d -> d.getGroupId().equals(dependency.getGroupId()))
			.filter(d -> d.getArtifactId().equals(dependency.getArtifactId()))
			.findFirst()
			.ifPresentOrElse(d -> d.setVersion(dependency.getVersion()), () -> model.addDependency(dependency));
	}
	
	public static void addRepository(Model model, RemoteRepository repository) {
		boolean exists = model.getRepositories().stream().anyMatch(r -> r.getId().equals(repository.getCode()));
		if (!exists) {
			Repository repositoryMaven = new Repository();
			repositoryMaven.setId(repository.getCode());
			repositoryMaven.setUrl(repository.getUrl());
			model.addRepository(repositoryMaven);
		}
	}
	
	public static void addRepository(Model model, Repository repository) {
		boolean exists = model.getRepositories().stream().anyMatch(r -> r.getId().equals(repository.getId()));
		if (!exists) {
			model.addRepository(repository);
		}
	}
}
