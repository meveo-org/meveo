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

package org.meveo.persistence.scheduler;

import java.util.Map;
import java.util.Objects;

public class RelationToPersist extends ItemToPersist {

    private EntityToPersist startEntityToPersist;
    private EntityToPersist endEntityToPersist;

    public RelationToPersist(String code, String name, Map<String, Object> values, EntityToPersist startEntityToPersist, EntityToPersist endEntityToPersist) {
        super(code, name, values);
        this.startEntityToPersist = startEntityToPersist;
        this.endEntityToPersist = endEntityToPersist;
    }

    public EntityToPersist getStartEntityToPersist() {
        return startEntityToPersist;
    }

    public void setStartEntityToPersist(EntityToPersist startEntityToPersist) {
        this.startEntityToPersist = startEntityToPersist;
    }

    public EntityToPersist getEndEntityToPersist() {
        return endEntityToPersist;
    }

    public void setEndEntityToPersist(EntityToPersist endEntityToPersist) {
        this.endEntityToPersist = endEntityToPersist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RelationToPersist relationToPersist = (RelationToPersist) o;
        return Objects.equals(startEntityToPersist, relationToPersist.startEntityToPersist) &&
                Objects.equals(endEntityToPersist, relationToPersist.endEntityToPersist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startEntityToPersist, endEntityToPersist);
    }

}
