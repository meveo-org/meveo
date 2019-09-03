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
package org.meveo.api.dto.technicalservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.interfaces.technicalservice.description.properties.PropertyDescription;

/**
 * Description of an output property of an entity or relation.
 *
 * @author Clément Bareth
 */
public class OutputPropertyDto implements PropertyDescription {

    @JsonProperty(required = true)
    private String property;

    private int trustness = 0;

    private String value;

    /**
     * @return The expected value for that property
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value The expected value for that property
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * @return The code of the CustomFieldTemplate linked to the CET
     */
    @Override
    public String getProperty() {
        return property;
    }

    /**
     * @param property he code of the CustomFieldTemplate linked to the CET
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * @return The percentage of confidence defined by the user for the specified property
     */
    public int getTrustness() {
        return trustness;
    }

    /**
     * @param trustness The percentage of confidence defined by the user for the specified property
     */
    public void setTrustness(int trustness) {
        this.trustness = trustness;
    }

}
