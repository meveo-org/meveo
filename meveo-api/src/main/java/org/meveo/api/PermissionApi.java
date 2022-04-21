package org.meveo.api;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.PermissionDto;
import org.meveo.api.dto.PermissionsDto;
import org.meveo.api.exception.ActionForbiddenException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.MeveoApiException;
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
	public PermissionDto find(String code) throws MeveoApiException {

		if (StringUtils.isBlank(code)) {
			missingParameters.add("code");
		}

		handleMissingParameters();

		if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationVisualization")))) {
			throw new ActionForbiddenException("User has no permission to access roles");
		}

		PermissionDto permissionDto = null;
		Permission permissionEntity = permissionService.findByCode(code);
		if (code == null) {
			throw new EntityDoesNotExistsException(Permission.class, code, "code");
		}
		permissionDto = new PermissionDto(permissionEntity);

		return permissionDto;

	}

	@Override
	public Permission createOrUpdate(PermissionDto dtoData) throws MeveoApiException, BusinessException {
		
		String description = dtoData.getDescription();
		if (StringUtils.isBlank(description)) {
			missingParameters.add("Description");
		}

		if (StringUtils.isBlank(dtoData.getCode())) {
			missingParameters.add("Code");
		}

		
		handleMissingParameters();

		if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationManagement")))) {
			throw new ActionForbiddenException("User has no permission to manage permissions.");
		}

		Permission permission = permissionService.findByCode(dtoData.getCode());

		if (permission == null) {
			return create(dtoData);
		} else {
			return update(dtoData);
		}
	}

	/**
	 *
	 * @param postData posted data to API
	 *
	 * @return Permission entity
	 * @throws MeveoApiException meveo api exception
	 * @throws BusinessException meveo api exception
	 */
	public Permission create(PermissionDto postData) throws MeveoApiException, BusinessException {

		String description = postData.getDescription();
		if (StringUtils.isBlank(description)) {
			missingParameters.add("Description");
		}

		if (StringUtils.isBlank(postData.getCode())) {
			missingParameters.add("Code");
		}

		handleMissingParameters();

		if (permissionService.findByCode(postData.getCode()) != null) {
			throw new EntityAlreadyExistsException(Permission.class, postData.getCode(), "code");
		}

		if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationManagement")))) {
			throw new ActionForbiddenException("User has no permission to manage permissions.");
		}

		Permission permission = new Permission();
		permission.setName(postData.getDescription());
		permission.setPermission(postData.getCode());
		permission.setDescription(postData.getDescription());
		permission.setCode(postData.getCode());
		
		permissionService.create(permission);

		return permission;
	}

	/**
	 * Update permission.
	 *
	 * @param postData Permission DTO
	 *
	 * @return Updated Permission entity
	 * @throws MeveoApiException meveo api exception
	 * @throws BusinessException business exception.
	 */
	public Permission update(PermissionDto postData) throws MeveoApiException, BusinessException {

		String description = postData.getDescription();
		if (StringUtils.isBlank(description)) {
			missingParameters.add("Description");
		}

		if (StringUtils.isBlank(postData.getCode())) {
			missingParameters.add("Code");
		}
		
		handleMissingParameters();

		if (!(currentUser.hasRole("superAdminManagement") || (currentUser.hasRole("administrationManagement")))) {
			throw new ActionForbiddenException("User has no permission to manage permission.");
		}

		Permission permission = permissionService.findByCode(postData.getCode());

		if (permission == null) {
			throw new EntityDoesNotExistsException(Permission.class, postData.getCode(), "code");
		}

		if (postData.getName() != null) {
			permission.setName(postData.getDescription());
			permission.setDescription(postData.getDescription());
		}

		if (postData.getPermission() != null) {
			permission.setPermission(postData.getCode());
			permission.setCode(postData.getCode());
		}
		
		
		return permissionService.update(permission);
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