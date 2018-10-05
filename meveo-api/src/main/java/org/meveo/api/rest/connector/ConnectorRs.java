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
package org.meveo.api.rest.connector;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ConnectorDto;
import org.meveo.api.dto.response.GetConnectorResponse;
import org.meveo.api.dto.response.ListConnectorResponse;
import org.meveo.api.rest.IBaseRs;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Connector rest service
 *
 * @author Clément Bareth
 */
@Path("/connector")
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public interface ConnectorRs extends IBaseRs {

    /**
     * Create a new connector.
     *
     * @return Request processing status
     */
    @Path("/")
    @POST
    ActionStatus create(ConnectorDto postData);

    /**
     * Update a connector.
     *
     * @return Request processing status
     */
    @Path("/")
    @PUT
    ActionStatus update(ConnectorDto postData);

    /**
     * Search for connector with a given name.
     * If version is not specified, return the last version of it.
     *
     * @return connector if exists
     */
    @Path("/{connectorName}")
    @GET
    GetConnectorResponse findByNameAndVersionOrLatest(@PathParam("connectorName") String connectorName, @QueryParam("version") String version);

    /**
     * Retrieve a list of all connectors.
     *
     * @param connectorName Name filter
     * @return list of all connectors
     */
    @Path("/")
    @GET
    ListConnectorResponse list(@QueryParam("connectorName") String connectorName);

    /**
     * Retrieve a list of all versions of the connector with the given name.
     *
     * @return list of all connectors
     */
    @Path("/{connectorName}/all")
    @GET
    ListConnectorResponse listByName(@PathParam("connectorName") String connectorName);

    /**
     * Remove a connector with a given name.
     * If version is provided, only delete the specified version.
     * If no version is provided, delete all the versions.
     *
     * @return Action result
     */
    @Path("/{connectorName}")
    @DELETE
    ActionStatus remove(@PathParam("connectorName") String connectorName, @QueryParam("version") String version);


    /**
     * Create new or update an existing connector.
     *
     * @param postData The connector's data
     * @return Request processing status
     */
    @Path("/createOrUpdate")
    @POST
    ActionStatus createOrUpdate(ConnectorDto postData);

}
