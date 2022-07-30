package org.meveo.api.rest.account;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.response.TitleDto;
import org.meveo.api.dto.response.account.TitleResponseDto;
import org.meveo.api.dto.response.account.TitlesResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.shared.Title;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Title}.
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/account/title")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("TitleRs")
public interface TitleRs extends IBaseRs {

    /**
     * Create a new title
     * 
     * @param postData The title's data
     * @return Request processing status
     */
    @POST
    @Path("/")
    @ApiOperation(value="Create a new title")
    ActionStatus create(@ApiParam("Title information") TitleDto postData);

    /**
     * Search for a title with a given code 
     * 
     * @param titleCode The title's code
     * @return A title's data
     */
    @GET
    @Path("/")
    @ApiOperation(value="Find title information")
    TitleResponseDto find(@QueryParam("titleCode") @ApiParam("Code of the title") String titleCode);

    /**
     * List titles 
     * 
     * @return A list of titles
     */
    @GET
    @Path("/list")
    @ApiOperation(value = "List titles")
    TitlesResponseDto list();

    /**
     * Update an existing title
     * 
     * @param postData The title's data
     * @return Request processing status
     */
    @PUT
    @Path("/")
    @ApiOperation(value="Update an existing title")
    ActionStatus update(@ApiParam("Title information") TitleDto postData);

    /**
     * Remove an existing title with a given code 
     * 
     * @param titleCode The title's code
     * @return Request processing status
     */
    @DELETE
    @Path("/{titleCode}")
    @ApiOperation(value="Remove an existing title")
    ActionStatus remove(@PathParam("titleCode") @ApiParam("Code of the title") String titleCode);

    /**
     * Create new or update an existing title
     * 
     * @param postData The title's data
     * @return Request processing status
     */
    @POST
    @Path("/createOrUpdate")
    @ApiOperation(value="Create new or update an existing title")
    ActionStatus createOrUpdate(@ApiParam("Title information") TitleDto postData);
}
