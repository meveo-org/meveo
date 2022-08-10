package org.meveo.service.base;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;

import org.hibernate.exception.ConstraintViolationException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementAlreadyExistsException;
import org.meveo.admin.exception.ElementNotFoundException;

/**
 * //TODO work on a better integration in the project : https://stackoverflow.com/q/24436046
 * @author sophie.perrineau
 * Conversion of exception/encapsulated exception to business exceptions when it is possible.
 * This conversion is usefull when an exception can't be  catch in the processing method, because exception is thrown when JPA commits transaction
 */
public class MeveoExceptionMapper {
	
	/**
     * Check an exception's causes, and return the BusinessException linked to this error
     * @param e
     * @param code optional : code of the element target of the action (used to build a message)
     * @return BusinessException or null, if the exception can't be converted.
     */
    public static BusinessException translatePersistenceException(Exception e, String className, String code) {
    	Throwable cause = e;
    	while (cause != null) {
       		if (cause instanceof BusinessException) {
       			return (BusinessException)cause;
       		} if (cause instanceof EntityExistsException) {
       			return new ElementAlreadyExistsException(code,className, e);
       		}else if (cause instanceof EntityNotFoundException) {
           			return new ElementNotFoundException(code, className, e);
       		} else if (cause instanceof ConstraintViolationException) {
				return new org.meveo.admin.exception.ConstraintViolationException("ConstraintViolation ("+((ConstraintViolationException)cause).getConstraintName()+") for " + className + " with code=" + code, e);
    		}
    		cause = cause.getCause();
    	}
    	return null;
    }

}
