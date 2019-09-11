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

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.dto.git.GitRepositoryDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.git.GitRepositoryApi;
import org.meveo.api.rest.impl.BaseCrudRs;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.git.GitRepository;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.util.List;

/**
 * REST interface to manage git repositories
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
@Path("/git")
public class GitRepositoryRs extends BaseCrudRs<GitRepository, GitRepositoryDto> {

    @Inject
    private GitRepositoryApi gitRepositoryApi;

    @Override
    public BaseCrudApi<GitRepository, GitRepositoryDto> getBaseCrudApi() {
        return gitRepositoryApi;
    }

    @POST
    @Path("/repositories")
    public void create(GitRepositoryDto postData) throws BusinessException {
        gitRepositoryApi.create(postData);
    }

    @PUT
    @Path("/repositories")
    public void createOrUpdate(GitRepositoryDto postData) throws MeveoApiException, BusinessException {
        gitRepositoryApi.createOrUpdate(postData);
    }

    @POST
    @Path("/repositories/{code}")
    public void update(GitRepositoryDto postData, String code) throws BusinessException {
        postData.setCode(code);
        gitRepositoryApi.update(postData);
    }

    @GET
    @Path("/repositories/{code}")
    public GitRepositoryDto find(String code) throws MeveoApiException, EntityDoesNotExistsException {
        return gitRepositoryApi.find(code);
    }

    @GET
    @Path("/repositories")
    public List<GitRepositoryDto> list() {
        return gitRepositoryApi.list();
    }

    @DELETE
    @Path("/repositories/{code}")
    public void remove(String code) throws BusinessException {
        gitRepositoryApi.remove(code);
    }
}
