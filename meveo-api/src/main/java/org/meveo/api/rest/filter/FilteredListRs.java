package org.meveo.api.rest.filter;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.elasticsearch.search.sort.SortOrder;
import org.meveo.api.dto.FilterDto;
import org.meveo.api.rest.IBaseRs;

/**
 * Provides APIs for conducting Full Text Search.
 *
 * @author Edward P. Legaspi
 * @author Andrius Karpavicius
 * @author Tony Alejandro
 * @version 6.7.0
 **/
@Path("/filteredList")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("FilteredListRs")
public interface FilteredListRs extends IBaseRs {

    /**
     * Execute a filter to retrieve a list of entities
     *
     * @param filter - if the code is set we lookup the filter in DB, else we parse the inputXml to create a transient filter
     * @param from Pagination - starting record
     * @param size Pagination - number of records per page
     * @return Response
     */
    @Path("/listByFilter")
    @POST
    @ApiOperation(value = "List filter")
    Response listByFilter(@ApiParam("Filter information") FilterDto filter, @QueryParam("from") @ApiParam("Starting record") Integer from, @QueryParam("size") @ApiParam("Number of records per page") Integer size);

    /**
     * Execute a search in Elastic Search on all fields (_all field)
     *
     * @param classnamesOrCetCodes Entity classes to match - full class name
     * @param query Query - words (will be joined by AND) or query expression (+word1 - word2)
     * @param from Pagination - starting record
     * @param size Pagination - number of records per page
     * @param sortField Pagination - field used to sort the results
     * @param sortOrder Pagination - ASC or DESC order of the results
     * @return Response object that contains JSON results in String format
     */
    @Path("/search")
    @GET
    @ApiOperation(value = "Search filter by classnames or cet code")
    Response search(@QueryParam("classnamesOrCetCodes") @ApiParam("Entity classes to match") String[] classnamesOrCetCodes, @QueryParam("query") @ApiParam("Query") String query, @QueryParam("from") @ApiParam("Starting record") Integer from,
                           @QueryParam("size") @ApiParam("Number of records per page") Integer size, @QueryParam("sortField") @ApiParam("Sort the results of field") String sortField, @QueryParam("sortOrder") @ApiParam("Sort ASC or DESC order of the results") SortOrder sortOrder);

    /**
     * Execute a search in Elastic Search on given fields for given values. Query values by field are passed in extra query parameters in a form of fieldName=valueToMatch
     *
     * @param classnamesOrCetCodes Entity classes to match - full class name
     * @param from Pagination - starting record
     * @param size Pagination - number of records per page
     * @param sortField Pagination - field used to sort the results
     * @param sortOrder Pagination - ASC or DESC order of the results
     * @param info provides request URI information
     * @return Response object that contains JSON results in String format
     */
    @Path("/searchByField")
    @GET
    @ApiOperation(value = "Search filter by field")
    Response searchByField(@QueryParam("classnamesOrCetCodes") @ApiParam("Entity classes to match") String[] classnamesOrCetCodes, @QueryParam("from") @ApiParam("Starting record") Integer from, @QueryParam("size") @ApiParam("Number of records per page") Integer size,
            @QueryParam("sortField") @ApiParam("Sort the results of field") String sortField, @QueryParam("sortOrder") @ApiParam("Sort ASC or DESC order of the results") SortOrder sortOrder, @Context @ApiParam("Request URI information") UriInfo info);

    /**
     * Clean and reindex Elastic Search repository
     *
     * @return Request processing status
     */
    @Path("/reindex")
    @GET
    @ApiOperation(value = "Clean and reindex Elastic Search repository")
    Response reindex();

    /**
     * Execute a search in Elastic Search on all fields (_all field) and all entity types
     *
     * Deprecated in v. 6.2. Use /search instead
     *
     * @param query Query - words (will be joined by AND) or query expression (+word1 - word2)
     * @param category search by category that is directly taken from the name of the entity found in entityMapping. property of elasticSearchConfiguration.json. e.g. Customer,
     *        CustomerAccount, AccountOperation, etc. See elasticSearchConfiguration.json entityMapping keys for a list of categories.
     * @param from Pagination - starting record
     * @param size Pagination - number of records per page
     * @param sortField Pagination - field used to sort the results
     * @param sortOrder Pagination - ASC or DESC order of the results
     * @return Response object that contains JSON results in String format
     */
    @Path("/fullSearch")
    @GET
    @Deprecated
    @ApiOperation("Search filter")
    Response fullSearch(@QueryParam("query") @ApiParam("Query") String query, @QueryParam("category") @ApiParam("Search by category") String category, @QueryParam("from") @ApiParam("Starting record") Integer from, @QueryParam("size") @ApiParam("Number of records per page") Integer size,
            @QueryParam("sortField") @ApiParam("Sort the results of field") String sortField, @QueryParam("sortOrder") @ApiParam("Sort ASC or DESC order of the results") SortOrder sortOrder);
}