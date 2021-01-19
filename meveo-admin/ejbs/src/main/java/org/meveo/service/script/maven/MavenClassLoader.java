/**
 * 
 */
package org.meveo.service.script.maven;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import org.meveo.model.scripts.MavenDependency;
import org.meveo.service.script.CharSequenceCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class loader used to load maven libraries
 * 
 * @author clement.bareth
 * @since 6.14.0
 * @version 6.14.0
 */
public class MavenClassLoader extends URLClassLoader {
	
	private static final Logger LOG = LoggerFactory.getLogger(MavenClassLoader.class);
	private static final MavenClassLoader INSTANCE = new MavenClassLoader();
	
	/**
	 * @return an instance of the MavenClassLoader
	 */
	public static MavenClassLoader getInstance() {
		return INSTANCE;
	}
	
	private Set<MavenDependency> loadedLibraries = new HashSet<>();
	
	private MavenClassLoader() {
		super(new URL[] {}, MavenClassLoader.class.getClassLoader());
	}
	
	/**
	 * Load a maven library class. <br>
	 * Note : always use this method rather than {@link #loadClass(String)}, which should only be used internally
	 
	 * @param name Name of the class
	 * @return the class
	 * @throws ClassNotFoundException if the class can't be found
	 */
	public Class<?> loadExternalClass(String name) throws ClassNotFoundException {
		return super.loadClass(name);
	}

	@Override
	@Deprecated
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		try {
			return super.loadClass(name);
		} catch (Exception e) {
			// In case the library references a script or model class which is managed by Meveo
			return CharSequenceCompiler.getCompiledClass(name);
		}
	}
	
	/**
	 * @param mavenDependency the library to check
	 * @return whether the given library has been loaded
	 */
	public synchronized boolean isLibraryLoaded(MavenDependency mavenDependency) {
		return loadedLibraries.contains(mavenDependency);
	}

	/**
	 * Add the jar at the given location to the class loader
	 * 
	 * @param mavenDependency The maven dependency definition
	 * @param locations locations of the artifacts
	 */
	public synchronized void addLibrary(MavenDependency mavenDependency, Set<String> locations) {
		locations.forEach(location -> {
	        try {
		        File file = new File(location);
	            URL url = file.toURI().toURL();
	            super.addURL(url);
	        } catch (Exception e) {
	        	LOG.warn("Libray {} not added to classpath", location);
	            throw new RuntimeException(e);
	        }
		});
		loadedLibraries.add(mavenDependency);		
	}
	
}
