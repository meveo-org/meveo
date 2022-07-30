/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package org.meveo.service.admin.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.ws.rs.QueryParam;

public class MeveoModuleFilters {

    @QueryParam("active")
    private Boolean active;

    @QueryParam("downloaded")
    private Boolean downloaded;
    
    @QueryParam("code")
    private String code;

    @QueryParam("itemCode")
    private String itemCode;

    @QueryParam("itemType")
    private String itemType;

    @JsonIgnore
    private transient String itemClass;
    
    /**
	 * @return the {@link #code}
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(Boolean downloaded) {
        this.downloaded = downloaded;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemClass() {
        return itemClass;
    }

    public void setItemClass(String itemClass) {
        this.itemClass = itemClass;
    }
}
