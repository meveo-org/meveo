package org.meveo.api;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;
import javax.enterprise.inject.spi.CDI;
import javax.persistence.Entity;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.commons.utils.EjbUtils;

public class ApiUtils {
	
	/**
     * Get a corresponding API service for a given DTO object. Find API service class first trying with item's classname and then with its super class (a simplified version instead
     * of trying various classsuper classes)
     * 
     * @param dto DTO object
     * @param throwException Should exception be thrown if API service is not found
     * @return Api service
     * @throws MeveoApiException meveo api exception
     * @throws ClassNotFoundException class not found exception.
     */
    @SuppressWarnings("rawtypes")
    protected static ApiService getApiService(BaseEntityDto dto, boolean throwException) throws MeveoApiException, ClassNotFoundException {
        String entityClassName = dto.getClass().getSimpleName().substring(0, dto.getClass().getSimpleName().lastIndexOf("Dto"));

        return getApiService(entityClassName, throwException);
    }

    /**
     * Find API service class first trying with JPA entity's classname and then with its super class (a simplified version instead of trying various class superclasses).
     * 
     * @param classname JPA entity classname
     * @param throwException Should exception be thrown if API service is not found
     * @return Api service
     * @throws ClassNotFoundException class not found exception.
     */
    @SuppressWarnings("rawtypes")
    protected static ApiService getApiService(String classname, boolean throwException) throws ClassNotFoundException {

        Class clazz = Class.forName(classname);
        return getApiService(clazz, throwException);
    }

    /**
     * Find API service class first trying with JPA entity's classname and then with its super class (a simplified version instead of trying various class superclasses).
     * 
     * @param entityClass JPA entity class
     * @param throwException Should exception be thrown if API service is not found
     * @return Api service
     * 
     */
    @SuppressWarnings("rawtypes")
	public static ApiService getApiService(Class<?> entityClass, boolean throwException) {

        ApiService apiService = (ApiService) EjbUtils.getServiceInterface(entityClass.getSimpleName() + "Api", true);
        if(apiService == null){
        	Entity entityAnnot = entityClass.getAnnotation(Entity.class);
        	if(entityAnnot != null) {
        		apiService = (ApiService) EjbUtils.getServiceInterface(entityAnnot.name() + "Api", true);
        	}
        }

        if (apiService == null) {
            apiService = (ApiService) EjbUtils.getServiceInterface(entityClass.getSuperclass().getSimpleName() + "Api", true);
        }
        
        if(apiService == null) {
	        NamedLiteral nl = NamedLiteral.of(entityClass.getSimpleName() + "Api");
	        Instance<Object> selection = CDI.current().select(nl);
	        if(selection.isResolvable()) {
	        	apiService = (ApiService) selection.get();
	        }
        }
        
        if (apiService == null && throwException) {
            throw new RuntimeException("Failed to find implementation of API service for class " + entityClass.getName());
        }

        return apiService;
    }

    /**
     * Find API versioned service class first trying with JPA entity's classname and then with its super class (a simplified version instead of trying various class superclasses).
     * 
     * @param classname JPA entity classname
     * @param throwException Should exception be thrown if API service is not found
     * @return Api service
     * @throws ClassNotFoundException class not found exception.
     */
    @SuppressWarnings("rawtypes")
	public static ApiVersionedService getApiVersionedService(String classname, boolean throwException) throws ClassNotFoundException {

        Class clazz = Class.forName(classname);
        return getApiVersionedService(clazz, throwException);
    }

    /**
     * Find API versioned service class first trying with JPA entity's classname and then with its super class (a simplified version instead of trying various class superclasses).
     * 
     * @param entityClass JPA entity class
     * @param throwException Should exception be thrown if API service is not found
     * @return Api service
     *
     */
    @SuppressWarnings("rawtypes")
	public static ApiVersionedService getApiVersionedService(Class entityClass, boolean throwException) {

        ApiVersionedService apiService = (ApiVersionedService) EjbUtils.getServiceInterface(entityClass.getSimpleName() + "Api");
        if (apiService == null) {
            apiService = (ApiVersionedService) EjbUtils.getServiceInterface(entityClass.getSuperclass().getSimpleName() + "Api");
        }
        if (apiService == null && throwException) {
            throw new RuntimeException("Failed to find implementation of API service for class " + entityClass.getName());
        }

        return apiService;
    }

}
