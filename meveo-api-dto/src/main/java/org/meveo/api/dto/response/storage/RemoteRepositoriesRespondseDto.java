package org.meveo.api.dto.response.storage;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.storage.RemoteRepositoryDto;

import java.util.List;

/**
 * @author Hien Bach
 */
public class RemoteRepositoriesRespondseDto extends BaseResponse {

    private static final long serialVersionUID = 7852072763882664077L;

    @ApiModelProperty("List of remote repositories information")
    private List<RemoteRepositoryDto> remoteRepositories;

    public List<RemoteRepositoryDto> getRemoteRepositories() {
        return remoteRepositories;
    }

    public void setRemoteRepositories(List<RemoteRepositoryDto> remoteRepositories) {
        this.remoteRepositories = remoteRepositories;
    }
}
