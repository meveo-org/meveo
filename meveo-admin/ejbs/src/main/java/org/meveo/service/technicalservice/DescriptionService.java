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
package org.meveo.service.technicalservice;

import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.Description;
import org.meveo.model.technicalservice.InputMeveoProperty;

import javax.inject.Inject;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

/**
 * EJB for managing Technical service's descriptions
 *
 * @author clement.bareth
 * @since 04.02.2019
 */
public class DescriptionService {

    @Inject
    @MeveoJpa
    private EntityManagerWrapper emWrapper;

    /**
     * Retrieve an input property description from service code, description name and property name
     *
     * @param serviceCode Code of the service to which the property belongs
     * @param descriptionName Name of the service's description to which the property belongs
     * @param propertyName Name of the property to retrieve
     * @return The JPA input property description
     */
    public InputMeveoProperty find(String serviceCode, String descriptionName, String propertyName){
        CriteriaBuilder cb = emWrapper.getEntityManager().getCriteriaBuilder();
        CriteriaQuery<InputMeveoProperty> query = cb.createQuery(InputMeveoProperty.class);

        Root<InputMeveoProperty> root = query.from(InputMeveoProperty.class);

        final Join<InputMeveoProperty, CustomFieldTemplate> cft = root.join("property");
        final Join<InputMeveoProperty, Description> description = root.join("description");
        final Join<Description, Function> service = description.join("service");

        final Predicate and = cb.and(
                cb.equal(cft.get("code"), propertyName),
                cb.equal(service.get("code"), serviceCode),
                cb.equal(description.get("name"), descriptionName)
        );

        query.where(and);

        return emWrapper.getEntityManager().createQuery(query).getSingleResult();
    }

}
