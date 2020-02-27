package org.meveo.api.rest.custom;

import javax.transaction.NotSupportedException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.custom.CustomTableDataDto;
import org.meveo.api.dto.custom.CustomTableDataResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.rest.IBaseRs;
import org.meveo.api.rest.PATCH;

/**
 * Rest API for custom table for relation data management
 * 
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 **/
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("Custom table")
public interface ICustomTableRs <T extends CustomTableDataDto > extends IBaseRs {

    /**
     * Append data to a custom table
     *
     * @param dto Custom table data
     * @return Request processing status
     */
    @POST
    @Path("/")
    @ApiOperation(value = "Append data to a custom table")
    void append(@ApiParam("Custom table data") T dto) throws MeveoApiException, BusinessException;

    /**
     * Update existing data in a custom table
     * 
     * @param dto Custom table data. 'id' field is used to identify an existing record.
     * @return Request processing status
     */
    @PATCH
    @Path("/")
    @ApiOperation(value = "Update existing data in a custom table")
    void update(@ApiParam("Custom table data") T dto) throws MeveoApiException, BusinessException;

    /**
     * Remove an existing data from a custom table.
     * 
     * @param dto Custom table data. 'id' field is used to identify an existing record. If no 'id' values are passed, will delete all the records in a table.
     * @return Request processing status
     */
    @DELETE
    @Path("/")
    @ApiOperation(value = "Remove an existing data from a custom table")
    void remove(@ApiParam("Custom table data") T dto) throws MeveoApiException, BusinessException;

    /**
     * Search in custom tables
     * 
     * @param customTableCode Custom table code - can be either db table's name or a custom entity template code
     * @param pagingAndFiltering Paging and search criteria
     * @return Custom table data
     * @throws NotSupportedException
     */
    @POST
    @Path("/list/{customTableCode}")
    @ApiOperation(value = "List of custom table")
    CustomTableDataResponseDto list(@PathParam("customTableCode") @ApiParam("Code of the custom table") String customTableCode, @ApiParam("Paging and search criteria") PagingAndFiltering pagingAndFiltering) throws MeveoApiException, BusinessException, NotSupportedException;

    /**
     * Append or update data in a custom table
     * 
     * @param dto Custom table data. 'id' field is used to identify an existing record. Presence of 'id' field will be treated as update operation.
     * @return Request processing status
     */
    @PUT
    @Path("/")
    @ApiOperation(value = "Create or update custom table data")
    void createOrUpdate(@ApiParam("Custom table data") T dto) throws MeveoApiException, BusinessException;

}