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

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.response.module.MeveoModuleDtoResponse;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.rest.IBaseRs;
import org.meveo.service.admin.impl.MeveoModuleFilters;

/**
 * JAX-RS interface for MeveoModule management
 * @author Clément Bareth
 * @lastModifiedVersion 6.3.0
 */
@Path("/module")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("Module")
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
    ActionStatus create(@ApiParam("Meveo module information") MeveoModuleDto moduleDto, @QueryParam("development") @ApiParam("Whether to development meveo module") @DefaultValue("false") boolean development);

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
    ActionStatus delete(@PathParam("code") @ApiParam("Code of the module") String code);

    /**
     * List all Meveo's modules
     *
     * @return A list of Meveo's modules
     */
    @GET
    @Path("/list")
    @ApiOperation(value = "List meveo module")
    Response list(@QueryParam("codesOnly") @ApiParam("Whether to codes only for list of modules") boolean codesOnly, @BeanParam @ApiParam("Meveo module filters information") MeveoModuleFilters filters);

    /**
     * Install Meveo module
     * 
     * @return Request processing status
     */
    @PUT
    @Path("/install")
    @ApiOperation(value = "Instance meveo module")
    ActionStatus install(@ApiParam("Meveo module information") MeveoModuleDto moduleDto);

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
    @GET
    @Path("/uninstall")
    @ApiOperation(value = "Uninstall meveo module")
    ActionStatus uninstall(@QueryParam("code") @ApiParam("Code of the meveo module") String code, @QueryParam("remove") @ApiParam("Whether to remove elements") @DefaultValue("false") boolean remove);

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
     * Add a business entity to a module
     *
     * @param moduleCode Code of the module to modify
     * @param itemCode  Code of the item to add
     * @param itemType Type of the item to add
     * @return the modified module
     */
    @POST()
    @Path("/{code}/items/add")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Add to module")
    MeveoModuleDto addToModule(@PathParam("code") @ApiParam("Code of the module to modify") String moduleCode, @FormParam("itemCode") @ApiParam("Code of the item to add") String itemCode, @FormParam("itemType") @ApiParam("Type of the item to add") String itemType) throws EntityDoesNotExistsException, BusinessException;

    /**
     * Remove a business entity from a module
     *
     * @param moduleCode Code of the module to modify
     * @param itemCode  Code of the item to remove
     * @param itemType Type of the item to remove
     * @return the modified module
     */
    @POST()
    @Path("/{code}/items/remove")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Remove from module")
    MeveoModuleDto removeFromModule(@PathParam("code") @ApiParam("Code of the module to modify") String moduleCode, @FormParam("itemCode") @ApiParam("Code of the item to remove") String itemCode, @FormParam("itemType") @ApiParam("Type of the item to remove") String itemType) throws EntityDoesNotExistsException, BusinessException;

	/**
	 * Forks a Meveo module
	 * 
	 * @return Request processing status
	 */
	@PUT
	@Path("/fork/{code}")
    @ApiOperation(value = "Fork meveo module by code")
	ActionStatus fork(@PathParam("code") @ApiParam("Code of the module") String moduleCode);
}