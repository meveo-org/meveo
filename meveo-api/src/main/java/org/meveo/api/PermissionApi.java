package org.meveo.api;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.api.dto.PermissionDto;
import org.meveo.api.dto.PermissionsDto;
import org.meveo.model.security.BlackListEntry;
import org.meveo.model.security.EntityPermission;
import org.meveo.model.security.Permission;
import org.meveo.model.security.Role;
import org.meveo.model.security.WhiteListEntry;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.admin.impl.RoleService;

@Stateless
public class PermissionApi extends BaseApi {

    @Inject
    private PermissionService permissionService;
    
    @Inject
    private RoleService roleService;
    
    private Role role;
    private Permission permission;
    
    public PermissionsDto list() {
        PermissionsDto permissionsDto = new PermissionsDto();

        List<Permission> permissions = permissionService.list();
        if (permissions != null && !permissions.isEmpty()) {
            for (Permission p : permissions) {
                PermissionDto pd = new PermissionDto(p);
                permissionsDto.getPermission().add(pd);
            }
        }

        return permissionsDto;
    }
    
	public void addToWhiteList(String permissionName, String id, String roleName) {
		fetchRoleAndPermission(permissionName, roleName);
		permissionService.addToWhiteList(role, permission, id);
	}

	public void addToBlackList(String permissionName, String id, String roleName) {
		fetchRoleAndPermission(permissionName, roleName);
		permissionService.addToBlackList(role, permission, id);
	}

	public void removeEntityPermission(String permissionName, String id, String roleName) {
		fetchRoleAndPermission(permissionName, roleName);
		permissionService.removeEntityPermission(role, permission, id);
	}
	
	private <T extends EntityPermission> void fetchRoleAndPermission(String permissionName, String roleName) {
		Role role = roleService.findByName(roleName);
		if(role == null) {
			throw new IllegalArgumentException("Role " + roleName + " does not exists");
		}
		
		Permission permission = permissionService.findByPermission(permissionName);
		if(permission == null) {
			throw new IllegalArgumentException("Permission " + permissionName + " does not exists");
		}
	}
}