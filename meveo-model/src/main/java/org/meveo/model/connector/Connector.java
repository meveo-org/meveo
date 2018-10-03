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

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.ElementCollection;
import java.io.Serializable;
import java.util.List;

/**
 * A connector allows to retrieve entities in function of an initial context.
 *
 * @author Clément Bareth
 */
public class Connector extends ConnectorNode implements Serializable {

    @JsonProperty
    @ElementCollection
    private List<Description> descriptions;

    /**
     * Description of the inputs and outputs of the connector
     */
    public List<Description> getDescriptions() {
        return descriptions;
    }

    /**
     * @param descriptions Description of the inputs and outputs of the connector
     */
    public void setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
    }

}
