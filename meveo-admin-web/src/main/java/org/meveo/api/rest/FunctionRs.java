/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.api.rest;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.api.function.FunctionApi;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.Sample;
import org.meveo.service.script.ConcreteFunctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * API for managing {@link Function}.
 * 
 * @author Clement Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
//@Stateless
@Path("/function")
@Api("FunctionRs")
public class FunctionRs extends BaseRs {

	private static final Logger LOG = LoggerFactory.getLogger(FunctionRs.class);

	@Inject
	private FunctionApi functionApi;
	
	@Inject
	private ConcreteFunctionService functionService;
	
	/**
	 * @param code the code of the function
	 * @return the dto of the function
	 */
	@GET
	@Path("/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public FunctionDto getFunction(@PathParam("code") String code) throws BusinessException{
		return functionApi.find(code);
	}

	/**
	 * @param codeOnly Whether to retrieve only codes
	 * @return either all the functions or their codes
	 * @throws BusinessException 
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFunctions(@QueryParam("codeOnly") @ApiParam("Whether to retrieve only codes") boolean codeOnly) throws BusinessException {
		if(!codeOnly) {
			return Response.ok(functionApi.list(), MediaType.APPLICATION_JSON)
					.build();
		} else {
			return Response.ok(functionService.getCodes(), MediaType.APPLICATION_JSON)
					.build();
		}
	}

	@Path("/{code}/test")
	@PATCH
	@ApiOperation(value = "Update function ")
	public void updateTest(@PathParam("code") @ApiParam("Code of the function") String code, @ApiParam("Test suite file") File testSuite) throws IOException, BusinessException {

		functionApi.updateTest(code, testSuite);

	}

	/**
	 * Execute function and return result
	 *
	 * @param code   Code of the function
	 * @param params Parameters to execute the function with
	 * @return result of the fucnction execution
	 * @throws BusinessException
	 */
	@Path("/{code}/test")
	@POST
	@Produces("application/json; charset=UTF-8")
	@ApiOperation(value = "Test function")
	public Map<String, Object> test(@PathParam("code") @ApiParam("Code of the function") String code,
			@ApiParam("Parameters to execute the function with") Map<String, Object> params) throws BusinessException {
		if (params == null) {
			params = new HashMap<>();
		}

		params.put(FunctionApi.TEST_MODE, true);
		LOG.info("Starting test execution for function " + code);
		final Map<String, Object> execute = functionApi.execute(code, params);
		LOG.info("Ended test execution for function " + code);
		return execute;
	}

	/**
	 * Execute the associated test job for given function
	 *
	 * @param code Code of the function
	 */
	@Path("/{code}/job/start")
	@POST
	@ApiOperation(value = "Start function by code")
	public void startJob(@PathParam("code") @ApiParam("Code of the function") String code) throws BusinessException {
		functionApi.startJob(code);
	}

	/**
	 * Retrieves the samples for the {@linkplain Function} with the given code.
	 * 
	 * @param code code of the function
	 * @return the sample inputs of the function
	 */
	@GET
	@Path("/sample/code/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Get samples function by code")
	public List<Sample> getSamples(@PathParam("code") @ApiParam("Code of the function") String code) throws BusinessException {

		return functionApi.getSamples(code);
	}

}
