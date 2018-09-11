package org.meveo.validation.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Constraint(validatedBy=ClassNameValidator.class)
@Target({METHOD, FIELD, PARAMETER, TYPE})
@Retention(RUNTIME)
public @interface ClassName  {
	String message() default "{org.meveo.validation.constraint.ClassName}";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
