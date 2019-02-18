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
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.api.function.FunctionApi;
import org.meveo.api.rest.impl.BaseRs;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Stateless
@Path("/function")
public class FunctionRs extends BaseRs {

    @Inject
    private FunctionApi functionApi;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<FunctionDto> getFunctions() {
        return functionApi.list();
    }

    @Path("/{code}/test")
    @PATCH
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public void updateTest(@PathParam("code") String code, File testSuite) throws IOException, BusinessException {
        functionApi.updateTest(code, testSuite);
    }

    @Path("/{code}/test")
    @POST
    @Consumes("application/json; charset=UTF-8")
    @Produces("application/json; charset=UTF-8")
    public Map<String, Object> test(@PathParam("code") String code, Map<String, Object> params) throws BusinessException {
        if (params == null) {
            params = new HashMap<>();
        }

        params.put(FunctionApi.TEST_MODE, true);
        final Map<String, Object> execute = functionApi.execute(code, params);
        return execute;
    }

}
