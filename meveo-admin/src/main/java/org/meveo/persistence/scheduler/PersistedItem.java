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

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PersistedItem extends ItemToPersist {

    private String uuid;

    public PersistedItem(ItemToPersist itemToPersist, String uuid) {
        super(itemToPersist.getCode(), itemToPersist.getName(), itemToPersist.getValues());
        this.uuid = uuid;
    }
    
	public PersistedItem(String uuid, Map<String, Object> dto) {
		super(uuid, uuid, dto);
        this.uuid = uuid;
	}

	public String getUuid() {
        return uuid;
    }

    @Override
    @JsonProperty("properties")
    public Map<String, Object> getValues() {
        return super.getValues();
    }
}
