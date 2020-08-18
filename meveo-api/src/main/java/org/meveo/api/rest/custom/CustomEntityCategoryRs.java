package org.meveo.api.rest.custom;

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

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.dto.response.CustomEntityCategoriesResponseDto;
import org.meveo.api.dto.response.CustomEntityCategoryResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.customEntities.CustomEntityCategory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link CustomEntityCategory}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Path("/customEntityCategory")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CustomEntityCategoryRs")
public interface CustomEntityCategoryRs extends IBaseRs {

	/**
	 * Create a new {@linkplain CustomEntityCategory}
	 */
	@POST
	@Path("/entity")
	@ApiOperation(value = "Create a new custom entity category")
	ActionStatus create(@ApiParam("Custom entity category information") CustomEntityCategoryDto postData);

	/**
	 * Update an existing {@linkplain CustomEntityCategory}
	 * 
	 * @param postData The custom entity category data
	 * @return Request processing status
	 */
	@PUT
	@Path("/entity")
	@ApiOperation(value = "Update custom entity category information ")
	ActionStatus update(@ApiParam("Custom entity category information") CustomEntityCategoryDto postData);

	/**
	 * Define new or update existing custom entity category definition
	 *
	 * @param dto The custom entity category data
	 * @return
	 */
	@POST
	@Path("/entity/createOrUpdate")
	@ApiOperation(value = "Create or update custom entity category information ")
	ActionStatus createOrUpdateEntityCategory(@ApiParam("Custom entity category information") CustomEntityCategoryDto dto);

	/**
	 * Search for {@linkplain CustomEntityCategory} with a given code
	 */
	@GET
	@Path("/entity/{code}")
	@ApiOperation(value = "Find custom entity category information ")
	CustomEntityCategoryResponseDto find(@PathParam("code") @ApiParam("Code of the custom entity category") String code);

	/**
	 * List {@linkplain CustomEntityCategory}
	 * 
	 * @return A list of filtered custom entity category
	 */
	@POST
	@Path("/entity/list")
	@ApiOperation(value = "List custom entity category information ")
	CustomEntityCategoriesResponseDto list(@ApiParam("Paging and filtering information") PagingAndFiltering pagingAndFiltering);

	/**
	 * Remove custom entity category definition given its code
	 *
	 * @param customEntityCategoryCode
	 * @return
	 */
	@DELETE
	@Path("/entity/{customEntityCategoryCode}")
	@ApiOperation(value = "Remove custom entity category information by code ")
	ActionStatus removeEntityCategory(@PathParam("customEntityCategoryCode") @ApiParam("Code of the custom entity category") String customEntityCategoryCode,
			@DefaultValue("false") @QueryParam("deleteRelatedTemplates") @ApiParam("Whether to delete the related templates") boolean deleteRelatedTemplates);

}
