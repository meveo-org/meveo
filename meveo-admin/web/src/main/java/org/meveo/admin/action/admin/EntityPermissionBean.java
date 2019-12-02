package org.meveo.admin.action.admin;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.model.security.Role;
import org.meveo.service.admin.impl.PermissionService;
import org.primefaces.model.DualListModel;

@Named
@ViewScoped
public class EntityPermissionBean implements Serializable {
	
	@Inject
	private PermissionService permissionService;

	private Map<Role, String> permissionsByRole;
	
	public EntityPermissionBean() {
		super();
	}

	@SuppressWarnings("unchecked")
	public void init(String permissionName, String entityId) {
		
		 /* String nativeQuery = "SELECT r,\r\n" + 
				"	CASE WHEN e.type IS NULL THEN '0'\r\n" + 
				"		 WHEN e.type = 'WHITE' THEN '1'\r\n" + 
				"		 WHEN e.type = 'BLACK' THEN '2'\r\n" + 
				"	 END\r\n" + 
				"FROM adm_role r LEFT JOIN entity_permission e ON e.role_id = r.id " + 
				"WHERE e.permission = :permission AND e.entity_id :entityId"; */
		
	
		
	}

	@SuppressWarnings("unchecked")
	public Map<Role, String> getPermissionsByRole(String permissionName, String entityId) {
		if(permissionsByRole == null) {
			permissionsByRole = permissionService.getEntityPermissionByRole(permissionName, entityId);
		}
		
		System.out.println(permissionsByRole);
		
		return permissionsByRole;
	}

	

}
