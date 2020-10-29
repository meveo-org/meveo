package org.meveo.model.security;

/**
 * Default permission categories.
 * 
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12
 */
public enum DefaultPermissionCategory {

	GIT("GIT"), //
	USER("USER"), //
	ENDPOINT("ENDPOINT");

	private String name;

	private DefaultPermissionCategory(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
