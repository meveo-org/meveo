package org.meveo.model;

import java.lang.annotation.*;

/**
 * @author phung
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
@Inherited
public @interface CustomFieldEntity {

    /**
     * (Required) A custom field template code prefix.
     */
    /**
     * @return cft code prefix
     */
    String cftCodePrefix() default "";

    /**
     * Additional fields of an entity that should be included to complete a custom field template code value.
     * @return array of CFT code fields.
     */
    String[] cftCodeFields() default {};

    /**
     * Setting to true will allow the entity to be shown in the Custom Entities page.
     * @return true/false
     */
    boolean isManuallyManaged() default true;

}