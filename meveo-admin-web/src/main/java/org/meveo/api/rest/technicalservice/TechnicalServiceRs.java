/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.api.rest.technicalservice;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.Form;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.ListTechnicalServiceResponse;
import org.meveo.api.dto.response.TechnicalServiceResponse;
import org.meveo.api.dto.technicalservice.ProcessDescriptionsDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceFilters;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.rest.IBaseRs;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Technical service rest service
 *
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("TechnicalServiceRs")
public interface TechnicalServiceRs<D extends TechnicalServiceDto> extends IBaseRs {

	/**
	 * Check if service exists
	 *
	 * @param name    Name of service to check
	 * @param version Version of service to check - optional
	 * @return true if specified service exists
	 */
	@Path("/{name}")
	@HEAD
	@ApiOperation(value = "Check exist technical service")
	Response exists(@PathParam("name") @ApiParam("Name of service to check") String name, @QueryParam("version") @ApiParam("Version of service to check") Integer version);

	/**
	 * Count the number of hits corresponding to the query
	 *
	 * @param filters Filters used to restrict results
	 * @return A {@link Response} containing the number of hits in the body
	 */
	@Path("/count")
	@GET
	@ApiOperation(value = "Count technical service")
	Response count(@Form @ApiParam("Filters used to restrict results") TechnicalServiceFilters filters);

	/**
	 * Retrieve the description of a technical service
	 *
	 * @param name    Name of the technical service
	 * @param version Version of the technical service - if not provided, will get
	 *                last version.
	 * @return A {@link Response} containing the description in the body
	 */
	@Path("/{name}/description")
	@GET
	@ApiOperation(value = "Description technical service")
	Response description(@PathParam("name") @ApiParam("Name of the technical service") String name,
			@QueryParam("version") @ApiParam("Version of the technical service") Integer version);

	/**
	 * Updat the description of a technical service
	 *
	 * @param name    Name of the technical service
	 * @param version Version of the technical service - if not provided, will
	 *                update last version.
	 * @param dtos    New description of the technical service
	 * @return A {@link Response} with status 200 if the update was a success
	 */
	@Path("/{name}/description")
	@PUT
	@ApiOperation(value = "Update description technical service")
	Response updateDescription(@PathParam("name") @ApiParam("Name of the technical service") String name,
			@QueryParam("version") @ApiParam("Version of the technical service") Integer version, @ApiParam("Process descriptions information") ProcessDescriptionsDto dtos);

	/**
	 * Create a new technical service.
	 *
	 * @return Request processing status
	 */
	@Path("/")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Create technical service")
	ActionStatus create(@ApiParam("Technical service information") D postData);

	/**
	 * Update a technical service.
	 *
	 * @return Request processing status
	 */
	@Path("/")
	@PUT
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update technical service")
	ActionStatus update(@ApiParam("Technical service information") D postData);

	/**
	 * Create new or update an existing technical service.
	 *
	 * @param postData The technical service's data
	 * @return Request processing status
	 */
	@Path("/createOrUpdate")
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Create or update technical service")
	ActionStatus createOrUpdate(@ApiParam("Technical service information") D postData);

	/**
	 * Search for technical service with a given name. If version is not specified,
	 * return the last version of it.
	 *
	 * @return technical service if exists
	 */
	@Path("/{name}")
	@GET
	@ApiOperation(value = "Find technical service by name and version or latest")
	TechnicalServiceResponse findByNameAndVersionOrLatest(@PathParam("name") @ApiParam("Name of the technical service") String name,
			@QueryParam("version") @ApiParam("Version of the technical service") Integer version);

	/**
	 * Retrieve a list of all technical services.
	 *
	 * @return list of all technical services
	 */
	@Path("/")
	@GET
	@ApiOperation(value = "List technical service")
	ListTechnicalServiceResponse list(@Form @ApiParam("Technical service filters information") TechnicalServiceFilters filters,
			@HeaderParam("If-Modified-Since") @ApiParam("Since date") Date sinceDate);

	/**
	 * Retrieves the names of all technical services
	 *
	 * @return List of all technical services names
	 */
	@Path("/list/names")
	@GET
	@ApiOperation(value = "Retrieves the names of all technical services")
	Response names();

	/**
	 * Retrieves the versions number of a technical services
	 *
	 * @param name Name of the technical service
	 * @return The versions number of the specified technical service
	 */
	@Path("/{name}/versions")
	@GET
	@ApiOperation(value = "Version technical service")
	Response versions(@PathParam("name") @ApiParam("Name of the technical service") String name);

	/**
	 * Retrieve a list of all versions of the technical service with the given name.
	 *
	 * @return list of all technical services
	 */
	@Path("/{name}/all")
	@GET
	@ApiOperation(value = "List technical service by name")
	ListTechnicalServiceResponse listByName(@PathParam("name") @ApiParam("Name of the technical service") String name);

	/**
	 * Remove a technical service with a given name. If version is provided, only
	 * delete the specified version. If no version is provided, delete all the
	 * versions.
	 *
	 * @return Action result
	 */
	@Path("/{name}")
	@DELETE
	@ApiOperation(value = "Remove technical service by name")
	ActionStatus remove(@PathParam("name") @ApiParam("Name of the technical service") String name,
			@QueryParam("version") @ApiParam("Version of the technical service") Integer version);

	@Path("/{name}/{version}/disable")
	@POST
	@ApiOperation(value = "Disable technical service")
	void disable(@PathParam("name") @ApiParam("Name of the technical service") String name, @PathParam("version") @ApiParam("Version of the technical service") Integer version)
			throws EntityDoesNotExistsException, BusinessException;

	@Path("/{name}/{version}/disable")
	@POST
	@ApiOperation(value = "Enable technical service")
	void enable(@PathParam("name") @ApiParam("Name of the technical service") String name, @ApiParam("Version of the technical service") @PathParam("version") Integer version)
			throws EntityDoesNotExistsException, BusinessException;
}
