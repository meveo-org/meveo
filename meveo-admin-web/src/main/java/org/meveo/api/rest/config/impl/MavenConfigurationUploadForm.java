package org.meveo.api.rest.config.impl;

import io.swagger.annotations.ApiModelProperty;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class MavenConfigurationUploadForm {

    @FormParam("jarFile")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @ApiModelProperty("Zipped maven content")
    private InputStream data;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    private String filename;

    @FormParam("groupId")
    @PartType(MediaType.APPLICATION_JSON)
    private String groupId;

    @FormParam("artifactId")
    @PartType(MediaType.APPLICATION_JSON)
    private String artifactId;

    @FormParam("version")
    @PartType(MediaType.APPLICATION_JSON)
    private String version;

    @FormParam("classifier")
    @PartType(MediaType.APPLICATION_JSON)
    private String classifier;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }
}
