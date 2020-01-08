package org.meveo.api.dto.response.storage;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import org.meveo.api.dto.response.BaseResponse;
import org.meveo.api.storage.RepositoryDto;

/**
 * @author Edward P. Legaspi
 */
public class RepositoriesResponseDto extends BaseResponse {

	private static final long serialVersionUID = 7852072763882664077L;

	@ApiModelProperty("List of repositories information")
	private List<RepositoryDto> repositories;

	public List<RepositoryDto> getRepositories() {
		return repositories;
	}

	public void setRepositories(List<RepositoryDto> repositories) {
		this.repositories = repositories;
	}
}
