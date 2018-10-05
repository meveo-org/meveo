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
package org.meveo.model.connector;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;

/**
 * @author Clément Bareth
 */
@Entity
@Table(name = "connectors", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "version"}))
@GenericGenerator(
        name = "ID_GENERATOR",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {@Parameter(name = "sequence_name", value = "connector_seq")}
)
public class ConnectorInstance extends BusinessEntity {

	private static final long serialVersionUID = 1L;

	@Type(type = "json")
    @Column(name = "connector", columnDefinition = "text")
    private Connector connector;

    @Column(name = "name")
    private String name;

    /**
     * Connector retrieved
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * @param connector Connector to store. Should not be null
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    /**
     * Name of the connector
     */
    public String getName() {
        return name;
    }

    /**
     * @param name Name of the connector
     */
    public void setName(String name) {
        this.name = name;
    }

}
