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

import org.jboss.resteasy.annotations.Form;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.ListTechnicalServiceResponse;
import org.meveo.api.dto.response.TechnicalServiceResponse;
import org.meveo.api.dto.technicalservice.ProcessDescriptionsDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceDto;
import org.meveo.api.dto.technicalservice.TechnicalServiceFilters;
import org.meveo.api.rest.IBaseRs;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;

/**
 * Technical service rest service
 *
 * @author Clément Bareth
 */
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface TechnicalServiceRs<D extends TechnicalServiceDto> extends IBaseRs {

    /**
     * Check if service exists
     *
     * @param name Name of service to check
     * @param version Version of service to check - optional
     * @return true if specified service exists
     */
    @Path("/{name}")
    @HEAD
    Response exists(@PathParam("name") String name, @QueryParam("version") Integer version);

    /**
     * Count the number of hits corresponding to the query
     *
     * @param filters Filters used to restrict results
     * @return A {@link Response} containing the number of hits in the body
     */
    @Path("/count")
    @GET
    Response count(@Form TechnicalServiceFilters filters);

    /**
     * Retrieve the description of a technical service
     *
     * @param name Name of the technical service
     * @param version Version of the technical service - if not provided, will get last version.
     * @return A {@link Response} containing the description in the body
     */
    @Path("/{name}/description")
    @GET
    Response description(@PathParam("name") String name, @QueryParam("version") Integer version);

    /**
     * Updat the description of a technical service
     *
     * @param name Name of the technical service
     * @param version Version of the technical service - if not provided, will update last version.
     * @param dtos New description of the technical service
     * @return A {@link Response} with status 200 if the update was a success
     */
    @Path("/{name}/description")
    @PUT
    Response updateDescription(@PathParam("name") String name, @QueryParam("version") Integer version, ProcessDescriptionsDto dtos);

    /**
     * Create a new technical service.
     *
     * @return Request processing status
     */
    @Path("/")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    ActionStatus create(D postData);

    /**
     * Update a technical service.
     *
     * @return Request processing status
     */
    @Path("/")
    @PUT
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    ActionStatus update(D postData);

    /**
     * Create new or update an existing technical service.
     *
     * @param postData The technical service's data
     * @return Request processing status
     */
    @Path("/createOrUpdate")
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    ActionStatus createOrUpdate(D postData);

    /**
     * Search for technical service with a given name.
     * If version is not specified, return the last version of it.
     *
     * @return technical service if exists
     */
    @Path("/{name}")
    @GET
    TechnicalServiceResponse findByNameAndVersionOrLatest(@PathParam("name") String name, @QueryParam("version") Integer version);

    /**
     * Retrieve a list of all technical services.
     *
     * @return list of all technical services
     */
    @Path("/")
    @GET
    Response list(@Form TechnicalServiceFilters filters, @HeaderParam("If-Modified-Since") Date sinceDate);

    /**
     * Retrieves the names of all technical services
     *
     * @return List of all technical services names
     */
    @Path("/list/names")
    @GET
    Response names();

    /**
     * Retrieves the versions number of a technical services
     *
     * @param name Name of the technical service
     * @return The versions number of the specified technical service
     */
    @Path("/{name}/versions")
    @GET
    Response versions(@PathParam("name") String name);

    /**
     * Retrieve a list of all versions of the technical service with the given name.
     *
     * @return list of all technical services
     */
    @Path("/{name}/all")
    @GET
    ListTechnicalServiceResponse listByName(@PathParam("name") String name);

    /**
     * Remove a technical service with a given name.
     * If version is provided, only delete the specified version.
     * If no version is provided, delete all the versions.
     *
     * @return Action result
     */
    @Path("/{name}")
    @DELETE
    ActionStatus remove(@PathParam("name") String name, @QueryParam("version") Integer version);

    /**
     * Update the name of a technical service
     *
     * @param oldName Service name to update
     * @param newName New name of the service
     * @return
     */
    @Path("/rename")
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    ActionStatus rename(@FormParam("oldName") String oldName, @FormParam("newName") String newName);

    /**
     * Re-number the specified version
     *
     * @param name Name of the technical service to update
     * @param oldVersion Version number to update
     * @param newVersion New version number to give
     * @return
     */
    @Path("/renameVersion/{name}")
    @PUT
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    ActionStatus renameVersion(@PathParam("name") String name, @FormParam("oldVersion") Integer oldVersion, @FormParam("newVersion") Integer newVersion);

}
