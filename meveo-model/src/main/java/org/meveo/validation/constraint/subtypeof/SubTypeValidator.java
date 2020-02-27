/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.validation.constraint.subtypeof;

import java.util.Collection;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Validates that the target entity is a subtype of specified class
 * 
 * @author clement.bareth
 * @since 6.0.0
 * @version 6.8.0
 */
public class SubTypeValidator implements ConstraintValidator<SubTypeOf, Object> {

    private Class<?> superTypeClass;

    @Override
    public void initialize(SubTypeOf constraintAnnotation) {
        superTypeClass = constraintAnnotation.value();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
    	if(value == null) {
    		return true;
    	}
    	
    	if(value instanceof Collection) {
    		Collection<?> collection = (Collection<?>) value;
    		if(collection.isEmpty()) {
    			return true;
    		}
    		
    		return superTypeClass.isAssignableFrom(collection.iterator().next().getClass());
    	} else {
    		return superTypeClass.isAssignableFrom(value.getClass());
    	}
    }
}
