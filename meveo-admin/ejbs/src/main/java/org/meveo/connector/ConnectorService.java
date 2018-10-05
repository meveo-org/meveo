/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.connector;

import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.connector.ConnectorInstance;
import org.meveo.service.base.BusinessService;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Connector persistence service
 *
 * @author Clément Bareth
 */
public class ConnectorService extends BusinessService<ConnectorInstance> {

    /**
     * Retrieve the last version of the connector with the specified name
     *
     * @param name Name of the connector to retrieve
     * @return The last version number or empty if the connector does not exists
     */
    public Optional<Integer> latestVersionNumber(String name) {
        String queryString = "Select max(connector.version) from Connector connector \n" +
                "where connector.name = :name";
        Query q = getEntityManager().createQuery(queryString)
                .setParameter("name", name);
        try {
            return Optional.of((Integer) q.getSingleResult());
        } catch (NoResultException ignored) {
        }
        return Optional.empty();
    }

    /**
     * Retrieve all the version of the connector that have the specified name
     *
     * @param name Name of the connector to retrieve
     * @return The list of connector's version
     */
    public List<ConnectorInstance> findByName(String name) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "connector", null);
        qb.addCriterion("connector.name", "=", name, true);
        try {
            return (List<ConnectorInstance>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            log.warn("No CustomEntityInstance by name {} found", name);
        }
        return new ArrayList<>();
    }

    /**
     * Retrieve the latest version of the connector
     *
     * @param name Name of the connector to retrieve
     * @return The last version of the connector
     */
    public Optional<ConnectorInstance> findLatestByName(String name) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "connector", null);
        qb.addCriterion("connector.name", "=", name, true);
        qb.addSql("connector.version = (select max(ci.version) from ConnectorInstance ci where ci.name = connector.name)");
        try {
            return Optional.of((ConnectorInstance) qb.getQuery(getEntityManager()).getSingleResult());
        } catch (NoResultException e) {
            log.warn("No CustomEntityInstance by name {} found", name);
        }
        return Optional.empty();
    }

    /**
     * Retrieve a connector based on name and version
     *
     * @param name    Name of the connector to retrieve
     * @param version Version of the connector to retrieve
     * @return The retrieved connector or empty if not found
     */
    public Optional<ConnectorInstance> findByNameAndVersion(String name, Integer version) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "connector", null);
        qb.addCriterion("connector.name", "=", name, true);
        qb.addCriterion("connector.version", "=", version, true);
        try {
            return Optional.of((ConnectorInstance) qb.getQuery(getEntityManager()).getSingleResult());
        } catch (NoResultException e) {
            log.warn("No CustomEntityInstance by name {} and version {} found", name, version);
        }
        return Optional.empty();
    }

}
