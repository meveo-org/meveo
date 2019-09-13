package org.meveo.api.rest.custom;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.rest.IBaseRs;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/customEntityCategory")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface CustomEntityCategoryRs extends IBaseRs {

    /**
     * Define new or update existing custom entity category definition
     *
     * @param dto
     * @return
     */
    @POST
    @Path("/entity/createOrUpdate")
    public ActionStatus createOrUpdateEntityCategory(CustomEntityCategoryDto dto);

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

