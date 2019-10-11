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

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Path("/customEntityCategory")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface CustomEntityCategoryRs extends IBaseRs {

	/**
	 * Create a new {@linkplain CustomEntityCategory}
	 */
	@POST
	@Path("/entity")
	ActionStatus create(CustomEntityCategoryDto postData);

	/**
	 * Update an existing {@linkplain CustomEntityCategory}
	 * 
	 * @param postData The custom entity category data
	 * @return Request processing status
	 */
	@PUT
	@Path("/entity")
	ActionStatus update(CustomEntityCategoryDto postData);
	
    /**
     * Define new or update existing custom entity category definition
     *
     * @param dto The custom entity category data
     * @return
     */
    @POST
    @Path("/entity/createOrUpdate")
    public ActionStatus createOrUpdateEntityCategory(CustomEntityCategoryDto dto);

    /**
	 * Search for {@linkplain CustomEntityCategory} with a given code
	 */
	@GET
	@Path("/entity/{code}")
	CustomEntityCategoryResponseDto find(@PathParam("code") String code);

	/**
	 * List {@linkplain CustomEntityCategory}
	 * 
	 * @return A list of filtered custom entity category
	 */
	@POST
	@Path("/entity/list")
	CustomEntityCategoriesResponseDto list(PagingAndFiltering pagingAndFiltering);
    
	/**
     * Remove custom entity category definition given its code
     *
     * @param customEntityCategoryCode
     * @return
     */
    @DELETE
    @Path("/entity/{customEntityCategoryCode}")
    public ActionStatus removeEntityCategory(@PathParam("customEntityCategoryCode") String customEntityCategoryCode, @DefaultValue("false") @QueryParam("deleteRelatedTemplates") boolean deleteRelatedTemplates);

}

