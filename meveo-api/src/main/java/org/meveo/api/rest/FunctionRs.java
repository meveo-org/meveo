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

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.rest.impl.BaseRs;
import org.meveo.model.scripts.Function;
import org.meveo.service.script.ConcreteFunctionService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Stateless
@Path("/function/{code}")
public class FunctionRs extends BaseRs {

    @Inject
    private ConcreteFunctionService concreteFunctionService;

    @PathParam("code")
    private String code;

    @Path("/execute")
    @POST @Produces(MediaType.APPLICATION_JSON)
    private Map<String, Object> execute(Map<String, Object> inputs) throws BusinessException {
        return concreteFunctionService.getFunctionService(code).execute(code, inputs);
    }

    @Path("/test")
    public TestRs testRs(){
        return new TestRs();
    }

    public class TestRs {

        @POST @Produces(MediaType.APPLICATION_JSON)
        private Response executeTest() {
            return Response.ok().build();
        }

        @GET
        private String getTest(String file) {
            final Function function = concreteFunctionService.findByCode(code);
            return function.getTestSuite();
        }

        @PUT
        private void updateTest(String file) throws BusinessException {
            final Function function = concreteFunctionService.findByCode(code);
            function.setTestSuite(file);
            concreteFunctionService.update(function);
        }
    }


}
