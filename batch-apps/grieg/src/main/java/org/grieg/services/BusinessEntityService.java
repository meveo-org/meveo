/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.grieg.services;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.meveo.model.BusinessEntity;

public class BusinessEntityService<E extends BusinessEntity> extends PersistenceService<E> {

    public BusinessEntityService(EntityManager em) {
        super(em);
    }

    @SuppressWarnings("unchecked")
    public E findByCode(String code, String providerCode) {
        log.debug("start of find " + getEntityClass().getSimpleName() + " by code (code=" + code + ") ..");
        final Class<? extends E> productClass = getEntityClass();
        StringBuilder queryString = new StringBuilder("from " + productClass.getName() + " a");
        queryString.append(" where a.code =:code");
        queryString.append(" and a.provider.code =:providerCode");
        Query query = em.createQuery(queryString.toString());
        query.setParameter("code", code);
        query.setParameter("providerCode", providerCode);

        E e = (E) query.getResultList().get(0);

        log.debug("end of find " + getEntityClass().getSimpleName() + " by code (code=" + code + "). Result found=" + (e != null) + ".");
        return e;
    }
}
