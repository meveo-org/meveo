/**
 * 
 */
package org.meveo.admin.patches;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation used for patch execution ordering and scheduling
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface PatchDefinition {
	
	/** Execution order */
	int order();
	
	/** Unique patch name */
	String name();
}
