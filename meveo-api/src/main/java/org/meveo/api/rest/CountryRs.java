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
import org.meveo.api.dto.CountryDto;
import org.meveo.api.dto.response.GetCountryResponse;

/**
 * Web service for managing {@link org.meveo.model.billing.Country} and {@link org.meveo.model.billing.TradingCountry}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 *
 **/
@Path("/country")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("CountryRs")
public interface CountryRs extends IBaseRs {

    /**
     * Creates a tradingCountry base from the supplied country code. If the country code does not exists, a country and tradingCountry records are created
     * 
     * @param countryDto country
     * @return action status
     */
    @POST
    @Path("/")
    @ApiOperation(value = "Create country information")
    ActionStatus create(@ApiParam("Country information") CountryDto countryDto);

    /**
     * Search country with a given country code.
     * 
     * @param countryCode country code
     * @return {@link org.meveo.api.dto.response.GetCountryResponse}.
     */
    @GET
    @Path("/")
    @ApiOperation(value = "Find country information")
    GetCountryResponse find(@QueryParam("countryCode") @ApiParam("Code of the country") String countryCode);

    /**
     * Does not delete a country but the tradingCountry associated to it.
     * 
     * @param countryCode country code
     * @param currencyCode currency code
     * @return action status
     */
    @DELETE
    @Path("/{countryCode}/{currencyCode}")
    @ApiOperation(value = "Remove country information")
    ActionStatus remove(@PathParam("countryCode") @ApiParam("Code of the country") String countryCode, @PathParam("currencyCode") @ApiParam("Code of the currency") String currencyCode);

    /**
     * Modify a country. Same input parameter as create. The country and tradingCountry are created if they don't exists. The operation fails if the tradingCountry is null.
     * 
     * @param countryDto country 
     * @return action status
     */
    @PUT
    @Path("/")
    @ApiOperation(value = "Update country information")
    ActionStatus update(@ApiParam("Country information") CountryDto countryDto);

    /**
     * @param countryDto country
     * @return action status
     */
    @POST
    @Path("/createOrUpdate")
    @ApiOperation(value = "Create or update country information")
    ActionStatus createOrUpdate(@ApiParam("Country information") CountryDto countryDto);

}
