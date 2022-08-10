package org.meveo.api.rest;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.*;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.meveo.api.dto.response.PagingAndFiltering;

/**
 * @author clement.bareth
 */
public interface IBaseBaseCrudRs extends IBaseRs {

    /**
     * Import a file containing entities DTO
     *
     * @param input     Mutipart input
     * @param overwrite Whether existing entities should be overwritten
     */
    @POST
    @Path("/import")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(value = "Import from multiple files")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "File contained serialized data", allowMultiple = true, type = "file", required = true, dataType = "file", name = "file", paramType = "formData")
    })
    void importData(@NotNull @ApiParam(hidden = true) MultipartFormDataInput input,
                    @QueryParam("overwrite") @ApiParam("Whether to overwrite existing data") boolean overwrite) throws IOException;

    /**
     * Import a CSV file containing entities DTO
     *
     * @param csv       CSV file
     * @param overwrite Whether existing entities should be overwritten
     */
    @POST
    @Path("/import")
    @Consumes({"text/csv", "application/vnd.ms-excel"})
	@ApiOperation(value = "Import from a file", consumes = "text/csv, application/vnd.ms-excel, text/xml, application/xml, application/json")
	void importCSV(@ApiParam("File to import") File csv, @ApiParam("Whether to overwrite existing data") @QueryParam("overwrite") boolean overwrite);

    /**
     * Import a XML file containing entities DTO
     *
     * @param input     XML file
     * @param overwrite Whether existing entities should be overwritten
     */
    @POST
    @Path("/import")
    @Consumes({"text/xml", "application/xml"})
	@ApiOperation(value = "Import from a file", consumes = "text/csv, application/vnd.ms-excel, text/xml, application/xml, application/json")
	void importXML(@ApiParam("File to import") File xml,  @ApiParam("Whether to overwrite existing data") @QueryParam("overwrite") boolean overwrite);

    /**
     * Import a file containing entities DTO
     *
     * @param overwrite Whether existing entities should be overwritten
     */
    @POST
    @Path("/import")
    @Consumes("application/json")
	@ApiOperation(value = "Import from a file", consumes = "text/csv, application/vnd.ms-excel, text/xml, application/xml, application/json")
	void importJSON(@ApiParam("File to import") File json, @ApiParam("Whether to overwrite existing data") @QueryParam("overwrite") boolean overwrite);

    @POST
    @Path("/export")
    @Produces("text/csv")
	@ApiOperation(value = "Export to a file", produces = "text/csv, application/xml, application/json")
	File exportCSV(PagingAndFiltering config);

    @POST
    @Path("/export")
    @Produces("application/json")
	@ApiOperation(value = "Export to a file", produces = "text/csv, application/xml, application/json")
	File exportJSON(PagingAndFiltering config);

    @POST
    @Path("/export")
    @Produces("application/xml")
	@ApiOperation(value = "Export to a file",produces = "text/csv, application/xml, application/json")
	File exportXML(PagingAndFiltering config);

}
