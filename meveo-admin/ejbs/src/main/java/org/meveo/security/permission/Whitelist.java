package org.meveo.security.permission;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.meveo.model.security.DefaultRole;

/**
 * Will trigger creation of a white list entry for the annotated entity, specified role and required permissions specified by the {@link RequirePermission} annotation. <br>
 * If method does not have a {@link RequirePermission} annotation, will have no effect.
 * 
 * @author clement.bareth
 */
@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Whitelist {

	DefaultRole[] value() default {};
	
}
