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
package org.meveo.service.technicalservice.wsendpoint;

import java.io.File;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.meveo.admin.exception.BusinessException;
import org.meveo.keycloak.client.KeycloakAdminClientService;
import org.meveo.model.git.GitRepository;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.wsendpoint.WSEndpoint;
import org.meveo.service.base.BusinessService;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;

/**
 * EJB for managing technical services websocket endpoints
 *
 * @since 01.02.2019
 * @version 6.9.0
 */
@Stateless
public class WSEndpointService extends BusinessService<WSEndpoint> {

	public static final String EXECUTE_ALL_ENDPOINTS = "Execute_All_Endpoints";
	public static final String ENDPOINTS_CLIENT = "endpoints";
	public static final String EXECUTE_ENDPOINT_TEMPLATE = "Execute_Endpoint_%s";
	public static final String ENDPOINT_MANAGEMENT = "endpointManagement";

	@Context
	private HttpServletRequest request;

	@EJB
	private KeycloakAdminClientService keycloakAdminClientService;

	@Inject
	@MeveoRepository
	private GitRepository meveoRepository;

	public static String getEndpointPermission(WSEndpoint endpoint) {
		return String.format(EXECUTE_ENDPOINT_TEMPLATE, endpoint.getCode());
	}

	/**
	 * Retrieve all endpoints associated to the given service
	 *
	 * @param code Code of the service
	 * @return All endpoints associated to the service with the provided code
	 */
	public List<WSEndpoint> findByServiceCode(String code) {
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaQuery<WSEndpoint> query = cb.createQuery(WSEndpoint.class);
		Root<WSEndpoint> root = query.from(WSEndpoint.class);
		final Join<WSEndpoint, Function> service = root.join("service");
		query.where(cb.equal(service.get("code"), code));
		return getEntityManager().createQuery(query).getResultList();
	}

	
	/**
	 * Create a new endpoint in database. Also create associated client and roles in
	 * keycloak.
	 *
	 * @param entity Endpoint to create
	 */
	@Override
	public void create(WSEndpoint entity) throws BusinessException {
		super.create(entity);
	}


	/**
	 * Remove an endpoint from database. Also remove associated role in keycloak.
	 *
	 * @param entity Endpoint to remove
	 */
	@Override
	public void remove(WSEndpoint entity) throws BusinessException {
		super.remove(entity);
	}

	/**
	 * Checks if an endpoint interface is already created and pushed in git
	 * repository.
	 * 
	 * @param endpoint endpoint to search
	 * @return true if endpoint interface exists
	 */
	public boolean isEndpointScriptExists(WSEndpoint endpoint) {
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File endpointDir = new File(repositoryDir, "/wsendpoints/" + endpoint.getCode());
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
		final File f = new File(repositoryDir, "/wsendpoints/" + WSEndpoint.ENDPOINT_INTERFACE_JS + ".js");

		return f.exists() && !f.isDirectory();
	}

	public File getScriptFile(WSEndpoint endpoint) {
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File endpointDir = new File(repositoryDir, "/wsendpoints/" + endpoint.getCode());
		endpointDir.mkdirs();
		return new File(endpointDir, endpoint.getCode() + ".js");
	}
	
	public File getBaseScriptFile() {
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File endpointFile = new File(repositoryDir, "/wsendpoints/" + WSEndpoint.ENDPOINT_INTERFACE_JS + ".js");
		return endpointFile;
	}
}
