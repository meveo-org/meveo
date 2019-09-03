package org.meveo.api.rest.custom;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
 **/
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface ICustomTableRs <T extends CustomTableDataDto > extends IBaseRs {

    /**
     * Append data to a custom table
     *
     * @param dto Custom table data
     * @return Request processing status
     */
    @POST
    @Path("/")
    void append(T dto) throws MeveoApiException, BusinessException;

    /**
     * Update existing data in a custom table
     * 
     * @param dto Custom table data. 'id' field is used to identify an existing record.
     * @return Request processing status
     */
    @PATCH
    @Path("/")
    void update(T dto) throws MeveoApiException, BusinessException;

    /**
     * Remove an existing data from a custom table.
     * 
     * @param dto Custom table data. 'id' field is used to identify an existing record. If no 'id' values are passed, will delete all the records in a table.
     * @return Request processing status
     */
    @DELETE
    @Path("/")
    void remove(T dto) throws MeveoApiException, BusinessException;

    /**
     * Search in custom tables
     * 
     * @param customTableCode Custom table code - can be either db table's name or a custom entity template code
     * @param pagingAndFiltering Paging and search criteria
     * @return Custom table data
     */
    @POST
    @Path("/list/{customTableCode}")
    CustomTableDataResponseDto list(@PathParam("customTableCode") String customTableCode, PagingAndFiltering pagingAndFiltering) throws MeveoApiException, BusinessException;

    /**
     * Append or update data in a custom table
     * 
     * @param dto Custom table data. 'id' field is used to identify an existing record. Presence of 'id' field will be treated as update operation.
     * @return Request processing status
     */
    @PUT
    @Path("/")
    void createOrUpdate(T dto) throws MeveoApiException, BusinessException;

}