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

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.swagger.annotations.ApiParam;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.ScriptInstanceApi;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.api.function.FunctionApi;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.Sample;
import org.meveo.service.script.ConcreteFunctionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @author Clement Bareth
 * @lastModifiedVersion 6.5.2
 */
@Stateless
@Path("/function")
public class FunctionRs extends BaseRs {

	private static final Logger LOG = LoggerFactory.getLogger(FunctionRs.class);

	@Inject
	private FunctionApi functionApi;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<FunctionDto> getFunctions() {
		return functionApi.list();
	}

	@Path("/{code}/test")
	@PATCH
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
	public Map<String, Object> test(@PathParam("code") @ApiParam("Code of the function") String code, @ApiParam("Parameters to execute the function with") Map<String, Object> params) throws BusinessException {
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
	public List<Sample> getSamples(@PathParam("code") @ApiParam("Code of the function") String code) throws BusinessException {

		return functionApi.getSamples(code);
	}

}
