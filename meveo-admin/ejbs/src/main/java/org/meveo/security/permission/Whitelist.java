package org.meveo.security.permission;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.meveo.model.security.DefaultRole;

@Retention(RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Whitelist {

	DefaultRole[] value();
	
}
