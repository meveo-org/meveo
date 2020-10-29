package org.meveo.model.security;

import java.util.stream.Stream;

/**
 * Default permissions.
 * 
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12
 */
public enum DefaultPermission {
	
	NONE("", null, null),
	
	GIT_WRITE("git-write", DefaultPermissionCategory.GIT, -31L),
	GIT_READ("git-read", DefaultPermissionCategory.GIT, -32L),
	EXECUTE_ENDPOINT("execute-endpoint", DefaultPermissionCategory.ENDPOINT, -33L),
	USER_MANAGEMENT("userManagement", DefaultPermissionCategory.USER, -25L),
	USER_SELF_MANAGEMENT("userSelfManagement", DefaultPermissionCategory.USER, -26L);
	
	private String permission;
	private DefaultPermissionCategory category;
	private Long id;
	
	private DefaultPermission(String permission, DefaultPermissionCategory category, Long id) {
		this.permission = permission;
		this.category = category;
		this.id = id;
	}

	public String getPermission() {
		return permission;
	}

	public DefaultPermissionCategory getCategory() {
		return category;
	}

	public Long getId() {
		return id;
	}
	
	public Permission get() {
		Permission p = new Permission();
		p.setCategory(category.getName());
		p.setId(id);
		p.setPermission(permission);
		
		return p;
	}
	
	public static DefaultPermission findByName(String name) {
		return Stream.of(values())
				.filter(p -> p.permission.equals(name))
				.findFirst()
				.orElse(null);
	}
	
}
