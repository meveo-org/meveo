package org.meveo.api.rest;

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

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.RoleDto;
import org.meveo.api.dto.RolesDto;
import org.meveo.api.dto.response.GetRoleResponse;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.PagingAndFiltering.SortOrder;
import org.meveo.model.security.Role;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Role}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/role")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("RoleRs")
public interface RoleRs extends IBaseRs {

	/**
	 * Create role.
	 * 
	 * @param postData posted data containing role dto.
	 * @return action status
	 */
	@POST
	@ApiOperation(value = "Create role")
	@Path("/")
	ActionStatus create(@ApiParam("Role information") RoleDto postData);

	/**
	 * Update role.
	 * 
	 * @param postData posted data
	 * @return action status.
	 */
	@PUT
	@ApiOperation(value = "Update role")
	@Path("/")
	ActionStatus update(@ApiParam("Role information") RoleDto postData);

	/**
	 * Remove role.
	 * 
	 * @param roleName Role name
	 * @return action status.
	 */
	@DELETE
	@Path("/{roleName}/{provider}")
	@ApiOperation(value = "Remove role by roleName")
	ActionStatus remove(@PathParam("roleName") @ApiParam("Name of the role") String roleName);

	/**
	 * Search role.
	 * 
	 * @param roleName Role name
	 * @return found role
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find role by roleName")
	GetRoleResponse find(@QueryParam("roleName") @ApiParam("Name of the role") String roleName);

	/**
	 * Create or update role.
	 * 
	 * @param postData posted data
	 * @return action status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update role")
	ActionStatus createOrUpdate(@ApiParam("Role information") RoleDto postData);

	/**
	 * List roles matching a given criteria.
	 * 
	 * @param query     Search criteria. Query is composed of the following:
	 *                  filterKey1:filterValue1|filterKey2:filterValue2
	 * @param fields    Data retrieval options/fieldnames separated by a comma.
	 *                  Specify "permissions" in fields to include the permissions.
	 *                  Specify "roles" to include child roles.
	 * @param offset    Pagination - from record number
	 * @param limit     Pagination - number of records to retrieve
	 * @param sortBy    Sorting - field to sort by - a field from a main entity
	 *                  being searched. See Data model for a list of fields.
	 * @param sortOrder Sorting - sort order.
	 * @return A list of roles
	 */
	@GET
	@Path("/list")
	@ApiOperation(value = "List get role")
	RolesDto listGet(@QueryParam("query") @ApiParam("Query to search criteria") String query, @QueryParam("fields") @ApiParam("Data retrieval options/fieldnames") String fields,
			@QueryParam("offset") @ApiParam("Offset from record number") Integer offset, @QueryParam("limit") @ApiParam("Number of records to retrieve") Integer limit,
			@DefaultValue("name") @QueryParam("sortBy") @ApiParam("Sort by a field") String sortBy,
			@DefaultValue("ASCENDING") @QueryParam("sortOrder") @ApiParam("Sort order") SortOrder sortOrder);

	/**
	 * List roles matching a given criteria.
	 * 
	 * @param pagingAndFiltering Pagination and filtering criteria. Specify
	 *                           "permissions" in fields to include the permissions.
	 *                           Specify "roles" to include child roles.
	 * @return A list of roles
	 */
	@POST
	@Path("/list")
	@ApiOperation(value = "List post role")
	RolesDto listPost(@ApiParam("Pagination and filtering criteria") PagingAndFiltering pagingAndFiltering);

	/**
	 * List external roles.
	 * 
	 * @return list of external roles
	 */
	@GET
	@Path("/external")
	@ApiOperation(value = "List external roles")
	RolesDto listExternalRoles();
	
	@DELETE
	@Path("/{role}/{permission}")
	@ApiOperation("Remove a permission from a role")
	void removePermissionFromRole(@PathParam("role") String role, @PathParam("permission") String permission) throws BusinessException;

}