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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.meveo.commons.utils.JsonUtils;
import org.meveo.keycloak.client.KeycloakAdminClientService;
import org.meveo.model.git.GitRepository;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.security.DefaultPermission;
import org.meveo.model.security.DefaultRole;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.security.permission.RequirePermission;
import org.meveo.security.permission.SecuredEntity;
import org.meveo.security.permission.Whitelist;
import org.meveo.service.base.BusinessService;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.meveo.service.script.ScriptInstanceService;

import com.fasterxml.jackson.databind.ObjectMapper;

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

	@Inject
	private ScriptInstanceService scriptInstanceService;

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
	@RequirePermission(value = DefaultPermission.EXECUTE_ENDPOINT, orRole = DefaultRole.ADMIN)
	public void create(@Whitelist(DefaultRole.ADMIN) Endpoint entity) throws BusinessException {

		// Create client if not exitsts
		// keycloakAdminClientService.createClient(ENDPOINTS_CLIENT);

		// String endpointPermission = getEndpointPermission(entity);

		// Create endpoint permission and add it to Execute_All_Endpoints composite
		// keycloakAdminClientService.addToComposite(ENDPOINTS_CLIENT,
		// endpointPermission, EXECUTE_ALL_ENDPOINTS);

		// Create endpointManagement role in default client if not exists
		// KeycloakAdminClientConfig keycloakConfig = KeycloakUtils.loadConfig();
		// keycloakAdminClientService.createRole(keycloakConfig.getClientId(),
		// ENDPOINT_MANAGEMENT);

		// Add endpoint role and selected composite roles
//		if (CollectionUtils.isNotEmpty(entity.getRoles())) {
//			for (String compositeRole : entity.getRoles()) {
//				keycloakAdminClientService.addToCompositeCrossClient(ENDPOINTS_CLIENT, keycloakConfig.getClientId(),
//						endpointPermission, compositeRole);
//			}
//		}
//
//		// Add Execute_All_Endpoints to endpointManagement composite if not already in
//		keycloakAdminClientService.addToCompositeCrossClient(ENDPOINTS_CLIENT, keycloakConfig.getClientId(),
//				EXECUTE_ALL_ENDPOINTS, ENDPOINT_MANAGEMENT);

		super.create(entity);
	}

	/**
	 * Remove an endpoint from database. Also remove associated role in keycloak.
	 *
	 * @param entity Endpoint to remove
	 */
	@Override
	@RequirePermission(value = DefaultPermission.EXECUTE_ENDPOINT, orRole = DefaultRole.ADMIN)
	public void remove(@SecuredEntity(remove = true) Endpoint entity) throws BusinessException {
		super.remove(entity);
//		String role = getEndpointPermission(entity);
//		keycloakAdminClientService.removeRole(ENDPOINTS_CLIENT, role);
//		if (CollectionUtils.isNotEmpty(entity.getRoles())) {
//			for (String compositeRole : entity.getRoles()) {
//				keycloakAdminClientService.removeRoleInCompositeRole(role, compositeRole);
//			}
//		}
//		keycloakAdminClientService.removeRole(role);
	}

	/**
	 * Update an endpoint.
	 *
	 * @param entity Endpoint to update
	 * @return the updated endpoint
	 */
	@Override
	public Endpoint update(Endpoint entity) throws BusinessException {

//		Endpoint endpoint = findById(entity.getId());
//		String oldEndpointPermission = getEndpointPermission(endpoint);
//
//		keycloakAdminClientService.removeRole(ENDPOINTS_CLIENT, oldEndpointPermission);
//
//		// Create client if not exitsts
//		keycloakAdminClientService.createClient(ENDPOINTS_CLIENT);
//
//		String endpointPermission = getEndpointPermission(entity);
//
//		// Create endpoint permission and add it to Execute_All_Endpoints composite
//		keycloakAdminClientService.addToComposite(ENDPOINTS_CLIENT, endpointPermission, EXECUTE_ALL_ENDPOINTS);
//
//		KeycloakAdminClientConfig keycloakConfig = KeycloakUtils.loadConfig();
//		List<String> roles = keycloakAdminClientService.getCompositeRolesByRealmClientId(keycloakConfig.getClientId(),
//				keycloakConfig.getRealm());
//		for (String compositeRole : roles) {
//			if (!compositeRole.equals(EXECUTE_ALL_ENDPOINTS)) {
//				keycloakAdminClientService.removeRoleInCompositeRole(oldEndpointPermission, compositeRole);
//			}
//		}
//
//		for (String compositeRole : entity.getRoles()) {
//			keycloakAdminClientService.addToCompositeCrossClient(ENDPOINTS_CLIENT, keycloakConfig.getClientId(),
//					endpointPermission, compositeRole);
//		}

		entity = super.update(entity);

		return entity;
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
		final File endpointDir = new File(repositoryDir, "/endpoints/" + endpoint.getCode());
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
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File endpointDir = new File(repositoryDir, "/endpoints/" + endpoint.getCode());
		endpointDir.mkdirs();
		return new File(endpointDir, endpoint.getCode() + ".js");
	}
	
	public File getBaseScriptFile() {
		final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
		final File endpointFile = new File(repositoryDir, "/endpoints/" + Endpoint.ENDPOINT_INTERFACE_JS + ".js");
		return endpointFile;
	}

	public String getJsonSchemaContent(Endpoint endpoint) {

		ScriptInstance scriptInstance = scriptInstanceService.findByCode(endpoint.getService().getCode());
		String content = scriptInstance.getScript();

		final File cetDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode() + "/src/main/java/custom/entities");

		List<File> files = new ArrayList<>();

		String regex = "import (.*?);";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			String className = matcher.group(1);
			if (className.startsWith("org.meveo.model.customEntities")) {
				String fileName = className.split("\\.")[4];
				File file = new File(cetDir.getAbsolutePath(), fileName + ".json");
				if (file.exists()) {
					files.add(file);
				}
				continue;
			}
		}

		List<Map<String, Object>> list = new ArrayList<>();
		if (!files.isEmpty()) {
			for (File file : files) {
				try {
					byte[] mapData = Files.readAllBytes(file.toPath());
					ObjectMapper objectMapper = new ObjectMapper();
					Map<String, Object> jsonMap = objectMapper.readValue(mapData, HashMap.class);
					list.add(jsonMap);
				} catch (IOException e) {
				}
			}
		}

		String json = JsonUtils.toJson(list, true);
		return json;
	}
}
