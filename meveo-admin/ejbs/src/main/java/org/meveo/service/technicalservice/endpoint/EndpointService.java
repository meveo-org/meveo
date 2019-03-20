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

import org.meveo.admin.exception.BusinessException;
import org.meveo.keycloak.client.KeycloakAdminClientService;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.service.base.BusinessService;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import java.util.List;

/**
 * EJB for managing technical services endpoints
 *
 * @author clement.bareth
 * @since 01.02.2019
 */
@Stateless
@DeclareRoles({EndpointService.ENDPOINT_MANAGEMENT})
@RolesAllowed({EndpointService.ENDPOINT_MANAGEMENT})
public class EndpointService extends BusinessService<Endpoint> {

    public static final String EXECUTE_ALL_ENDPOINTS = "Execute_All_Endpoints";
    public static final String ENDPOINTS_CLIENT = "endpoints";
    public static final String EXECUTE_ENDPOINT_TEMPLATE = "Execute_Endpoint_%s";
    public static final String ENDPOINT_MANAGEMENT = "endpointManagement";

    public static String getEndpointPermission(Endpoint entity) {
        return String.format(EXECUTE_ENDPOINT_TEMPLATE, entity.getCode());
    }

    @EJB
    private KeycloakAdminClientService keycloakAdminClientService;

    /**
     * Retrieve all endpoints associated to the given service
     *
     * @param code Code of the service
     * @return All endpoints associated to the service with the provided code
     */
    public List<Endpoint> findByServiceCode(String code){
        CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        CriteriaQuery<Endpoint> query = cb.createQuery(Endpoint.class);
        Root<Endpoint> root = query.from(Endpoint.class);
        final Join<Endpoint, Function> service = root.join("service");
        query.where(cb.equal(service.get("code"), code));
        return getEntityManager().createQuery(query).getResultList();
    }

    public List<Endpoint> findByParameterName(String code, String parameterName){
        return getEntityManager()
                .createNamedQuery("findByParameterName", Endpoint.class)
                .setParameter("serviceCode", code)
                .setParameter("propertyName", parameterName)
                .getResultList();
    }

    @Override
    public void create(Endpoint entity) throws BusinessException {
        super.create(entity);

        // Create client if not exitsts
        keycloakAdminClientService.createClient(ENDPOINTS_CLIENT);

        String endointPermission = getEndpointPermission(entity);

        // Create endpoint permission and add it to Execute_All_Endpoints composite
        keycloakAdminClientService.addToComposite(ENDPOINTS_CLIENT, endointPermission, EXECUTE_ALL_ENDPOINTS);

        // Add Execute_All_Endpoints to endpointManagement composite if not already in
        keycloakAdminClientService.addToCompositeCrossClient(ENDPOINTS_CLIENT, ENDPOINT_MANAGEMENT, EXECUTE_ALL_ENDPOINTS);
    }

    @Override
    public void remove(Endpoint entity) throws BusinessException {
        super.remove(entity);

        keycloakAdminClientService.removeRole(ENDPOINTS_CLIENT, getEndpointPermission(entity));
    }
}
