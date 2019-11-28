package org.meveo.api.config;

import org.meveo.api.BaseApi;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.service.config.impl.MavenConfigurationService;

import javax.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MavenConfigurationApi extends BaseApi {

    @Inject
    private MavenConfigurationService mavenConfigurationService;

    public void uploadAnArtifact(InputStream inputStream, String groupId, String artifactId, String version, String classifier, String filename) throws BusinessApiException {
        String filePath = mavenConfigurationService.createDirectory(groupId, artifactId, version, classifier);
        filePath = filePath + File.separator + filename;

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