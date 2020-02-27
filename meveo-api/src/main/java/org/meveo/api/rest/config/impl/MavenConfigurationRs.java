package org.meveo.api.rest.config.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.meveo.api.config.MavenConfigurationApi;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.config.MavenConfigurationDto;
import org.meveo.api.dto.config.MavenConfigurationResponseDto;
import org.meveo.api.dto.response.storage.RemoteRepositoriesRespondseDto;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.storage.RemoteRepositoryDto;
import org.meveo.service.config.impl.MavenConfigurationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing the maven configuration to which this application will pull
 * jars that will be use as dependencies when building scripts or functions.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@RequestScoped
@Interceptors({ WsRestApiInterceptor.class })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Path("/mavenConfiguration")
@Api("MavenConfigurationRs")
public class MavenConfigurationRs extends BaseRs {

	@Inject
	private MavenConfigurationService mavenConfigurationService;

	@Inject
	private MavenConfigurationApi mavenConfigurationApi;

	/**
	 * Create or update the maven configuration.
	 * 
	 * @param postData maven configuration values
	 * @return status of the request
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create or update maven configuration")
	public ActionStatus createOrUpdate(@ApiParam("Maven configuration information") MavenConfigurationDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			mavenConfigurationService.setMavenRepositories(postData.getMavenRepositories());
			mavenConfigurationService.saveConfiguration();

		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	@GET
	@Path("/")
	@ApiOperation(value = "Get maven configuration")
	public MavenConfigurationResponseDto getConfiguration() {

		MavenConfigurationResponseDto result = new MavenConfigurationResponseDto();
		result.getMavenConfiguration().setMavenRepositories(mavenConfigurationService.getMavenRepositories());

		return result;
	}

	/**
	 * Upload a new artifact in the maven configuration.
	 *
	 * @param uploadForm maven configuration upload values
	 */
	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation("Upload a new artifact")
	public void uploadAnArtifact(@MultipartForm @ApiParam("Upload form") @NotNull MavenConfigurationUploadForm uploadForm) throws Exception {
		mavenConfigurationApi.uploadAnArtifact(uploadForm.getData(), uploadForm.getGroupId(), uploadForm.getArtifactId(), uploadForm.getVersion(), uploadForm.getClassifier(),
				uploadForm.getFilename());
	}

	/**
	 * Create or update the remote repository.
	 *
	 * @param postData remote repository values
	 * @return status of the request
	 */
	@POST
	@Path("/remoteRepository")
	@ApiOperation(value = "Create or update remote repository")
	public ActionStatus createOrUpdate(@ApiParam("Remote repository information") RemoteRepositoryDto postData) {

		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");
		try {
			mavenConfigurationApi.createOrUpdate(postData);
		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}

	/**
	 * List remote repositories
	 *
	 * @return A list of remote repositories
	 */
	@GET
	@Path("/remoteRepository")
	@ApiOperation(value = "List of remote repositories")
	public RemoteRepositoriesRespondseDto list() {
		RemoteRepositoriesRespondseDto result = new RemoteRepositoriesRespondseDto();
		try {
			result.setRemoteRepositories(mavenConfigurationApi.list());
		} catch (Exception e) {
			processException(e, result.getActionStatus());
		}

		return result;
	}

	/**
	 * Remove an existing remote repository with a given code
	 *
	 * @param code The code of remote repository
	 * @return Request processing status
	 */
	@DELETE
	@Path("/remoteRepository/{code}")
	@ApiOperation(value = "Remove remote repository by code")
	public ActionStatus remove(@PathParam("code") @ApiParam("Code of the remote repository") String code) {
		ActionStatus result = new ActionStatus(ActionStatusEnum.SUCCESS, "");

		try {
			mavenConfigurationApi.remove(code);
		} catch (Exception e) {
			processException(e, result);
		}

		return result;
	}
}
