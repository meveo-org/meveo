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

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.ListTechnicalServiceResponse;
import org.meveo.api.dto.response.TechnicalServiceResponse;
import org.meveo.api.dto.technicalservice.TechnicalServiceDto;
import org.meveo.api.rest.IBaseRs;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Technical service rest service
 *
 * @author Clément Bareth
 */
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface TechnicalServiceRs extends IBaseRs {

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
     * Create a new technical service.
     *
     * @return Request processing status
     */
    @Path("/")
    @POST
    ActionStatus create(TechnicalServiceDto postData);

    /**
     * Update a technical service.
     *
     * @return Request processing status
     */
    @Path("/")
    @PUT
    ActionStatus update(TechnicalServiceDto postData);

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
     * @param name Name filter
     * @return list of all technical services
     */
    @Path("/")
    @GET
    ListTechnicalServiceResponse list(@QueryParam("name") String name);

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
     * Create new or update an existing technical service.
     *
     * @param postData The technical service's data
     * @return Request processing status
     */
    @Path("/createOrUpdate")
    @POST
    ActionStatus createOrUpdate(TechnicalServiceDto postData);

    @Path("/rename")
    @PUT
    ActionStatus rename(@FormParam("oldName") String oldName, @FormParam("newName") String newName);

    @Path("/renameVersion/{name}")
    @PUT
    ActionStatus renameVersion(@PathParam("name") String name, @FormParam("oldVersion") Integer oldVersion, @FormParam("newVersion") Integer newVersion);

}
