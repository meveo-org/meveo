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

package org.meveo.api.rest.persistence;

import java.util.Objects;

public class EntityProperty {

    private String entityName;
    private String entityProperty;

    public EntityProperty(String entityName, String entityProperty) {
        this.entityName = entityName;
        this.entityProperty = entityProperty;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityProperty() {
        return entityProperty;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EntityProperty that = (EntityProperty) o;
        return Objects.equals(entityName, that.entityName) &&
                Objects.equals(entityProperty, that.entityProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityName, entityProperty);
    }
}
