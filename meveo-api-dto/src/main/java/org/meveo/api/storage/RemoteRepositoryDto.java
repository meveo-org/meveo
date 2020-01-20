package org.meveo.api.storage;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.model.storage.RemoteRepository;

import java.io.Serializable;

/**
 * @author Hien Bach
 * @lastModifiedVersion 6.7.0
 */
public class RemoteRepositoryDto implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("Code of remote repisitory")
    private String code;

    @ApiModelProperty("The url of remote repisitory")
    private String url;

    public RemoteRepositoryDto() {

    }

    /**
     * Instantiates a new remote repository dto.
     *
     * @param remoteRepository the remote repository entity
     */
    public RemoteRepositoryDto(RemoteRepository remoteRepository) {
        this.code = remoteRepository.getCode();
        this.url = remoteRepository.getUrl();
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
