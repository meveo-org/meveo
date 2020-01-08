package org.meveo.api.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.meveo.api.BaseApi;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.service.config.impl.MavenConfigurationService;

/**
 * @author Hien
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 * @since 6.5.0
 */
public class MavenConfigurationApi extends BaseApi {

	@Inject
	private MavenConfigurationService mavenConfigurationService;

	/**
	 * Upload an artifact in the maven configuration
	 *
	 * @param inputStream JAR file
	 * @param groupId     GroupId
	 * @param artifactId  ArtifactId
	 * @param version     Version
	 * @param classifier  Classifier
	 * @param filename    Name of the jar file
	 */
	public void uploadAnArtifact(InputStream inputStream, String groupId, String artifactId, String version, String classifier, String filename) throws BusinessApiException {
		String filePath = mavenConfigurationService.createDirectory(groupId, artifactId, version, classifier);
		filePath = filePath + File.separator + mavenConfigurationService.buildArtifactName(artifactId, version, classifier);

		try {
			OutputStream outputStream = new FileOutputStream(new File(filePath));
			int read = 0;
			byte[] data = new byte[1024];

			while ((read = inputStream.read(data)) != -1) {
				outputStream.write(data, 0, read);
			}

			inputStream.close();
			outputStream.flush();
			outputStream.close();

		} catch (Exception e) {
			throw new BusinessApiException("Error uploading file: " + filename + ". " + e.getMessage());
		}
	}
}