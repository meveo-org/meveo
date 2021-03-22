/**
 * 
 */
package org.meveo.model.customEntities.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import org.meveo.model.customEntities.CustomRelationshipTemplate;

/**
 * Indicate that a field is a reference to an other existing entity / set of entities
 * 
 * @author clement.bareth
 * @since 6.14.0
 * @version 6.14.0
 */
@Retention(RUNTIME)
public @interface Relation {

	/**
	 * @return the code of the associated {@link CustomRelationshipTemplate}
	 */
	String value();
}
