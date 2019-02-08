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
import org.meveo.service.script.ConcreteFunctionService;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Map;

@Path("/function/{code}")
public class FunctionRs extends BaseRs {

    @Inject
    private ConcreteFunctionService concreteFunctionService;

    @PathParam("code")
    private String code;

    @Path("/execute") @POST
    private Map<String, Object> execute(Map<String, Object> inputs) throws BusinessException {
        return concreteFunctionService.getFunctionService(code).execute(code, inputs);
    }
}
