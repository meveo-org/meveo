package org.meveo.model;

import java.lang.annotation.*;

/**
 * Specifies that CRUD operation on an entity should fire events
 * 
 * @author Andrius Karpavicius
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface ObservableEntity {

}
