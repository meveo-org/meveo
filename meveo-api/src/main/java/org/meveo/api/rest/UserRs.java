package org.meveo.api.rest;

import java.util.List;
import java.util.Set;

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
import org.meveo.api.dto.UserDto;
import org.meveo.api.dto.UsersDto;
import org.meveo.api.dto.response.GetUserResponse;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.dto.response.PagingAndFiltering.SortOrder;
import org.meveo.service.git.RSAKeyPair;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Web service for managing {@link org.meveo.model.admin.User}. User has a
 * unique username that is use for update, search and remove operation.
 * 
 * @author Mohamed Hamidi
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/user")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("UserRs")
public interface UserRs extends IBaseRs {
	
	@GET
	@Path("/current/roles")
	public Set<String> getRoles();

	/**
	 * Create user.
	 * 
	 * @param postData user to be created
	 * @return action status
	 */
	@POST
	@Path("/")
	@ApiOperation(value = "Create user")
	ActionStatus create(@ApiParam("User information") UserDto postData);

	/**
	 * Update user.
	 * 
	 * @param postData user to be updated
	 * @return action status
	 */
	@PUT
	@Path("/")
	@ApiOperation(value = "Update user")
	ActionStatus update(@ApiParam("User information") UserDto postData);

	/**
	 * Remove user with a given username.
	 * 
	 * @param username user name
	 * @return action status
	 */
	@DELETE
	@Path("/{username}")
	@ApiOperation(value = "Remove user by username")
	ActionStatus remove(@PathParam("username") @ApiParam("The name of user") String username);

	/**
	 * Search user with a given username.
	 * 
	 * @param username user name
	 * @return user
	 */
	@GET
	@Path("/")
	@ApiOperation(value = "Find user by username")
	GetUserResponse find(@QueryParam("username") @ApiParam("The name of user") String username);

	/**
	 * Create or update user based on the username.
	 * 
	 * @param postData user to be created or updated
	 * @return action status
	 */
	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update user")
	ActionStatus createOrUpdate(@ApiParam("User information") UserDto postData);

	/**
	 * Creates a user in keycloak and core.
	 * 
	 * @param postData user to be created externally
	 * @return action status
	 */
	@POST
	@Path("/external")
	@ApiOperation(value = "Create external user")
	ActionStatus createExternalUser(@ApiParam("User information") UserDto postData);

	/**
	 * Updates a user in keycloak and core given a username.
	 * 
	 * @param postData user to be updated
	 * @return action status
	 */
	@PUT
	@Path("/external/")
	@ApiOperation(value = "Update external user")
	ActionStatus updateExternalUser(@ApiParam("User information") UserDto postData);

	/**
	 * Deletes a user in keycloak and core given a username.
	 * 
	 * @param username the username of the user to be deleted.
	 * @return action status
	 */
	@DELETE
	@Path("/external/{username}")
	@ApiOperation(value = "Remove external user by username")
	ActionStatus deleteExternalUser(@PathParam("username") @ApiParam("The name of user") String username);

	/**
	 * List users matching a given criteria.
	 * 
	 * @param query     Search criteria. Query is composed of the following:
	 *                  filterKey1:filterValue1|filterKey2:filterValue2
	 * @param fields    Data retrieval options/fieldnames separated by a comma.
	 *                  Specify "securedEntities" in fields to include the secured
	 *                  entities.
	 * @param offset    Pagination - from record number
	 * @param limit     Pagination - number of records to retrieve
	 * @param sortBy    Sorting - field to sort by - a field from a main entity
	 *                  being searched. See Data model for a list of fields.
	 * @param sortOrder Sorting - sort order.
	 * @return A list of users
	 */
	@GET
	@Path("/list")
	@ApiOperation(value = "List get user")
	UsersDto listGet(@QueryParam("query") @ApiParam("Query to search criteria") String query, @QueryParam("fields") @ApiParam("Data retrieval options/fieldnames") String fields,
			@QueryParam("offset") @ApiParam("Offset from record number") Integer offset, @QueryParam("limit") @ApiParam("Number of records to retrieve") Integer limit,
			@DefaultValue("userName") @QueryParam("sortBy") @ApiParam("Sort by a field") String sortBy,
			@DefaultValue("ASCENDING") @QueryParam("sortOrder") @ApiParam("Sort order") SortOrder sortOrder);

	/**
	 * List users matching a given criteria.
	 * 
	 * @param pagingAndFiltering Pagination and filtering criteria. Specify
	 *                           "securedEntities" in fields to include the secured
	 *                           entities.
	 * @return A list of users
	 */
	@POST
	@Path("/list")
	@ApiOperation(value = "List post user")
	UsersDto listPost(@ApiParam("Pagination and filtering criteria") PagingAndFiltering pagingAndFiltering);

	/**
	 * Generate and set ssh keys for a user.
	 *
	 * @param username If provided, will set ssh keys for corresponding user.
	 *                 Instead, will set ssh keys for logged user
	 * @return the generated {@link RSAKeyPair}
	 */
	@POST
	@Path("/ssh/generate")
	@Consumes(MediaType.TEXT_PLAIN)
	@ApiOperation(value = "Generate SH key by username")
	RSAKeyPair generateShKey(@QueryParam("username") @ApiParam("Username to connect") String username, @ApiParam("Pass phrase") String passphrase) throws BusinessException;

}