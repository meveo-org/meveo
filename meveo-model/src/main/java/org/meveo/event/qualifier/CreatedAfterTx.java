package org.meveo.event.qualifier;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

@Qualifier
@Target({ METHOD, FIELD, PARAMETER, TYPE })
@Retention(RUNTIME)

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
public @interface CreatedAfterTx {

}
