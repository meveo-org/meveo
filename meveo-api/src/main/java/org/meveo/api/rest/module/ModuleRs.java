/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.api.rest.module;

import java.io.File;
import java.util.List;

import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.response.module.MeveoModuleDtoResponse;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.export.ExportFormat;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.rest.module.impl.ModuleUploadForm;
import org.meveo.service.admin.impl.MeveoModuleFilters;
import org.meveo.service.admin.impl.ModuleUninstall;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * JAX-RS interface for MeveoModule management
 * 
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/module")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("ModuleRs")
public interface ModuleRs extends IBaseRs {

	/**
	 * Create a new meveo module
	 * 
	 * @param moduleDto The meveo module's data
	 * @return Request processing status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create meveo module")
	ActionStatus create(@ApiParam("Meveo module information") MeveoModuleDto moduleDto,
			@QueryParam("development") @ApiParam("Whether to development meveo module") @DefaultValue("false") boolean development);

	/**
	 * Update an existing Meveo module
	 * 
	 * @param moduleDto The Meveo module's data
	 * @return Request processing status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update meveo module")
	ActionStatus update(@ApiParam("Meveo module information") MeveoModuleDto moduleDto);

	/**
	 * Create new or update an existing Meveo module
	 * 
	 * @param moduleDto The Meveo module's data
	 * @return Request processing status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update meveo module")
	ActionStatus createOrUpdate(@ApiParam("Meveo module information") MeveoModuleDto moduleDto);

	/**
	 * Remove an existing module with a given code
	 * 
	 * @param code The module's code
	 * @return Request processing status
	 */
	@DELETE
	@Path("/{code}")
	@ApiOperation(value = "Remove meveo module by code")
	ActionStatus delete(@PathParam("code") @ApiParam("Code of the module") String code, @BeanParam ModuleUninstall moduleUninstall);

	/**
	 * List all Meveo's modules
	 *
	 * @return A list of Meveo's modules
	 */
	@GET
	@Path("/list")
	@ApiOperation(value = "List meveo module")
	Response list(@QueryParam("codesOnly") @ApiParam("Whether to codes only for list of modules") boolean codesOnly,
			@BeanParam @ApiParam("Meveo module filters information") MeveoModuleFilters filters);

	/**
	 * Install Meveo module
	 * 
	 * @return Request processing status
	 */
	@PUT
	@Path("/install")
	@ApiOperation(value = "Instance meveo module")
	ActionStatus install(@ApiParam("Meveo module information") MeveoModuleDto moduleDto, @QueryParam("repository") List<String> repositories);
	
	/**
	 * 
	 * @param code code of the Git repository
	 * @param repositories The codes of the repositories to install data
	 * @throws BusinessException
	 * @throws MeveoApiException
	 */
	@POST
	@Path("/install/from/git/{code}")
	void installFromGitRepository(@PathParam("code") String code, @QueryParam("repository") List<String> repositories) throws BusinessException, MeveoApiException;

	/**
	 * Find a Meveo's module with a given code
	 * 
	 * @param code The Meveo module's code
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Get meveo module by code")
	MeveoModuleDtoResponse get(@QueryParam("code") @ApiParam("Code of the meveo module") String code);

	/**
	 * Uninstall a Meveo's module with a given code
	 *
	 * @param code   The Meveo module's code
	 * @param remove Whether to remove elements
	 * @return Request processing status
	 */
	@GET //TODO: change to "DELETE" method
	@Path("/uninstall")
	@ApiOperation(value = "Uninstall meveo module")
	ActionStatus uninstall(@QueryParam("code") @ApiParam("Code of the meveo module") String code, //TODO: change to "path param"
			@QueryParam("remove") @ApiParam("Whether to remove elements") @DefaultValue("false") boolean remove);

	/**
	 * Enable a Meveo's module with a given code
	 * 
	 * @param code The Meveo module's code
	 * @return Request processing status
	 */
	@GET
	@Path("/enable")
	@ApiOperation(value = "Enable meveo module")
	ActionStatus enable(@QueryParam("code") @ApiParam("Code of the meveo module") String code);

	/**
	 * Disable a Meveo's module with a given code
	 * 
	 * @param code The Meveo module's code
	 * @return Request processing status
	 */
	@GET
	@Path("/disable")
	@ApiOperation(value = "Disable meveo module")
	ActionStatus disable(@QueryParam("code") @ApiParam("Code of the meveo module") String code);
	
