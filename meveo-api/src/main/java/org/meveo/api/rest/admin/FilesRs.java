package org.meveo.api.rest.admin;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.admin.GetFilesResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.rest.admin.impl.FileUploadForm;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing the app file system preset in a specific folder.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/admin/files")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("FilesRs")
public interface FilesRs extends IBaseRs {

	@GET
	@Path("/all")
	@ApiOperation(value = "List of files")
	GetFilesResponseDto listFiles();

	@GET
	@Path("/")
	@ApiOperation(value = "List directory from where to list files")
	GetFilesResponseDto listFiles(@QueryParam("dir") @ApiParam("Directory from where to list files") String dir);

	@POST
	@Path("/createDir")
	@ApiOperation(value = "Create directory from where to list files")
	ActionStatus createDir(@ApiParam("Directory from where to list files") String dir);

	@POST
	@Path("/zipFile")
	@ApiOperation(value = "Zip file")
	ActionStatus zipFile(@ApiParam("File to be zipped") String file);

	@POST
	@Path("/zipDirectory")
	@ApiOperation(value = "Zip directory")
	ActionStatus zipDir(@ApiParam("Directory to be zipped") String dir);

	@POST
	@Path("/suppressFile")
	@ApiOperation(value = "Suppress file")
	ActionStatus suppressFile(@ApiParam("File to be suppressed") String file);

	@POST
	@Path("/suppressDirectory")
	@ApiOperation(value = "Suppress directory")
	ActionStatus suppressDir(@ApiParam("Directory to be suppressed") String dir);

	@POST
	@Path("/upload")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Upload file")
	ActionStatus uploadFile(@MultipartForm @ApiParam("Upload form") FileUploadForm form);

	@GET
	@Path("/downloadFile")
	@ApiOperation(value = "Download file")
	ActionStatus downloadFile(@QueryParam("file") @ApiParam("File to be downloaded") String file);

	@PUT
	@Path("/renameFolder")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "Rename folder")
	ActionStatus renameFolder(@FormParam("oldFolder") @ApiParam("Old folder") String oldFolder,@FormParam("newFolder") @ApiParam("New folder") String newFolder);

}
