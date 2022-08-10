package org.meveo.api.rest.module.impl;

import io.swagger.annotations.ApiModelProperty;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class ModuleUploadForm {

    @FormParam("zipFile")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @ApiModelProperty("Zipped module with files")
    private InputStream data;

    @FormParam("filename")
    @PartType(MediaType.TEXT_PLAIN)
    private String filename;

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
