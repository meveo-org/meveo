package org.meveo.model;

import java.lang.annotation.*;

/**
 * Denotes an entity that has a validity period
 * 
 * @author Andrius Karpavicius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface VersionedEntity {
}