/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.api.rest.swagger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.meveo.util.Version;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.models.Info;
import io.swagger.models.Swagger;

@SwaggerDefinition(
        basePath = "/api/rest",
        tags = {
                @Tag(name = "Git", description = "Manage git repositories")
        }
)
public class SwaggerApiDefinition extends ApiListingResource {

    @Override
    protected Swagger process(Application app, ServletContext servletContext, ServletConfig sc, HttpHeaders headers, UriInfo uriInfo) {
        Info info = new Info();
        info.setTitle("Meveo");
        info.setVersion(Version.appVersion);

        Swagger swagger = super.process(app, servletContext, sc, headers, uriInfo);
        swagger.setInfo(info);

        return swagger;
    }


}
