package org.meveo.api.rest.storage;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.rest.IBaseRs;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.service.storage.BinaryStorageUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing the file system.
 * 
 * @author Clément Bareth <clement.bareth@web-drone.fr>
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/fileSystem")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("FileSystemRs")
public interface FileSystemRs extends IBaseRs {

	/**
	 * Retrieve a binary from a repository
	 * 
	 * @param repositoryCode storage
	 * @param cetCode        custom entity code
	 * @param uuid           entity id
	 * @param cftCode        custom field template code
	 * 
	 * @return ActionStatus request status
	 */
	@GET
	@Path("/binaries/{repositoryCode}/{cetCode}/{uuid}/{cftCode}")
	@ApiOperation(value = "Find binary by index and code repository and code cet and uuid and code cft")
	Response findBinary(@QueryParam(BinaryStorageUtils.INDEX) @ApiParam("Index") Integer index,
			@PathParam("repositoryCode") @ApiParam("Code of the repository") String repositoryCode,
			@PathParam("cetCode") @ApiParam("Code of the custom entity template") String cetCode, @PathParam("uuid") @ApiParam("uuid") String uuid,
			@PathParam("cftCode") @ApiParam("Code of the custom field template") String cftCode)
			throws IOException, EntityDoesNotExistsException, BusinessApiException, org.meveo.api.exception.EntityDoesNotExistsException;
}
