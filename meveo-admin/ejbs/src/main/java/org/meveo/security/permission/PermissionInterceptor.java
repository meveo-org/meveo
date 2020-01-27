package org.meveo.security.permission;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.commons.lang.StringUtils;
import org.meveo.admin.exception.UserNotAuthorizedException;
import org.meveo.admin.listener.ApplicationInitializer;
import org.meveo.model.IEntity;
import org.meveo.model.security.DefaultPermission;
import org.meveo.model.security.DefaultRole;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.PermissionService;

/**
 * Security interceptor. Must always be executed last as it uses the final business method's return value
 * @author clement.bareth
 */
@RequirePermission
@Interceptor
public class PermissionInterceptor {
	
    private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();

    public static boolean isWrapperType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }
	
    private static Set<Class<?>> getWrapperTypes() {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }
	
	@Inject
	@CurrentUser
	private MeveoUser currentUser;
	
	@Inject
	private PermissionService permissionService;

	@SuppressWarnings("unchecked")
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
		IEntity<?> iEntity = null;
		boolean remove = false;
		
		for(Parameter parameter : method.getParameters()) {
			SecuredEntity securedEntity = parameter.getAnnotation(SecuredEntity.class);
			Whitelist whitelist = parameter.getAnnotation(Whitelist.class);
			if(securedEntity != null || whitelist != null) {
				nbSecuredEntities++;
				if(nbSecuredEntities > 1) {
					throw new IllegalArgumentException(context.getClass() + "#" + method.getName() + " parameters can only be annotated once with @SecuredEntity or @Whitelist");
				}
				
				if(!parameter.getType().isPrimitive() && !isWrapperType(parameter.getType()) && !IEntity.class.isAssignableFrom(parameter.getType())) {
					throw new IllegalArgumentException(context.getClass() + "#" + method.getName() + " : only primitive types or types that implements IEntity can be annotated with @SecuredEntity or @Whitelist");
				}
				
				if(parameter.getType().isPrimitive() || isWrapperType(parameter.getType())) {
					id = String.valueOf(context.getParameters()[parameterOrder]);
					
				} else {
					iEntity = (IEntity<?>) context.getParameters()[parameterOrder];
					if(iEntity.getId() != null) {
						id = String.valueOf(iEntity.getId());
					}
				}
				
				if(securedEntity != null) {
					remove = securedEntity.remove();
				}
				
				if(whitelistFinal == null) {
					whitelistFinal = whitelist;
				}
				
			}
			
			parameterOrder++;
		}
		
		List<String> permissions = checkAuthorization(requirePermission, currentUser, id);
		
		Object result = context.proceed();

		// In case of an annotated IEntity, the id might be set after the method execution
		if(iEntity != null && id == null) {
			id = String.valueOf(iEntity.getId());
		}
		
		// If secured entity has @Whitelist annotation, add or remove the id of the entity to the corresponding whitelist
		if(whitelistFinal != null && id != null) {
			for(String permission : permissions) {
				for(DefaultRole role : whitelistFinal.value()) {
					permissionService.addToWhiteList(role.get(), DefaultPermission.findByName(permission).get(), id);
				} 
			}
		}
		
		if(remove && id != null) {
			for(String permission : permissions) {
				permissionService.removeFromEntityPermissions(permission, id);
			}
		}
		
		String orRole = requirePermission.orRole().getRoleName();
		
		// In case we did not have a @SecuredEntity annotation, we need to check blacklist / whitelist on the returned value
		if(id == null && result instanceof IEntity) {
			iEntity = (IEntity<?>) result;
			if(iEntity.getId() != null) {
				id = String.valueOf(iEntity.getId());
				checkWhiteAndBlackLists(id, permissions, currentUser, orRole);
			}
			
		} else if(id == null && result instanceof Collection) {
			Collection<?> resultCollection = (Collection<?>) result;
			if(!resultCollection.isEmpty()) {
				Object firstItem = resultCollection.iterator().next();
				if(firstItem instanceof IEntity) {
					Collection<IEntity<?>> iEntityCollection =  (Collection<IEntity<?>>) resultCollection;
					for (IEntity<?> e : new ArrayList<>(iEntityCollection)) {
						try {
							if(e.getId() != null) {
								id = String.valueOf(e.getId());
								checkWhiteAndBlackLists(id, permissions, currentUser, orRole);
							}
						} catch(UserNotAuthorizedException ex) {
							iEntityCollection.remove(e);
						}
					}
 				}
			}
		}
		
		return result;
	}

	/**
	 * @param id
	 * @param permissions
	 * @param orRole TODO
	 * @throws UserNotAuthorizedException
	 */
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
