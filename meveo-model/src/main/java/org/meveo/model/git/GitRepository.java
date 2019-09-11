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

package org.meveo.model.git;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity reprensenting a git repository hosted in the meveo instance, or anywhere in remote
 * @author Clément Bareth
 * @lastModifiedVersion 6.4.0
 */
@Entity
@Table(name = "git_repository", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
public class GitRepository extends BusinessEntity {

    /**
     * Roles that allows a user to make pull, fetch and clone actions
     */
    @Column(name = "reading_roles", columnDefinition = "TEXT")
    @Type(type = "jsonList")
    private List<String> readingRoles = new ArrayList<>();

    /**
     * Roles that allows a user to make commit, merge and push actions
     */
    @Column(name = "reading_roles", columnDefinition = "TEXT")
    @Type(type = "jsonList")
    private List<String> writingRoles = new ArrayList<>();

    /**
     * (Optional) Remote origin url if the repository is hosted somewhere else than locally
     */
    @Column(name = "remote_origin")
    private String remoteOrigin;

    /**
     * (Optional) Remote username to use when making action with distant repository. <br>
     * If not provided, will use current user credentials.
     */
    @Column(name = "remote_username")
    private String remoteUsername;

    /**
     * (Optional) Remote password to use when making action with distant repository. <br>
     *  If not provided, will use current user credentials.
     */
    @Column(name = "remote_password")
    private String remotePassword;

    public List<String> getReadingRoles() {
        return readingRoles;
    }

    public void setReadingRoles(List<String> readingRoles) {
        this.readingRoles = readingRoles;
    }

    public List<String> getWritingRoles() {
        return writingRoles;
    }

    public void setWritingRoles(List<String> writingRoles) {
        this.writingRoles = writingRoles;
    }

    public String getRemoteOrigin() {
        return remoteOrigin;
    }

    public void setRemoteOrigin(String remoteOrigin) {
        this.remoteOrigin = remoteOrigin;
    }

    public String getRemoteUsername() {
        return remoteUsername;
    }

    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername = remoteUsername;
    }

    public String getRemotePassword() {
        return remotePassword;
    }

    public void setRemotePassword(String remotePassword) {
        this.remotePassword = remotePassword;
    }

    public boolean isRemote(){
        return StringUtils.isBlank(this.remoteOrigin);
    }
}
