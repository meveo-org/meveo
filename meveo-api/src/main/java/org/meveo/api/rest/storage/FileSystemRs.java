package org.meveo.api.rest.storage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.rest.IBaseRs;

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
	 * 
	 * @param showOnExplorer whether to show the file in explorer or not
	 * @param repositoryCode storage
	 * @param cetCode        custom entity code
	 * @param uuid           entity id
	 * @param cftCode        custom field template code
	 * @return ActionStatus request status
	 */
	@GET
	@Path("/binaries/{repositoryCode}/{cetCode}/{uuid}/{cftCode}")
	ActionStatus findBinary(@QueryParam("showOnExplorer") Boolean showOnExplorer,
							@PathParam("repositoryCode") String repositoryCode,
							@PathParam("cetCode") String cetCode,
							@PathParam("uuid") String uuid,
							@PathParam("cftCode") String cftCode);
}
