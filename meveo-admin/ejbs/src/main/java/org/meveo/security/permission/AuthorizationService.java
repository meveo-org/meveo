/**
 * 
 */
package org.meveo.security.permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.admin.listener.ApplicationInitializer;
import org.meveo.model.security.DefaultPermission;
import org.meveo.model.security.DefaultRole;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;

/**
 * Checks whether a user can access a given resource
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class AuthorizationService {
	
	@Inject
	@CurrentUser
	private MeveoUser currentUser;

	public void checkWhiteAndBlackLists(String id, List<String> permissions, MeveoUser user, String orRole) throws UserNotAuthorizedException {
		if(orRole != null && user.getRoles().contains(orRole)) {
			return;
		}
		
		for(String p : permissions) {
			if(!isInWhiteList(p, id, user.getWhiteList()) || !isNotInBlackList(p, id, user.getBlackList())) {
				throw new UserNotAuthorizedException();
			}
		}
	}
	
	public void checkAuthorization(DefaultPermission permission, DefaultRole orRole, Object id) {
		checkAuthorization(permission.getPermission(), null, null, currentUser, String.valueOf(id), orRole.getRoleName());
	}
	
	public List<String> checkAuthorization(String permission, String[] oneOf, String[] allOf, MeveoUser user, String id, String orRole) throws UserNotAuthorizedException {
		boolean userHasPermission = true;
		
		List<String> validPermissions = new ArrayList<>();
		
		// First check if user has required permission
		if(!StringUtils.isEmpty(permission)) {
			if(!user.hasRole(permission)) {
				userHasPermission = false;
			} else {
				validPermissions.add(permission);
			}
		}
		
		if(oneOf != null && oneOf.length > 0) {
			userHasPermission = Stream.of(oneOf).anyMatch(user::hasRole);
			Stream.of(oneOf).filter(user::hasRole).forEach(validPermissions::add);
		}
		
		if(allOf != null && allOf.length > 0) {
			userHasPermission = Stream.of(allOf).allMatch(user::hasRole);
			if(userHasPermission) {
				validPermissions.addAll(Arrays.asList(allOf));
			}
		}
		
		// Bypass security check at initialization
		if(user.getUserName().equals(ApplicationInitializer.APPLICATION_INITIALIZER)) {
			return validPermissions;
		}
		
		// If a role is specified, grant access if user has target role
		if(orRole != null && user.getRoles().contains(orRole)) {
			return validPermissions;
		}
		
		if(!userHasPermission) {
			throw new UserNotAuthorizedException();
		}
		
		// Then if an id is defined, check the whitelists and black lists to make sure user have access to this specific resource
		if (id != null) {
			checkWhiteAndBlackLists(id, validPermissions, user, orRole);
		}
		
		return validPermissions;
		
	}
	
	public List<String> checkAuthorization(RequirePermission requirePermission, MeveoUser user, String id) throws UserNotAuthorizedException {
		String permission = requirePermission.value().getPermission();
		String[] oneOf = Stream.of(requirePermission.oneOf()).map(DefaultPermission::getPermission).toArray(String[]::new);
		String[] allOf = Stream.of(requirePermission.allOf()).map(DefaultPermission::getPermission).toArray(String[]::new);
		DefaultRole orRole = requirePermission.orRole();
		
		return checkAuthorization(permission, oneOf, allOf, user, id, orRole.getRoleName());
	}
	
	private boolean isInWhiteList(String permission, String id, Map<String, List<String>> whiteList) {
		return whiteList.get(permission) == null ? true : whiteList.get(permission).contains(id);
	}
	
	private boolean isNotInBlackList(String permission, String id, Map<String, List<String>> blacklist) {
		return blacklist.get(permission) == null ? true : !blacklist.get(permission).contains(id);
	}
	
}
