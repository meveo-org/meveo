package org.meveo.api.storage;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.BaseEntity;
import org.meveo.model.storage.RemoteRepository;

import java.io.Serializable;

/**
 * @author Hien Bach
 * @author Edward P. Legaspi
 * @version 6.15
 */
public class RemoteRepositoryDto extends BusinessEntityDto {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("The url of remote repository")
    private String url;

    public RemoteRepositoryDto() {

    }

    /**
     * Instantiates a new remote repository dto.
     *
     * @param remoteRepository the remote repository entity
     */
    public RemoteRepositoryDto(RemoteRepository remoteRepository) {
        super(remoteRepository);
        url = remoteRepository.getUrl();
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
