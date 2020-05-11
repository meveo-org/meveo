package org.meveo.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.meveo.model.module.MeveoModuleItem;

/**
 * This interface is use to order the module items during install.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 * @see MeveoModuleItem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface ModuleItemOrder {

	int value();
}