	/**
	 * @param moduleCode
	 * @param itemCode
	 * @param itemType
	 * @return
	 * @throws EntityDoesNotExistsException
	 * @throws BusinessException
	 */
	@DELETE
	@Path("/{code}/items/{itemType}/{itemCode}")
	@ApiOperation(value = "Remove from module")
	MeveoModuleDto removeItemFromModule(
			@PathParam("code") @ApiParam("Code of the module to modify") String moduleCode,
			@PathParam("itemCode") @ApiParam("Code of the item to remove") String itemCode, 
			@PathParam("itemType") @ApiParam("Type of the item to remove") String itemType,
			@QueryParam("appliesTo") @ApiParam("Applies to expression") String appliesTo
		) throws EntityDoesNotExistsException, BusinessException;
	
	/**
	 * @param moduleCode
	 * @param itemCode
	 * @param itemType
	 * @return
	 * @throws EntityDoesNotExistsException
	 * @throws BusinessException
	 */
	@PUT
	@Path("/{code}/items/{itemType}/{itemCode}")
	@ApiOperation(value = "Remove from module")
	MeveoModuleDto addItemToModule(
			@PathParam("code") @ApiParam("Code of the module to modify") String moduleCode,
			@PathParam("itemCode") @ApiParam("Code of the item to remove") String itemCode, 
			@PathParam("itemType") @ApiParam("Type of the item to remove") String itemType,
			@QueryParam("appliesTo") @ApiParam("Applies to expression") String appliesTo
		) throws EntityDoesNotExistsException, BusinessException;

	/**
	 * Add a file/folder to a module
	 *
	 * @param moduleCode Code of the module to modify
	 * @param path   Path of file/folder
	 * @return the modified module
	 */
	@POST()
	@Path("/{code}/file/add")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "Add file to module")
	MeveoModuleDto addFileToModule(@PathParam("code") @ApiParam("Code of the module to modify") String moduleCode,
							   @FormParam("path") @ApiParam("Path of file/folder to add") String path)
			throws EntityDoesNotExistsException, BusinessException;

	/**
	 * Remove a file/folder from a module
	 *
	 * @param moduleCode Code of the module to modify
	 * @param path   Path of file/folder to remove
	 * @return the modified module
	 */
	@POST()
	@Path("/{code}/file/remove")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@ApiOperation(value = "Remove from module")
	MeveoModuleDto removeFileFromModule(@PathParam("code") @ApiParam("Code of the module to modify") String moduleCode,
										@FormParam("path") @ApiParam("Path of file/folder to remove") String path)
			throws EntityDoesNotExistsException, BusinessException;


	/**
	 * Forks a Meveo module
	 * 
	 * @return Request processing status
	 */
	@PUT
	@Path("/fork/{code}")
	@ApiOperation(value = "Fork meveo module by code")
	ActionStatus fork(@PathParam("code") @ApiParam("Code of the module") String moduleCode);

	/**
	 * Import a zipped module with files
	 *
	 * @param uploadForm  Upload module
	 * @param overwrite   Overwrite
	 */
	@POST
	@Path("/importZip")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@ApiOperation(value = "Import from a zip")
	void importZip(@GZIP @MultipartForm @NotNull @ApiParam("Upload module") ModuleUploadForm uploadForm, @ApiParam("Whether to overwrite existing data") @QueryParam("overwrite")  boolean overwrite) throws EntityDoesNotExistsException;

	/**
	 * Export module
	 *
	 * @param modulesCode List of the code meveo module
	 * @throws Exception 
	 */
	@GET
	@Path("/export")
	@ApiOperation(value = "Export to a file")
	File export(@QueryParam("modulesCode") @ApiParam("List of the code meveo module") List<String> modulesCode,@QueryParam("exportFormat") @ApiParam("Format of file") ExportFormat exportFormat) throws Exception;

	/**
	 * Release a Meveo module
	 *
	 * @return Request processing status
	 */
	@POST
	@Path("/release/{code}/{nextVersion}")
	@ApiOperation(value = "release meveo module by code")
	ActionStatus release(@PathParam("code") @ApiParam("Code of the module") String moduleCode, @PathParam("nextVersion") @ApiParam("next version meveo module") String nextVersion);
}