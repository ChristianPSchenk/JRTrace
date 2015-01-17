/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import java.util.HashMap;
import java.util.Map;

import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationMessages;

public class EngineXClassHolder {

	private EngineXMetadata metadata;
	private Map<ClassLoader, Class<?>> classCache = new HashMap<ClassLoader, Class<?>>();
	private Map<ClassLoader, Object> objectCache = new HashMap<ClassLoader, Object>();

	public EngineXClassHolder(EngineXMetadata metadata) {

		this.metadata = metadata;

	}

	/**
	 * Creates the JRTrace object that will be injected into target objects.
	 * Note: each JRTrace object that is used for injection therefore requires a
	 * public no-argument constructor.
	 * 
	 * Deadlock danger? Classloading and construction of enginex objects happens
	 * here while holding the lock. Rule: constructor of enginex objects should
	 * not obtain any locks
	 * 
	 * @param classLoader
	 */
	synchronized private void prepareEngineXClass(ClassLoader classLoader) {

		boolean contained = classCache.containsKey(classLoader);

		if (!contained) {

			
			EngineXClassLoader enginexclassloader= EngineXClassLoaderRegistry.getInstance().getClassLoader(classLoader);
			enginexclassloader.addMetadata(metadata);
			Class<?> mainClass = null;
			
			
			try {
				mainClass = enginexclassloader.loadClass(metadata
						.getExternalClassName());
			} catch (ClassNotFoundException e1) {
				throw new RuntimeException(e1);
			}
			JRLog.debug("JRTrace class loaded: "
					+ metadata.getExternalClassName()
	 				+ " with classloader "
					+ ((mainClass.getClassLoader() == null) ? "null"
							: mainClass.getClassLoader().toString()));
			classCache.put(classLoader, mainClass);
			
			
			
			try {
				
				if(metadata.hasXClassAnnotation())
				{
					Object mainInstance = mainClass.newInstance();			
					objectCache.put(classLoader,mainInstance);
				}

			} catch (  IllegalAccessException |InstantiationException e) {
				NotificationUtil
				.sendProblemNotification(
						NotificationMessages.MESSAGE_MISSING_NO_ARGUMENT_CONSTRUCTOR,

						EngineXNameUtil
								.getExternalName(metadata
										.getClassName()), "",
						"");
			}
		}
	}

	synchronized public Object getObject(ClassLoader classLoader) {
		classLoader = identifyJRTraceClassLoader(classLoader);
		prepareEngineXClass(classLoader);
		return objectCache.get(classLoader);
	}

	/**
	 * Returns the proper classloader to use for the injected code based on the classloader of the target class and the classloader
	 * policy used by the JRTrce class
	 * 
	 * @param classLoader the classloader of the target class
	 * @return the classloader to use for the creation of the injected methods
	 */
	private ClassLoader identifyJRTraceClassLoader(ClassLoader classLoader) {
		if (!(metadata.getClassLoaderPolicy() == XClassLoaderPolicy.TARGET)) {
			if (metadata.getClassLoaderPolicy() == XClassLoaderPolicy.BOOT)

			{
				classLoader = null;
			} else {
				if (metadata.getClassLoaderPolicy() == XClassLoaderPolicy.NAMED) {
					String classForLoader = metadata.getClassLoaderName();
					if (classForLoader == null || classForLoader.isEmpty()) {
						NotificationUtil
								.sendProblemNotification(
										"ClassloaderPolicy is NAMED, but no 'classname' annotation was provided. Falling back to BOOT classloader",

										EngineXNameUtil
												.getExternalName(metadata
														.getClassName()), "",
										"");

					} else {
						classLoader = HelperLib
								.getCachedClassLoader(classForLoader);
						if (classLoader == null) {
							NotificationUtil
									.sendProblemNotification(
											"Failed to obtain a the class loader for the class "
													+ classForLoader
													+ ". Note: the class specified in the classloadername attribute must already be loaded when the instrumentation point is hit. Falling back to boot classloader",

											EngineXNameUtil
													.getExternalName(metadata
															.getClassName()),
											"", "");

						}
					}
				}

			}
		}
		return classLoader;
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
