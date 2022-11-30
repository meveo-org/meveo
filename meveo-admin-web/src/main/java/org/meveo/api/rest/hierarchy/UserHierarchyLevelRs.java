package org.meveo.api.rest.hierarchy;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.hierarchy.UserHierarchyLevelDto;
import org.meveo.api.dto.hierarchy.UserHierarchyLevelsDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.PagingAndFiltering.SortOrder;
import org.meveo.api.dto.response.UserHierarchyLevelResponseDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.hierarchy.UserHierarchyLevel;

/**
 * API for managing {@link UserHierarchyLevel}
 * @author Phu Bach
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/hierarchy/userGroupLevel")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("UserHierarchyLevelRs")
public interface UserHierarchyLevelRs extends IBaseRs {

    /**
     * Create a new user hierarchy level
     * 
     * @param postData The user hierarchy level's data
     * @return Request processing status
     */
    @POST
    @Path("/")
    @ApiOperation(value="Create user hierarchy level")
    ActionStatus create(@ApiParam("User hierarchy level information") UserHierarchyLevelDto postData);

    /**
     * Update an existing user hierarchy level
     * 
     * @param postData The user hierarchy level's data
     * @return Request processing status
     */
    @PUT
    @Path("/")
    @ApiOperation(value="Update user hierarchy level")
    ActionStatus update(@ApiParam("User hierarchy level information") UserHierarchyLevelDto postData);

    /**
     * Search for a user group level with a given code.
     * 
     * @param hierarchyLevelCode the code to string
     * @return the UserHierarchyLevel given the hierarchyCode
     */
    @GET
    @Path("/")
    @ApiOperation(value="Find user hierarchy level by code")
    UserHierarchyLevelResponseDto find(@QueryParam("hierarchyLevelCode") @ApiParam("Code of the user hierarchy level") String hierarchyLevelCode);

    /**
     * Remove an existing hierarchy level with a given code
     * 
     * @param hierarchyLevelCode The hierarchy level's code
     * @return Request processing status
     */
    @DELETE
    @Path("/{hierarchyLevelCode}")
    @ApiOperation(value="Remove user hierarchy level by code")
    ActionStatus remove(@PathParam("hierarchyLevelCode") @ApiParam("Code of the user hierarchy level") String hierarchyLevelCode);

    /**
     * Create new or update an existing user hierarchy level with a given code
     * 
     * @param postData The user hierarchy level's data
     * @return Request processing status
     */
    @POST
    @Path("/createOrUpdate")
    @ApiOperation(value="Create or update user hierarchy level")
    ActionStatus createOrUpdate(@ApiParam("User hierarchy level information") UserHierarchyLevelDto postData);

    /**
     * List user hierarchy levels matching a given criteria
     * 
     * @param query Search criteria. Query is composed of the following: filterKey1:filterValue1|filterKey2:filterValue2
     * @param fields Data retrieval options/fieldnames separated by a comma. Specify "childLevels" in fields to include the child levels of user hierarchy level.
     * @param offset Pagination - from record number
     * @param limit Pagination - number of records to retrieve
     * @param sortBy Sorting - field to sort by - a field from a main entity being searched. See Data model for a list of fields.
     * @param sortOrder Sorting - sort order.
     * @return A list of user hierarchy levels
     */
    @GET
    @Path("/list")
    @ApiOperation(value="List get user hierarchy level")
    UserHierarchyLevelsDto listGet(@QueryParam("query") @ApiParam("Query to search criteria") String query, @QueryParam("fields") @ApiParam("Data retrieval options/fieldnames") String fields, @QueryParam("offset") @ApiParam("Offset from record number") Integer offset,
            @QueryParam("limit") @ApiParam("Number of records to retrieve") Integer limit, @DefaultValue("code") @QueryParam("sortBy") @ApiParam("Sort by a field") String sortBy, @DefaultValue("ASCENDING") @QueryParam("sortOrder") @ApiParam("Sort order") SortOrder sortOrder);

    /**
     * List user hierarchy levels matching a given criteria
     * 
     * @param pagingAndFiltering Pagination and filtering criteria. Specify "childLevels" in fields to include the child levels of user hierarchy level.
     * @return A list of user hierarchy levels
     */
    @POST
    @Path("/list")
    @ApiOperation(value="List post user hierarchy level")
    UserHierarchyLevelsDto listPost(@ApiParam("Pagination and filtering criteria") PagingAndFiltering pagingAndFiltering);

}