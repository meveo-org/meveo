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
package org.meveo.service.technicalservice.endpoint;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.revwalk.DepthWalk;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.scripts.Function;
import org.meveo.model.security.DefaultRole;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;

/**
 * EJB for managing technical services endpoints
 *
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 01.02.2019
 * @version 6.9.0
 */
@Stateless
public class EndpointService extends BusinessService<Endpoint> {

	public static final String EXECUTE_ENDPOINT_TEMPLATE = "Execute_Endpoint_%s";

	@Inject
	private GitClient gitClient;

	@Inject
	private PermissionService permissionService;

	@Inject
	@MeveoRepository
	private GitRepository meveoRepository;

	@Context
	private HttpServletRequest request;

	@Inject
	private CommitMessageBean commitMessageBean;

	public static String getEndpointPermission(Endpoint endpoint) {
		return String.format(EXECUTE_ENDPOINT_TEMPLATE, endpoint.getCode());
	}

	/**
	 * Retrieve all endpoints associated to the given service
	 *
	 * @param code Code of the service
	 * @return All endpoints associated to the service with the provided code
	 */
	public List<Endpoint> findByServiceCode(String code) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<Endpoint> query = cb.createQuery(Endpoint.class);
		Root<Endpoint> root = query.from(Endpoint.class);
		final Join<Endpoint, Function> service = root.join("service");
		query.where(cb.equal(service.get("code"), code));
		return getEntityManager().createQuery(query).getResultList();
	}

	/**
	 * @param code          Code of the service associated to the endpoint
	 * @param parameterName Filter on endpoint's parameters names
	 * @return the filtered list of endpoints
	 */
	public List<Endpoint> findByParameterName(String code, String parameterName) {
		return getEntityManager().createNamedQuery("findByParameterName", Endpoint.class).setParameter("serviceCode", code).setParameter("propertyName", parameterName)
				.getResultList();
	}

	/**
	 * Create a new endpoint in database. Also create associated client and roles in
	 * keycloak.
	 *
	 * @param entity Endpoint to create
	 */
	@Override
	public void create(Endpoint entity) throws BusinessException {
		validatePath(entity);
		super.create(entity);

		if (entity.isSecured()) {
			permissionService.createIfAbsent(getEndpointPermission(entity), DefaultRole.EXECUTE_ALL_ENDPOINTS.getRoleName());
		}
	}

	@Override
	public Endpoint update(Endpoint entity) throws BusinessException {
		validatePath(entity);
		Endpoint endpoint = super.update(entity);

		if (entity.isSecured()) {
			permissionService.createIfAbsent(getEndpointPermission(entity), DefaultRole.EXECUTE_ALL_ENDPOINTS.getRoleName());
		} else {
			permissionService.removeIfPresent(getEndpointPermission(entity));
		}

		return endpoint;
	}

	/**
	 * Remove an endpoint from database. Also remove associated role in keycloak.
	 *
	 * @param entity Endpoint to remove
	 */
	@Override
	public void remove( Endpoint entity) throws BusinessException {
		super.remove(entity);
		permissionService.removeIfPresent(getEndpointPermission(entity));
	}

	/**
	 * Checks if an endpoint interface is already created and pushed in git
	 * repository.
	 *
	 * @param endpoint endpoint to search
	 * @return true if endpoint interface exists
	 */
	public boolean isEndpointScriptExists(Endpoint endpoint) {
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File endpointDir = new File(repositoryDir, "/facets/javascript/endpoints/");
		File f = new File(endpointDir, endpoint.getCode() + ".js");

		return f.exists() && !f.isDirectory();
	}

	/**
	 * Checks if the base endpoint interface is already created and pushed in git
	 * repository.
	 *
	 * @param endpoint endpoint to search
	 * @return true if endpoint interface exists
	 */
	public boolean isBaseEndpointScriptExists() {
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File f = new File(repositoryDir, "/endpoints/" + Endpoint.ENDPOINT_INTERFACE_JS + ".js");

		return f.exists() && !f.isDirectory();
	}

	public File getScriptFile(Endpoint endpoint) {
		File repositoryDir;
		MeveoModule module = this.findModuleOf(endpoint);
		if (module == null) {
			repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		} else {
			repositoryDir = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
		}
		final File endpointDir = new File(repositoryDir, "/facets/javascript/endpoints/");
		endpointDir.mkdirs();
		return new File(endpointDir, endpoint.getCode() + ".js");
	}

	public File getBaseScriptFile() {
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File endpointFile = new File(repositoryDir, "/facets/javascript/endpoints/" + Endpoint.ENDPOINT_INTERFACE_JS + ".js");
		return endpointFile;
	}

	/**
	 * see java-doc {@link BusinessService#addFilesToModule(org.meveo.model.BusinessEntity, MeveoModule)}
	 */
	@Override
	public void addFilesToModule(Endpoint entity, MeveoModule module) throws BusinessException {
		super.addFilesToModule(entity, module);

		File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
		String path = "facets/javascript/endpoints/"+entity.getCode()+".js";

		File newJsFile = new File (gitDirectory, path);

		try {
			MeveoFileUtils.writeAndPreserveCharset(ESGenerator.generateFile(entity), newJsFile);
		} catch (IOException e) {
			throw new BusinessException("File cannot be write", e);
		}
		String message = "Add JS script for Endpoint: " + entity.getCode();
		try {
			message+=" "+commitMessageBean.getCommitMessage();
		} catch (ContextNotActiveException e) {
			log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
		}
		gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(newJsFile), message);
	}

	@Override
	public void removeFilesFromModule(Endpoint entity, MeveoModule module) throws BusinessException {
		super.removeFilesFromModule(entity, module);
		File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
		String path = "facets/javascript/endpoints/"+entity.getCode()+".js";
		File jsFile = new File (gitDirectory, path);
		jsFile.delete();
		String message = "Remove JS script for Endpoint: " + entity.getCode();
		try {
			message+=" "+commitMessageBean.getCommitMessage();
		} catch (ContextNotActiveException e) {
			log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
		}
		gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(jsFile), message);
	}

	private void validatePath(Endpoint entity) throws BusinessException {
		/* check that the path is valid */
		if (entity.getPath() != null) {
			Matcher matcher = Endpoint.pathParamPattern.matcher(entity.getPath());
			int i = 0;
			while (matcher.find()) {
				String param = matcher.group();
				String paramName = param.substring(1, param.length() - 1);
				if (entity.getPathParameters().size() > i) {
					String actualParam = entity.getPathParameters().get(i).toString();
					if (!paramName.equals(actualParam)) {
						throw new BusinessException(entity.getCode() +" endpoint is invalid. " +(i + 1) + "th path param is expected to be " + entity.getPathParameters().get(i) + " while actual value is " + paramName);
					}
					i++;

				} else {
					throw new BusinessException(entity.getCode() +" endpoint is invalid. Unexpected param " + paramName);
				}
			}

			if (entity.getPathParameters().size() > i) {
				throw new BusinessException(entity.getCode() +" endpoint is invalid. Missing param " + entity.getPathParameters().get(i));
			}
		}
	}
}