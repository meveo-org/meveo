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

package org.meveo.api.dto.git;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.meveo.api.dto.BusinessEntityDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * DTO representation of {@link org.meveo.model.git.GitRepository} entity
 * 
 * @author Clement Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@ApiModel("GitRepositoryDto")
public class GitRepositoryDto extends BusinessEntityDto {

	private static final long serialVersionUID = -3630922125856006992L;

	/**
	 * Roles that allows a user to make pull, fetch and clone actions
	 */
	@ApiModelProperty("List of reading roles that allows a user to make pull, fetch and clone actions")
	private List<String> readingRoles = new ArrayList<>();

	/**
	 * Roles that allows a user to make commit, merge and push actions
	 */
	@ApiModelProperty("List of writing roles that allows a user to make commit, merge and push actions")
	private List<String> writingRoles = new ArrayList<>();

	/**
	 * (Optional) Remote origin url if the repository is hosted somewhere else than
	 * locally
	 */
	@ApiModelProperty("Remote origin url if the repository is hosted somewhere else than locally")
	private String remoteOrigin;

	/**
	 * (Optional) Remote username to use when making action with distant repository.
	 * <br>
	 * If not provided, will use current user credentials.
	 */
	@ApiModelProperty("Remote username to use when making action with distant repository")
	private String remoteUsername;

	/**
	 * (Optional) Remote password to use when making action with distant repository.
	 * <br>
	 * If not provided, will use current user credentials.
	 */
	@ApiModelProperty("Remote password to use when making action with distant repository")
	private String remotePassword;
	
	@ApiModelProperty("Encrypted remote password")
	private String remotePasswordEncrypted;

	/**
	 * Whether the remote repository is hosted in a meveo instance
	 */
	@ApiModelProperty("Whether the remote repository is hosted in a meveo instance")
	private boolean meveoRepository;

	@ApiModelProperty("Default branch")
	private String defaultBranch = "master";

	@ApiModelProperty("Whether the default branch is checkout")
	private boolean locked;

	@JsonIgnore
	@ApiModelProperty("Current branch")
	private String currentBranch;

	@JsonIgnore
	@ApiModelProperty("List of branches")
	private List<String> branches;

	@JsonProperty
	public List<String> getBranches() {
		return branches;
	}

	public boolean isMeveoRepository() {
		return meveoRepository;
	}

	public void setMeveoRepository(boolean meveoRepository) {
		this.meveoRepository = meveoRepository;
	}

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

	public boolean isRemote() {
		return StringUtils.isNotBlank(this.remoteOrigin);
	}

	@JsonIgnore
	public void setBranches(List<String> branches) {
		this.branches = branches;
	}

	@JsonProperty
	public String getCurrentBranch() {
		if (currentBranch == null) {
			return "master";
		}

		return currentBranch;
	}

	@JsonIgnore
	public void setCurrentBranch(String currentBranch) {
		this.currentBranch = currentBranch;
	}

	/**
	 * @return the {@link #remotePasswordEncrypted}
	 */
	public String getRemotePasswordEncrypted() {
		return remotePasswordEncrypted;
	}

	/**
	 * @param remotePasswordEncrypted the remotePasswordEncrypted to set
	 */
	public void setRemotePasswordEncrypted(String remotePasswordEncrypted) {
		this.remotePasswordEncrypted = remotePasswordEncrypted;
	}

	public String getDefaultBranch() {
		return defaultBranch;
	}

	public void setDefaultBranch(String defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}
}
