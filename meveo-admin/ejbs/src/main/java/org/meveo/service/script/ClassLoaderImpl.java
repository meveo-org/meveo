/**
 * 
 */
package org.meveo.service.script;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.tools.JavaFileObject;

/**
 * A custom ClassLoader which maps class names to JavaFileObjectImpl instances.
 */
final class ClassLoaderImpl extends URLClassLoader {
   private final Map<String, JavaFileObject> classes = new HashMap<String, JavaFileObject>();

   ClassLoaderImpl(final ClassLoader parentClassLoader) {
      super(new URL[0], parentClassLoader);
   }
   
   public void addUrl(URL url) {
	   addURL(url);
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
		
		try {
			return this.findClass(qualifiedClassName);
		} catch (ClassNotFoundException nf) {

		}
		
		// Workaround for "feature" in Java 6
		// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6434149
		try {
			Class<?> c = Class.forName(qualifiedClassName);
			return c;
		} catch (ClassNotFoundException nf) {

		}

		try {
			Class<?> c = ClassLoader.getSystemClassLoader().loadClass(qualifiedClassName);
			return c;
		} catch (ClassNotFoundException ignored) {

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
   protected synchronized Class<?> loadClass(final String name, final boolean resolve)
         throws ClassNotFoundException {
      return super.loadClass(name, resolve);
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