/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.api.rest.technicalservice.impl;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.technicalservice.endpoint.EndpointDto;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.model.technicalservice.endpoint.Endpoint;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Rest endpoint for managing service endpoints
 *
 * @author clement.bareth
 * @since 04.02.2019
 */
@Path("/endpoint")
@DeclareRoles({"endpointManagement"})
@RolesAllowed({"endpointManagement"})
public class EndpointRs extends BaseRs {

    @EJB
    private EndpointApi endpointApi;

    @POST
    public Response create(@Valid @NotNull EndpointDto endpointDto) throws BusinessException {
        try {
            final Endpoint endpoint = endpointApi.create(endpointDto);
            return Response.status(201).entity(endpoint.getId()).build();
        } catch (NullPointerException e) {
            throw new NotFoundException("Function " + endpointDto.getServiceCode() + "does not exists.");
        }
    }

    @PUT
    public Response createOrReplace(@Valid @NotNull EndpointDto endpointDto) throws BusinessException {
        final Endpoint endpoint = endpointApi.createOrReplace(endpointDto);
        if(endpoint != null){
            return Response.status(201).entity(endpoint.getId()).build();
        }else{
            return Response.noContent().build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@QueryParam("service") String serviceCode){
        List<EndpointDto> dtoList;
        if(serviceCode != null){
            dtoList = endpointApi.findByServiceCode(serviceCode);
        }else{
            dtoList = endpointApi.list();
        }
        return Response.ok(dtoList).build();
    }

    @DELETE @Path("/{code}")
    public Response delete(@PathParam("code") @NotNull String code) throws BusinessException {
        endpointApi.delete(code);
        return Response.noContent().build();
    }

    @GET @Path("/{code}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response find(@PathParam("code") @NotNull String code){
        final EndpointDto endpointDto = endpointApi.findByCode(code);
        if(endpointDto != null){
            return Response.ok(endpointDto).build();
        }
        return Response.status(404).build();
    }

    @HEAD @Path("/{code}")
    public Response exists(@PathParam("code") @NotNull String code){
        final EndpointDto endpointDto = endpointApi.findByCode(code);
        if(endpointDto != null){
            return Response.noContent().build();
        }
        return Response.status(404).build();
    }

}
