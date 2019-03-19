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

package org.meveo.model.crm;

import org.meveo.model.customEntities.CustomEntityTemplate;

import java.io.Serializable;
import java.util.Objects;

public class CetUcPk implements Serializable {

    private String code;
    private CustomEntityTemplate customEntityTemplate;

    public CetUcPk(){

    }

    public CetUcPk(String code, CustomEntityTemplate customEntityTemplate) {
        this.code = code;
        this.customEntityTemplate = customEntityTemplate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public CustomEntityTemplate getCustomEntityTemplate() {
        return customEntityTemplate;
    }

    public void setCustomEntityTemplate(CustomEntityTemplate customEntityTemplate) {
        this.customEntityTemplate = customEntityTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CetUcPk cetUcPk = (CetUcPk) o;
        return Objects.equals(getCode(), cetUcPk.getCode()) &&
                Objects.equals(getCustomEntityTemplate(), cetUcPk.getCustomEntityTemplate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getCustomEntityTemplate());
    }
}
