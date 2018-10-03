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

/**
 * Description of an output property of an entity or relation.
 *
 * @author Clément Bareth
 */
public class OutputProperty {

    /**
     * Name of the entity or relation's related property.
     */
    @JsonProperty(required = true)
    private String property;

    /**
     * Percentage of confidence we have that the property corresponds to what we really wanted.
     */
    @JsonProperty(required = true)
    private int trustness;

    /**
     * Name of the entity or relation's related property.
     * @return The name of the entity or relation's related property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Percentage of confidence we have that the property corresponds to what we really wanted.
     * @return The percentage of confidence defined by the user for the specified property
     */
    public int getTrustness() {
        return trustness;
    }
}
