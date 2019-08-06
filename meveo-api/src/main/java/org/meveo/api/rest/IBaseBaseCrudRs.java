package org.meveo.api.rest;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.meveo.api.dto.response.PagingAndFiltering;

/**
 * 
 * @author clement.bareth
 *
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
	void importData(MultipartFormDataInput input, @QueryParam("overwrite") boolean overwrite) throws IOException;

	/**
	 * Import a CSV file containing entities DTO
	 * 
	 * @param input     CSV file
	 * @param overwrite Whether existing entities should be overwritten
	 */
	@POST
	@Path("/import")
	@Consumes({ "text/csv", "application/vnd.ms-excel" })
	void importCSV(File csv, @QueryParam("overwrite") boolean overwrite);

	/**
	 * Import a XML file containing entities DTO
	 * 
	 * @param input     XML file
	 * @param overwrite Whether existing entities should be overwritten
	 */
	@POST
	@Path("/import")
	@Consumes({ "text/xml", "application/xml" })
	void importXML(File csv, @QueryParam("overwrite") boolean overwrite);

	/**
	 * Import a file containing entities DTO
	 * 
	 * @param input     Mutipart input
	 * @param overwrite Whether existing entities should be overwritten
	 */
	@POST
	@Path("/import")
	@Consumes("application/json")
	void importJSON(File csv, @QueryParam("overwrite") boolean overwrite);

	@POST
	@Path("/export")
	@Produces("text/csv")
	File exportCSV(PagingAndFiltering config);

	@POST
	@Path("/export")
	@Produces("application/json")
	File exportJSON(PagingAndFiltering config);

	@POST
	@Path("/export")
	@Produces("application/xml")
	File exportXML(PagingAndFiltering config);

}
