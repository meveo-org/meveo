package org.meveo.api;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.PermissionDto;
import org.meveo.api.dto.PermissionsDto;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.model.security.Permission;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.local.IPersistenceService;

@Stateless
public class PermissionApi extends BaseCrudApi<Permission, PermissionDto> {

	@Inject
	private PermissionService permissionService;

	public PermissionApi() {
		super(Permission.class, PermissionDto.class);
	}

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

	@Override
	public PermissionDto find(String code) throws EntityDoesNotExistsException, MissingParameterException
	{
		/*if (StringUtils.isBlank(name)) {
	        missingParameters.add("roleName");
	    }
	
	    handleMissingParameters();
	
	    if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationVisualization")))) {
	        throw new ActionForbiddenException("User has no permission to access roles");
	    }
	
	    RoleDto roleDto = null;
	    Role role = roleService.findByName(name);
	    if (role == null) {
	        throw new EntityDoesNotExistsException(Role.class, name, "name");
	    }
	    roleDto = new RoleDto(role, true, true);
	
	    return roleDto;*/
	return null;
	}

	@Override
	public Permission createOrUpdate(PermissionDto dtoData) throws MeveoApiException, BusinessException {
		/*
		 * String name = postData.getName(); if (name == null) {
		 * missingParameters.add("name"); }
		 * 
		 * handleMissingParameters();
		 * 
		 * if (!(currentUser.hasRole("superAdminManagement") ||
		 * (currentUser.hasRole("administrationManagement")))) { throw new
		 * ActionForbiddenException("User has no permission to manage roles"); }
		 * 
		 * Role role = roleService.findByName(name); if (role == null) { return
		 * create(postData); } else { return update(postData); }
		 */
		return null;
	}

	@Override
	public PermissionDto toDto(Permission entity) throws MeveoApiException {
		return PermissionDto.toDTO(entity);
	}

	@Override
	public IPersistenceService<Permission> getPersistenceService() {

		return permissionService;
	}

}