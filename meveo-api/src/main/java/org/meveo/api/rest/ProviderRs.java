package org.meveo.api.rest;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ProviderDto;
import org.meveo.api.dto.ProvidersDto;
import org.meveo.api.dto.response.GetProviderResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Web service for managing Provider.
 * 
 * @author Edward P. Legaspi
 **/
@Path("/provider")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })

public interface ProviderRs extends IBaseRs {

    /**
     * Create provider.
     * 
     * @param postData Provider data to be created
     * @return action status
     */
    @POST
    @Path("/")
    ActionStatus create(ProviderDto postData);

    /**
     * Search for provider with a given code.
     * 
     * @param providerCode An optional Provider code. If not passed, a current user's provider will be retrieved
     * @return providers
     */
    @GET
    @Path("/")
    GetProviderResponse find(@QueryParam("providerCode") String providerCode);

    /**
     * Update provider.
     * 
     * @param postData Provider data
     * @return action status
     */
    @PUT
    @Path("/")
    ActionStatus update(ProviderDto postData);


    /**
     * Create or update a provider if it doesn't exists.
     * 
     * @param postData Provider data
     * @return action status
     */
    @POST
    @Path("/createOrUpdate")
    ActionStatus createOrUpdate(ProviderDto postData);

    /**
     * @param postData provider to be updated
     * @return action status
     */
    @PUT
    @Path("/updateProviderCF")
    ActionStatus updateProviderCF(ProviderDto postData);

    /**
     * @param providerCode provider's code
     * @return provider if exists
     */
    @GET
    @Path("/findProviderCF")
    GetProviderResponse findProviderCF(@QueryParam("providerCode") String providerCode);

    /**
     * Register a new tenant
     * 
     * @param postData Tenant/Provider data
     * @return Action status
     */
    @POST
    @Path("/createTenant")
    public ActionStatus createTenant(ProviderDto postData);

    /**
     * List tenants
     * 
     * @return A list of Tenant/provider data
     */
    @GET
    @Path("/listTenants")
    public ProvidersDto listTenants();

    /**
     * Remove a tenant
     * 
     * @param providerCode Tenant/provider code
     * @return Action status
     */
    @DELETE
    @Path("/{providerCode}")
    public ActionStatus removeTenant(@PathParam("providerCode") String providerCode);
}
