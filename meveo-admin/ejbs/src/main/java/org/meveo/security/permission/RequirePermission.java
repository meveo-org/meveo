package org.meveo.security.permission;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.interceptor.InterceptorBinding;

import org.meveo.model.security.DefaultPermission;
import org.meveo.model.security.DefaultRole;

@Retention(RUNTIME)
@InterceptorBinding
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface RequirePermission {
	
	/**
	 * This permission is required to access the method
	 */
	@Nonbinding DefaultPermission value() default DefaultPermission.NONE;
	
	/**
	 * At least one of these permissions is required to access the method 
	 */
	@Nonbinding DefaultPermission[] oneOf() default {};
	
	/**
	 * All of these permissions are quired to access the annotated method
	 */
	@Nonbinding DefaultPermission[] allOf() default {};
	
}
