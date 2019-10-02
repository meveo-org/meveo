package org.meveo.api.rest.storage;

import java.io.IOException;

import javax.ws.rs.BeanParam;
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

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @author Clément Bareth <clement.bareth@web-drone.fr>
 */
@Path("/fileSystem")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface FileSystemRs extends IBaseRs {

	/**
	 * Retrieve a binary from a repository
	 * @param repositoryCode storage
	 * @param cetCode        custom entity code
	 * @param uuid           entity id
	 * @param cftCode        custom field template code
	 * 
	 * @return ActionStatus request status
	 */
	@GET
	@Path("/binaries/{repositoryCode}/{cetCode}/{uuid}/{cftCode}")
    Response findBinary(@QueryParam(BinaryStorageUtils.INDEX) Integer index,
						@PathParam("repositoryCode") String repositoryCode,
						@PathParam("cetCode") String cetCode,
						@PathParam("uuid") String uuid,
						@PathParam("cftCode") String cftCode) throws IOException, EntityDoesNotExistsException, BusinessApiException, org.meveo.api.exception.EntityDoesNotExistsException;
}
