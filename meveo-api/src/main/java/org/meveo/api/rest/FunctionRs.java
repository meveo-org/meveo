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
import org.meveo.api.function.FunctionApi;
import org.meveo.api.rest.impl.BaseRs;
import org.xml.sax.SAXException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Map;

@Stateless
@Path("/function/{code}")
public class FunctionRs extends BaseRs {

    @Inject
    private FunctionApi functionApi;

    @PathParam("code")
    private String code;

    @Path("/execute")
    @POST @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> execute(Map<String, Object> inputs) throws BusinessException {
        return functionApi.execute(code, inputs);
    }

    @Path("/test")
    public TestRs testRs(){
        return new TestRs();
    }

    public class TestRs {

        @POST @Produces(MediaType.APPLICATION_JSON)
        public Response executeTest() throws Exception {
            return functionApi.executeTest(code);
        }

        @GET
        public String getTest() {
            return functionApi.getTest(code);
        }

        @PUT
        public void updateTest(String file) throws BusinessException {
            functionApi.updateTest(code, file);
        }
    }


}
