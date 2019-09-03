/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.catalog.impl;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.catalog.ServiceTemplate;
import org.meveo.service.base.BusinessService;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import java.util.List;

/**
 * Service Template service implementation.
 * 
 */
@Stateless
public class ServiceTemplateService extends BusinessService<ServiceTemplate> {

    @Override
    public void create(ServiceTemplate serviceTemplate) throws BusinessException {
        super.create(serviceTemplate);
    }

    @Override
    public ServiceTemplate update(ServiceTemplate serviceTemplate) throws BusinessException {
        ServiceTemplate result = super.update(serviceTemplate);
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<ServiceTemplate> listAllActiveExcept(ServiceTemplate st) {
        QueryBuilder qb = new QueryBuilder(ServiceTemplate.class, "s", null);
        qb.addCriterion("id", "<>", st.getId(), true);

        try {
            return (List<ServiceTemplate>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public synchronized void duplicate(ServiceTemplate entity) throws BusinessException {
        entity = refreshOrRetrieve(entity);
        // Lazy load related values first
        String code = findDuplicateCode(entity);
        // Detach and clear ids of entity and related entities
        detach(entity);
        entity.setId(null);
        entity.setCode(code);
        create(entity);
    }

}