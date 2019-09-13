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
import org.meveo.api.logging.WsRestApiInterceptor;
import org.meveo.api.rest.impl.BaseCrudRs;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.git.GitRepository;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitRepositoryService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * REST interface to manage git repositories
 * @author Clement Bareth
 * @lastModifiedVersion 6.4.0
 */
@RequestScoped
@Path("/git")
@Interceptors({ WsRestApiInterceptor.class })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.MULTIPART_FORM_DATA,  "text/csv"})
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "text/csv" })
public class GitRepositoryRs extends BaseCrudRs<GitRepository, GitRepositoryDto> {

    @Inject
    private GitRepositoryApi gitRepositoryApi;

    @Inject
    private GitRepositoryService gitRepositoryService;

    @Inject
    private GitClient gitClient;

    @Override
    public BaseCrudApi<GitRepository, GitRepositoryDto> getBaseCrudApi() {
        return gitRepositoryApi;
    }

    @GET
    @Path("/repositories")
    public List<GitRepositoryDto> list() {
        return gitRepositoryApi.list();
    }

    @POST
    @Path("/repositories")
    public void create(GitRepositoryDto postData) throws BusinessException {
        gitRepositoryApi.create(postData);
    }

    @PUT
    @Path("/repositories")
    public void overwrite(GitRepositoryDto postData) throws BusinessException {
        final boolean exists = gitRepositoryApi.exists(postData);
        if(exists){
            gitRepositoryApi.remove(postData.getCode());
        }

        gitRepositoryApi.create(postData);
    }

    @POST
    @Path("/repositories/{code}")
    public void update(GitRepositoryDto postData, @PathParam("code") String code) throws BusinessException {
        postData.setCode(code);
        gitRepositoryApi.update(postData);
    }

    @GET
    @Path("/repositories/{code}")
    public GitRepositoryDto find(@PathParam("code") String code) throws MeveoApiException, EntityDoesNotExistsException {
        return gitRepositoryApi.find(code);
    }

    @DELETE
    @Path("/repositories/{code}")
    public void remove(@PathParam("code") String code) throws BusinessException {
        gitRepositoryApi.remove(code);
    }

    @POST
    @Path("/repositories/{code}/commit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void commit(@PathParam("code") String code, @FormParam("message") String message, @FormParam("pattern") List<String> files) throws BusinessException {
        final GitRepository repository = gitRepositoryService.findByCode(code);
        gitClient.commit(repository, files, message);
    }

    @POST
    @Path("/repositories/{code}/push")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void push(@PathParam("code") String code, @FormParam("username") String username, @FormParam("password") String password) throws BusinessException {
        final GitRepository repository = gitRepositoryService.findByCode(code);
        gitClient.push(repository, username, password);
    }

    @POST
    @Path("/repositories/{code}/pull")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void pull(@PathParam("code") String code, @FormParam("username") String username, @FormParam("password") String password) throws BusinessException {
        final GitRepository repository = gitRepositoryService.findByCode(code);
        gitClient.pull(repository, username, password);
    }

    @POST
    @Path("/repositories/{code}/checkout/{branch}")
    public void checkout(@PathParam("code") String code, @PathParam("branch") String branch, @QueryParam("create") boolean createBranch) throws BusinessException {
        final GitRepository repository = gitRepositoryService.findByCode(code);
        gitClient.checkout(repository, branch, createBranch);
    }

    @POST
    @Path("/repositories/{code}/create/{branch}")
    public void create(@PathParam("code") String code, @PathParam("branch") String branch) throws BusinessException {
        final GitRepository repository = gitRepositoryService.findByCode(code);
        gitClient.createBranch(repository, branch);
    }

}
