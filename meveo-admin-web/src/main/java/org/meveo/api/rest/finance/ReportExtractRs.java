package org.meveo.api.rest.finance;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.finance.ReportExtractDto;
import org.meveo.api.dto.response.finance.ReportExtractResponseDto;
import org.meveo.api.dto.response.finance.ReportExtractsResponseDto;
import org.meveo.api.dto.response.finance.RunReportExtractDto;
import org.meveo.api.rest.IBaseRs;
import org.meveo.model.finance.ReportExtract;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link ReportExtract}.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 * @since 5.0
 **/
@Path("/finance/reportExtracts")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api("ReportExtractRs")
public interface ReportExtractRs extends IBaseRs {

	@POST
	@Path("/")
	@ApiOperation(value = "Create report extract")
	ActionStatus create(@ApiParam("Report extract information") ReportExtractDto postData);

	@POST
	@Path("/")
	@ApiOperation(value = "Update report extract")
	ActionStatus update(@ApiParam("Report extract information") ReportExtractDto postData);

	@POST
	@Path("/createOrUpdate")
	@ApiOperation(value = "Create or update report extract")
	ActionStatus createOrUpdate(@ApiParam("Report extract information") ReportExtractDto postData);

	@DELETE
	@Path("/")
	@ApiOperation(value = "Remove report extract by code")
	ActionStatus remove(@ApiParam("Code of the report extract") String reportExtractCode);

	@GET
	@Path("/list")
	@ApiOperation(value = "List of report extracts")
	ReportExtractsResponseDto list();

	@GET
	@Path("/")
	@ApiOperation(value = "Find report extract by code")
	ReportExtractResponseDto find(@QueryParam("reportExtractCode") @ApiParam("Code of the report extract") String reportExtractCode);

	@POST
	@Path("/run")
	@ApiOperation(value = "Run report extract")
	ActionStatus runReport(@ApiParam("Run report extract information") RunReportExtractDto postData);

}
