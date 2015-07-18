/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * This registry is not multi thread safe. It is intended to be used from the
 * {@link JRTraceClassAndObjectCache} only in a synchronized manner.
 * 
 * 
 * Each instance stores the set of class loaders that is responsible for a
 * consistent set of JRTrace classes identified by the myCacheId field. (each
 * installation of classes installs a new set of JRTrace classes and gets its
 * own set of classloaders)
 * 
 * @author Christian Schenk
 *
 */
public class JRTraceClassLoaderRegistry {

	private Map<ClassLoader, JRTraceClassLoader> classLoaderMap = new HashMap<ClassLoader, JRTraceClassLoader>();
	private int myCacheId;
	private static Map<Integer, JRTraceClassLoaderRegistry> instances = new HashMap<Integer, JRTraceClassLoaderRegistry>();

	private JRTraceClassLoaderRegistry(int currentCacheId) {
		this.myCacheId = currentCacheId;
	}

	public static JRTraceClassLoaderRegistry getInstance(int currentCacheId) {
		JRTraceClassLoaderRegistry registry = instances.get(currentCacheId);
		if (registry == null) {
			registry = new JRTraceClassLoaderRegistry(currentCacheId);
			instances.put(currentCacheId, registry);
		}
		return registry;
	}

	public JRTraceClassLoader getClassLoader(ClassLoader classLoader) {

		JRTraceClassLoader enginexclassloader = classLoaderMap.get(classLoader);
		if (enginexclassloader == null) {

			enginexclassloader = new JRTraceClassLoader(classLoader, myCacheId);
			classLoaderMap.put(classLoader, enginexclassloader);
		}
		return enginexclassloader;
	}

	public void clear() {
		classLoaderMap.clear();
		instances.remove(myCacheId);

	}

}
