/**
 * 
 */
package org.meveo.service.script;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.meveo.model.CustomEntity;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.custom.CustomEntityTemplateService;

public class CustomEntityClassLoader extends ClassLoader {
	private static Map<String, Class<?>> dynamicClasses = new ConcurrentHashMap<>();
	
	private CustomEntityTemplateService cetService;

	public CustomEntityClassLoader(CustomEntityTemplateService cetService, ClassLoader parentClassLoader) {
		super(parentClassLoader);
		this.cetService = cetService;
	}
	
	public Class<? extends CustomEntity> compile(CustomEntityTemplate cet) {
		var clazz = cetService.getCETClass(cet);
		dynamicClasses.put("org.meveo.model.customEntities." + cet.getCode(), clazz);
		return clazz;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (dynamicClasses.get(name) != null) {
			return dynamicClasses.get(name);
		}
		return super.loadClass(name, resolve);
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		if (dynamicClasses.get(name) != null) {
			return dynamicClasses.get(name);
		}
		
		try {
			return super.findClass(name);
		} catch (ClassNotFoundException e) {
			String cetCode = name.replaceFirst(".*\\.(.*)", "$1");
			CustomEntityTemplate cet = cetService.findByCode(cetCode);
			if (cet == null) {
				throw e;
			}
			return compile(cet);
		}
		
	}
	
	
}
