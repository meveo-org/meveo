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

package org.meveo.api.rest.git;

import io.swagger.annotations.ApiModelProperty;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.meveo.api.dto.git.GitRepositoryDto;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;
import java.io.InputStream;

public class GitRepositoryUploadForm {

    @FormParam("zipFile")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    @ApiModelProperty("Zipped repository content")
    private InputStream data;

    @FormParam("repository")
    @PartType(MediaType.APPLICATION_JSON)
    @ApiModelProperty("Repository information")
    private GitRepositoryDto repository;

    public InputStream getData() {
        return data;
    }

    public void setData(InputStream data) {
        this.data = data;
    }

    public GitRepositoryDto getRepository() {
        return repository;
    }

    public void setRepository(GitRepositoryDto repository) {
        this.repository = repository;
    }
}
