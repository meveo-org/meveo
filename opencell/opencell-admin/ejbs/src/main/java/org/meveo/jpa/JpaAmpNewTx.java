package org.meveo.jpa;

import javax.interceptor.InterceptorBinding;
import java.lang.annotation.*;

/**
 * Annotation to bind interceptor that in case of application managed persistence context, a new EM will be instantiated for the period of a method call
 * 
 * @author Andrius Karpavicius
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface JpaAmpNewTx {

}