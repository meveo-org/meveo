/**
 * 
 */
package org.meveo.api.persistence;

import java.lang.reflect.Field;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.persistence.CrossStorageService;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class CrossStorageApiFactory {
	
	@Inject
	private CrossStorageService crossStorageService;
	
	@Inject
	private CustomFieldsCacheContainerProvider cache;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Produces
	public CrossStorageApi createApi(InjectionPoint injectionPoint) {
		if(injectionPoint.getMember() instanceof Field) {
			Class<?> genericType = ReflectionUtils.getFieldGenericsType((Field) injectionPoint.getMember());
			return new CrossStorageApi(crossStorageService, cache, genericType);
		}
		
		return null;
	}
	
	public static <T> CrossStorageApi<T> get(Class<T> type) {
		CrossStorageService crossStorageService = CDI.current().select(CrossStorageService.class).get();
		CustomFieldsCacheContainerProvider cache = CDI.current().select(CustomFieldsCacheContainerProvider.class).get();
		return new CrossStorageApi<T>(crossStorageService, cache, type);
	}
}
