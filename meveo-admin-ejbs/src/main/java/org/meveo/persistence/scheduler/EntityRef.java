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

import org.apache.commons.lang3.StringUtils;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.storage.Repository;

import com.google.common.base.Objects;

public class EntityRef {
    private String uuid;
    private Integer trustScore;
    private String constraintCode;
    private String label;
    private Repository repository;

    public EntityRef(EntityReferenceWrapper wrapper) {
    	this.uuid = wrapper.getUuid();
    	if(this.uuid == null) {
    		this.uuid = wrapper.getCode();
    	}
    	
    	if(StringUtils.isBlank(uuid)) {
    		throw new IllegalArgumentException("UUID for entity with label " + label + " can't be null");
    	}
    	
    	this.label = wrapper.getClassnameCode();
    }

    public EntityRef(String uuid, String label) {
    	if(StringUtils.isBlank(uuid)) {
    		throw new IllegalArgumentException("UUID for entity with label " + label + "can't be null");
    	}
    	
        this.uuid = uuid;
        this.label = label;
    }

    public EntityRef(String uuid, Integer trustScore, String constraintCode, String label) {
    	if(StringUtils.isBlank(uuid)) {
    		throw new IllegalArgumentException("UUID for entity with label " + label + "can't be null");
    	}
    	
        this.uuid = uuid;
        this.trustScore = trustScore;
        this.constraintCode = constraintCode;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public boolean isTrusted() {
        return trustScore == null || new Integer(100).equals(trustScore);
    }

    public Integer getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(Integer trustScore) {
        this.trustScore = trustScore;
    }

    public String getConstraintCode() {
        return constraintCode;
    }

    public void setConstraintCode(String constraintCode) {
        this.constraintCode = constraintCode;
    }
    
    public Repository getRepository() {
		return repository;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityRef that = (EntityRef) o;
        return Objects.equal(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }
}
