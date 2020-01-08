package org.meveo.admin.action.admin;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.model.security.Permission;
import org.meveo.model.security.Role;
import org.meveo.service.admin.impl.PermissionService;

@Named
@ViewScoped
public class EntityPermissionBean implements Serializable {
	
	private static final long serialVersionUID = -2873317608273334815L;

	@Inject
	private PermissionService permissionService;
	
	private Map<String, List<RolePermission>> permissionsByRole = new HashMap<>();
	
	private String entityId;
	
	public EntityPermissionBean() {
		super();
	}

	public List<RolePermission> getPermissionsByRole(String permissionName, String entityId) {
		this.entityId = entityId;
		
		return permissionsByRole.computeIfAbsent(permissionName, k -> permissionService.getEntityPermissionByRole(permissionName, entityId)
				.entrySet()
				.stream()
				.map(e -> new RolePermission(e.getKey(), e.getValue()))
				.collect(Collectors.toList()));
	}
	
	public void save() {
		permissionsByRole.forEach((permission, mapping) -> {
			Permission persistencePermission = permissionService.findByPermission(permission);
			
			mapping.forEach(rp -> {
				switch(rp.getState()) {
					// Remove from white and black lists
					case "0" : 
						this.permissionService.removeFromWhiteList(rp.getRole(), permission, entityId);
						this.permissionService.removeFromBlackList(rp.getRole(), permission, entityId);
						break;
						
					// Remove from blacklist & add to whitelist
					case "1" : 
						this.permissionService.removeFromBlackList(rp.getRole(), permission, entityId);
						this.permissionService.addToWhiteList(rp.getRole(), persistencePermission, entityId);
						break;
						
					// Remove from whitelist & add to blacklist
					case "2" : 
						this.permissionService.removeFromWhiteList(rp.getRole(), permission, entityId);
						this.permissionService.addToBlackList(rp.getRole(), persistencePermission, entityId);
						break;
				}
			});
		});
	}
	
	public static class RolePermission {
		
		private Role role;
		private String state;
		
		public RolePermission() {
			super();
		}
		
		public RolePermission(Role role, String state) {
			super();
			this.role = role;
			this.state = state;
		}

		public Role getRole() {
			return role;
		}

		public void setRole(Role role) {
			this.role = role;
		}

		public String getState() {
			return state;
		}

		public void setState(String state) {
			this.state = state;
		}
		
	}

	

}
