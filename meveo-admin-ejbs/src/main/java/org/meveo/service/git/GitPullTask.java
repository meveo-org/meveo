package org.meveo.service.git;

import java.io.Serializable;

public class GitPullTask implements Serializable {

	private String user;
	private String password;
	private Long repoId;
	
	public GitPullTask(String user, String password, Long repoId) {
		this.user = user;
		this.password = password;
		this.repoId = repoId;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Long getRepoId() {
		return repoId;
	}

	public void setRepoId(Long repoId) {
		this.repoId = repoId;
	}
}
