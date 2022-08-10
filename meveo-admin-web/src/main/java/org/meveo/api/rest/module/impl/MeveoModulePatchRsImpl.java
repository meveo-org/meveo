package org.meveo.api.rest.module.impl;

import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.meveo.api.dto.module.MeveoModulePatchDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.module.MeveoModulePatchApi;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.module.MeveoModulePatch;

import io.swagger.annotations.Api;

/**
 * REST endpoint for managing module patch.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 * @see MeveoModulePatch
 */
@Interceptors({ WsRestApiInterceptor.class })
@Path("/module/patches")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("ModulePatchesRs")
public class MeveoModulePatchRsImpl extends BaseRs {

	@Inject
	private MeveoModulePatchApi modulePatchApi;

	/**
	 * Creates a module patch.
	 * 
	 * @param postData patch data
	 * @return request status with the created entity if successful
	 * @throws MeveoApiException exception when creation fails
	 */
	@POST
	public Response create(@Valid @NotNull MeveoModulePatchDto postData) throws MeveoApiException {

		try {
			return Response.status(Status.CREATED).entity(modulePatchApi.create(postData)).build();

		} catch (Exception e) {
			throw new MeveoApiException("Error creating modulePatch " + e.getMessage(), e);
		}
	}

	/**
	 * Deletes a module patch
	 * 
	 * @param moduleCode         code of module
	 * @param scriptInstanceCode code of script
	 * @param sourceVersion      the source version
	 * @param targetVersion      the target version
	 * @return request status
	 * @throws MeveoApiException when deletion fails
	 */
	@DELETE
	@Path("/{moduleCode}/{scriptInstanceCode}/{sourceVersion}/{targetVersion}")
	public Response delete(@PathParam("moduleCode") String moduleCode, @PathParam("scriptInstanceCode") String scriptInstanceCode, @PathParam("sourceVersion") String sourceVersion,
			@PathParam("targetVersion") String targetVersion) throws MeveoApiException {

		try {
			modulePatchApi.delete(moduleCode, scriptInstanceCode, sourceVersion, targetVersion);
			return Response.status(Status.NO_CONTENT).build();

		} catch (EntityDoesNotExistsException e) {
			return Response.status(Status.BAD_REQUEST).build();
		}
	}

	/**
	 * Searches for a module patch.
	 * 
	 * @param moduleCode         code of module
	 * @param scriptInstanceCode code of script
	 * @param sourceVersion      the source version
	 * @param targetVersion      the target version
	 * @return the patch entity if found
	 */
	@GET
	public Response find(@QueryParam("moduleCode") String moduleCode, @QueryParam("scriptInstanceCode") String scriptInstanceCode,
			@QueryParam("sourceVersion") String sourceVersion, @QueryParam("targetVersion") String targetVersion) {

		try {
			return Response.status(Status.OK).entity(modulePatchApi.find(moduleCode, scriptInstanceCode, sourceVersion, targetVersion)).build();

		} catch (EntityDoesNotExistsException e) {
			return Response.status(Status.NO_CONTENT).build();
		}
	}

	/**
	 * List all the patches associated with a given module.
	 * 
	 * @param moduleCode the code of the module
	 * @return list of patches associated to the given module
	 */
	@GET
	@Path("/{moduleCode}")
	public Response list(@PathParam("moduleCode") String moduleCode) {

		try {
			return Response.status(Status.OK).entity(modulePatchApi.list(moduleCode)).build();

		} catch (EntityDoesNotExistsException e) {
			return Response.status(Status.NO_CONTENT).build();
		}
	}
}
