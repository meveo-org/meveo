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
package org.meveo.model.technicalservice;

import org.meveo.model.crm.CustomFieldTemplate;

/**
 * Description of an output property of an entity or relation.
 *
 * @author Clément Bareth
 */
public class OutputProperty {

    private CustomFieldTemplate property;
    private int trustness;

    /**
     * @return CustomFieldTemplate related to the CustomEntityTemplate described
     */
    public CustomFieldTemplate getProperty() {
        return property;
    }

    /**
     * @param property CustomFieldTemplate related to the CustomEntityTemplate described
     */
    public void setProperty(CustomFieldTemplate property) {
        this.property = property;
    }

    /**
     * @return Percentage of confidence we have that the property corresponds to what we really wanted.
     */
    public int getTrustness() {
        return trustness;
    }

    /**
     * @param trustness Percentage of confidence we have that the property corresponds to what we really wanted
     */
    public void setTrustness(int trustness) {
        this.trustness = trustness;
    }
    
}
