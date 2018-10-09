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

import org.meveo.model.customEntities.CustomEntityTemplate;

/**
 * Description of an entity in stake for the connector.
 *
 * @author Clément Bareth
 */
public class EntityDescription extends Description {

    private String name;

    private CustomEntityTemplate type;

    @Override
    public String getName() {
        return name;
    }

    /**
     * Instance name of the variable described
     *
     * @param name Instance name of the variable described
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The CustomEntityTemplate described.
     *
     * @return The CET described
     */
    public CustomEntityTemplate getType() {
        return type;
    }

    /**
     * The CustomEntityTemplate described.
     *
     * @param customEntityTemplate The CustomEntityTemplate to describe
     */
    public void setType(CustomEntityTemplate customEntityTemplate) {
        this.type = customEntityTemplate;
    }

    @Override
    public String getTypeName() {
        return type.getCode();
    }

}
