/**
 * 
 */
package org.meveo.service.script.maven;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class MavenClassLoader {
	
	private static final Logger LOG = LoggerFactory.getLogger(MavenClassLoader.class);
	
	private static final URLClassLoader urlClassLoader;
	private static final Method addUrlMethod;
	
	static {
		urlClassLoader = new URLClassLoader(new URL[] {}, MavenClassLoader.class.getClassLoader());
		try {
			addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			addUrlMethod.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Class<?> loadClass(String className) throws ClassNotFoundException {
		return urlClassLoader.loadClass(className);
	}

	public static void addLibrary(String location) {
        File file = new File(location);
	
        try {
            URL url = file.toURI().toURL();
            addUrlMethod.invoke(urlClassLoader, url);
        } catch (Exception e) {
        	LOG.warn("Libray {} not added to classpath", location);
            throw new RuntimeException(e);
        }
        
	}
}
