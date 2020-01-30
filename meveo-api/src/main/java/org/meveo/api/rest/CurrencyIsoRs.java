package org.meveo.api.rest;

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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.CurrencyIsoDto;
import org.meveo.api.dto.response.GetCurrenciesIsoResponse;
import org.meveo.api.dto.response.GetCurrencyIsoResponse;

/**
 * Web service for managing Currency.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 **/
@Path("/currencyIso")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CurrencyIsoRs")
public interface CurrencyIsoRs extends IBaseRs {

    /**
     * Creates tradingCurrency base on currency code. If the currency code does not exists, a currency record is created
     * 
     * @param currencyIsoDto currency iso
     * @return action status.
     */
    @POST
    @Path("/")
    @ApiOperation(value = "Create currency iso information")
    ActionStatus create(@ApiParam("Currency iso information") CurrencyIsoDto currencyIsoDto);

    /**
     * Search currency with a given currency code.
     * 
     * @param currencyCode currency code
     * @return currency iso if found.
     */
    @GET
    @Path("/")
    @ApiOperation(value = "Find currency iso information")
    GetCurrencyIsoResponse find(@QueryParam("currencyCode") @ApiParam("Code of the currency") String currencyCode);

    /**
     * Remove currency with a given currency code.
     * 
     * @param currencyCode currency code
     * @return action status.
     */
    @DELETE
    @Path("/{currencyCode}")
    @ApiOperation(value = "Remove currency iso information")
    ActionStatus remove(@PathParam("currencyCode") @ApiParam("Code of the currency") String currencyCode);

    /**
     * Modify a tradingCurrency. Same input parameter as create. The currency and tradingCurrency are created if they don't exists. The operation fails if the tradingCurrency is
     * null
     * 
     * @param currencyIsoDto currency iso
     * @return action status.
     */
    @PUT
    @Path("/")
    @ApiOperation(value = "Update currency iso information")
    ActionStatus update(@ApiParam("Currency iso information") CurrencyIsoDto currencyIsoDto);

    /**
     * @param currencyIsoDto currency iso to create or update
     * @return action status.
     */
    @POST
    @Path("/createOrUpdate")
    @ApiOperation(value = "Create or update currency iso information")
    ActionStatus createOrUpdate(@ApiParam("Currency iso information") CurrencyIsoDto currencyIsoDto);

    /**
     * List all currencies.
     * @return list of all currency iso/
     */
    @GET
    @Path("/")
    @ApiOperation(value = "List all currencies")
    GetCurrenciesIsoResponse list();
    
}
