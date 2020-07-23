/**
 * 
 */
package org.meveo.service.script;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileObject;

import org.elasticsearch.common.util.ArrayUtils;
import org.meveo.service.custom.CustomEntityTemplateService;

/**
 * A custom ClassLoader which maps class names to JavaFileObjectImpl instances.
 */
final class ClassLoaderImpl extends URLClassLoader {
   private final Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();
   
   private static final URL[] urls;
   private static final Set<URL> additionalLibraries = new HashSet<>();
   
	static {
		try {
			urls = new URL[] { CustomEntityTemplateService.getClassesDir(null).toURI().toURL() };
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

   ClassLoaderImpl(final ClassLoader parentClassLoader) {
      super(urls, parentClassLoader);
   }
   
   public void addUrl(URL url) {
	   addURL(url);
	   additionalLibraries.add(url);
   }

   /**
    * @return An collection of JavaFileObject instances for the classes in the
    *         class loader.
    */
   Collection<JavaFileObject> files() {
      return Collections.unmodifiableCollection(classes.values());
   }

	@Override
	protected Class<?> findClass(final String qualifiedClassName) throws ClassNotFoundException {
		JavaFileObject file = classes.get(qualifiedClassName);
		if (file != null) {
			byte[] bytes = ((JavaFileObjectImpl) file).getByteCode();
			return defineClass(qualifiedClassName, bytes, 0, bytes.length);
		}

		return super.findClass(qualifiedClassName);
	}

   /**
    * Add a class name/JavaFileObject mapping
    * 
    * @param qualifiedClassName
    *           the name
    * @param javaFile
    *           the file associated with the name
    */
   void add(final String qualifiedClassName, final JavaFileObject javaFile) {
      classes.put(qualifiedClassName, javaFile);
   }
   
	@Override
	protected synchronized Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
		URL[] libs = additionalLibraries.toArray(new URL[additionalLibraries.size()]);
		URL[] urlsToUse = ArrayUtils.concat(this.getURLs(), libs, URL.class);
		try (URLClassLoader tempClassLoader = new URLClassLoader(urlsToUse, this.getParent())){ 
			return tempClassLoader.loadClass(name);
		} catch (Exception e) {
			return super.loadClass(name, resolve);
		}
	}

   @Override
   public InputStream getResourceAsStream(final String name) {
      if (name.endsWith(".class")) {
         String qualifiedClassName = name.substring(0,
               name.length() - ".class".length()).replace('/', '.');
         JavaFileObjectImpl file = (JavaFileObjectImpl) classes.get(qualifiedClassName);
         if (file != null) {
            return new ByteArrayInputStream(file.getByteCode());
         }
      }
      return super.getResourceAsStream(name);
   }
}