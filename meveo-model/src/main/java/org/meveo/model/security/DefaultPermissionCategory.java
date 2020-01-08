package org.meveo.model.security;

public enum DefaultPermissionCategory {
	
	GIT("GIT");

	private String name;

	private DefaultPermissionCategory(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
