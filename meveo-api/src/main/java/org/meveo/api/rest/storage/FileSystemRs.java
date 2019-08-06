package org.meveo.api.rest.storage;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.rest.IBaseRs;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
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
	@Path("/binaries")
	ActionStatus findBinary(@QueryParam("showOnExplorer") Boolean showOnExplorer, @QueryParam("repositoryCode") String repositoryCode, @QueryParam("cetCode") String cetCode,
			@QueryParam("uuid") String uuid, @QueryParam("cftCode") String cftCode);
}
