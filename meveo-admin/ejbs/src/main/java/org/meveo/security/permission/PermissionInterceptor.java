package org.meveo.security.permission;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.commons.lang.StringUtils;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.model.IEntity;
import org.meveo.model.security.DefaultPermission;
import org.meveo.model.security.DefaultRole;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.PermissionService;

@RequirePermission
@Interceptor
public class PermissionInterceptor {
	
	@Inject
	@CurrentUser
	private MeveoUser currentUser;
	
	@Inject
	private PermissionService permissionService;

	@AroundInvoke
	public Object before(InvocationContext context) throws Exception {
		Method method = context.getMethod();
		if(method == null) {
			return context.proceed();
		}
		
		RequirePermission requirePermission = method.getAnnotation(RequirePermission.class);
		String id = null;
		
		int nbSecuredEntities = 0;
		int parameterOrder = 0;
		
		Whitelist whitelistFinal = null;
		
		for(Parameter parameter : method.getParameters()) {
			SecuredEntity securedEntity = parameter.getAnnotation(SecuredEntity.class);
			Whitelist whitelist = parameter.getAnnotation(Whitelist.class);
			if(securedEntity != null || whitelist != null) {
				nbSecuredEntities++;
				if(nbSecuredEntities > 1) {
					throw new IllegalArgumentException(context.getClass() + "#" + method.getName() + " parameters can only be annotated once with @SecuredEntity or @Whitelist");
				}
				
				if(!parameter.getType().isPrimitive() && !IEntity.class.isAssignableFrom(parameter.getType())) {
					throw new IllegalArgumentException(context.getClass() + "#" + method.getName() + " : only primitive types or types that implements IEntity can be annotated with @SecuredEntity or @Whitelist");
				}
				
				if(parameter.getType().isPrimitive()) {
					id = String.valueOf(context.getParameters()[parameterOrder]);
					
				} else {
					id = String.valueOf(((IEntity<?>) context.getParameters()[parameterOrder]).getId());
				}
				
				if(whitelistFinal == null) {
					whitelistFinal = whitelist;
				}
				
			}
			
			parameterOrder++;
		}
		
		List<String> permissions = checkAuthorization(requirePermission, currentUser, id);
		
		Object result = context.proceed();
		
		// If secured entity has @Whitelist annotation, add the id of the entity to the corresponding whitelist
		if(whitelistFinal != null) {
			for(String permission : permissions) {
				for(DefaultRole role : whitelistFinal.value()) {
					permissionService.addToWhiteList(role.get(), DefaultPermission.findByName(permission).get(), id);
				}
			}
		}
		
		return result;
	}
	
	public List<String> checkAuthorization(String permission, String[] oneOf, String[] allOf, MeveoUser user, String id) throws UserNotAuthorizedException {
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
		
		if(!userHasPermission) {
			throw new UserNotAuthorizedException();
		}
		
		// Then if an id is defined, check the whitelists and black lists to make sure user have access to this specific resource
		if (id != null) {

			// All permissions should be validated against both black and white lists
			validPermissions.stream().filter(p -> {
				return isInWhiteList(p, id, user.getWhiteList()) && isNotInBlackList(p, id, user.getBlackList());
			}).findFirst().orElseThrow(() -> new UserNotAuthorizedException());

		}
		
		return validPermissions;
		
	}
	
	public List<String> checkAuthorization(RequirePermission requirePermission, MeveoUser user, String id) throws UserNotAuthorizedException {
		String permission = requirePermission.value().getPermission();
		String[] oneOf = Stream.of(requirePermission.oneOf()).map(DefaultPermission::getPermission).toArray(String[]::new);
		String[] allOf = Stream.of(requirePermission.allOf()).map(DefaultPermission::getPermission).toArray(String[]::new);
		
		return checkAuthorization(permission, oneOf, allOf, user, id);
	}
	
	private boolean isInWhiteList(String permission, String id, Map<String, List<String>> whiteList) {
		return whiteList.get(permission) == null ? true : whiteList.get(permission).contains(id);
	}
	
	private boolean isNotInBlackList(String permission, String id, Map<String, List<String>> whiteList) {
		return whiteList.get(permission) == null ? true : !whiteList.get(permission).contains(id);
	}
}
