package org.meveo.api.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
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
@Api("ProviderRs")
public interface ProviderRs extends IBaseRs {

    /**
     * Create provider.
     * 
     * @param postData Provider data to be created
     * @return action status
     */
    @POST
    @Path("/")
    @ApiOperation(value = "Create provider")
    ActionStatus create(@ApiParam("Provider information") ProviderDto postData);

    /**
     * Search for provider with a given code.
     * 
     * @param providerCode An optional Provider code. If not passed, a current user's provider will be retrieved
     * @return providers
     */
    @GET
    @Path("/")
    @ApiOperation(value = "Find provider by code")
    GetProviderResponse find(@QueryParam("providerCode") @ApiParam("Code of the provider") String providerCode);

    /**
     * Update provider.
     * 
     * @param postData Provider data
     * @return action status
     */
    @PUT
    @Path("/")
    @ApiOperation(value = "Update provider")
    ActionStatus update(@ApiParam("Provider information") ProviderDto postData);


    /**
     * Create or update a provider if it doesn't exists.
     * 
     * @param postData Provider data
     * @return action status
     */
    @POST
    @Path("/createOrUpdate")
    @ApiOperation(value = "Create or update provider")
    ActionStatus createOrUpdate(@ApiParam("Provider information") ProviderDto postData);

    /**
     * @param postData provider to be updated
     * @return action status
     */
    @PUT
    @Path("/updateProviderCF")
    @ApiOperation(value="Update provider cf")
    ActionStatus updateProviderCF(@ApiParam("Provider information") ProviderDto postData);

    /**
     * @param providerCode provider's code
     * @return provider if exists
     */
    @GET
    @Path("/findProviderCF")
    @ApiOperation(value="Find provider cf by code")
    GetProviderResponse findProviderCF(@QueryParam("providerCode") @ApiParam("Code of the provider") String providerCode);

    /**
     * Register a new tenant
     * 
     * @param postData Tenant/Provider data
     * @return Action status
     */
    @POST
    @Path("/createTenant")
    @ApiOperation(value = "Create tenant provider")
    ActionStatus createTenant(@ApiParam("Provider information") ProviderDto postData);

    /**
     * List tenants
     * 
     * @return A list of Tenant/provider data
     */
    @GET
    @Path("/listTenants")
    @ApiOperation(value = "List tenants")
    ProvidersDto listTenants();

    /**
     * Remove a tenant
     * 
     * @param providerCode Tenant/provider code
     * @return Action status
     */
    @DELETE
    @Path("/{providerCode}")
    @ApiOperation(value = "Remove tenant provider by code")
    ActionStatus removeTenant(@PathParam("providerCode") @ApiParam("Code of the provider") String providerCode);
}
