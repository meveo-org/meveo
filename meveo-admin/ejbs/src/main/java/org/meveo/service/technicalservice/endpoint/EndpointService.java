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
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.BusinessService;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
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
@DeclareRoles({"endpointManagement"})
@RolesAllowed({"endpointManagement"})
public class EndpointService extends BusinessService<Endpoint> {

    @Inject
    private PermissionService permissionService;

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
    protected void beforeUpdateOrCreate(Endpoint entity) throws BusinessException {
        String endointPermission = String.format("Execute_Endpoint_%s", entity.getCode());
        permissionService.createIfAbsent(endointPermission, "Execute_All_Endpoints", "SUPER_ADMIN");
        keycloakAdminClientService.createClient("endpoints");
    }
}
