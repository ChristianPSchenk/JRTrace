/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import java.util.HashMap;
import java.util.Map;

import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.JRLog;

public class EngineXClassHolder {

	private EngineXMetadata metadata;
	private Map<ClassLoader, Class<?>> classCache = new HashMap<ClassLoader, Class<?>>();
	private Map<ClassLoader, Object> objectCache = new HashMap<ClassLoader, Object>();

	public EngineXClassHolder(EngineXMetadata metadata) {

		this.metadata = metadata;

	}

	/**
	 * Deadlock danger? Classloading and construction of enginex objects happens
	 * here while holding the lock. Rule: constructor of enginex objects should
	 * not obtain any locks
	 * 
	 * @param classLoader
	 */
	synchronized private void prepareEngineXClass(ClassLoader classLoader) {

		boolean contained = classCache.containsKey(classLoader);

		if (!contained) {

			EngineXClassLoader enginexclassloader = new EngineXClassLoader(
					classLoader, metadata);
			Class<?> mainClass = null;
			try {

				mainClass = enginexclassloader.loadClass(metadata
						.getExternalClassName());
				JRLog.debug("JRTrace class loaded: "
						+ metadata.getExternalClassName()
						+ " with classloader "
						+ ((mainClass.getClassLoader() == null) ? "null"
								: mainClass.getClassLoader().toString()));
				classCache.put(classLoader, mainClass);
				objectCache.put(classLoader, mainClass.newInstance());

			} catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	synchronized public Object getObject(ClassLoader classLoader) {
		if (!(metadata.getClassLoaderPolicy() == XClassLoaderPolicy.TARGET)) {
			if (metadata.getClassLoaderPolicy() == XClassLoaderPolicy.BOOT)

			{
				classLoader = null;
			} else {
				if (metadata.getClassLoaderPolicy() == XClassLoaderPolicy.NAMED) {
					String classForLoader = metadata.getClassLoaderName();
					if (classForLoader.isEmpty())
						throw new RuntimeException(
								"No classloader class defined");

					classLoader = HelperLib
							.getCachedClassLoader(classForLoader);
					if (classLoader == null) {
						throw new RuntimeException("The classloader of class "
								+ classForLoader + " could not be identified.");
					}
				} else
					throw new RuntimeException("Unknown classloader policy");

			}
		}
		prepareEngineXClass(classLoader);
		return objectCache.get(classLoader);
	}

	public EngineXMetadata getMetadata() {
		return metadata;
	}

	/**
	 * 
	 * @return an
	 */
	synchronized public Class<?> getEngineXClass() {

		if (metadata.getClassLoaderPolicy() != XClassLoaderPolicy.TARGET) {
			getObject(null);
		}
		int size = classCache.size();
		if (size == 1)
			return classCache.values().iterator().next();

		return null;
	}
}
