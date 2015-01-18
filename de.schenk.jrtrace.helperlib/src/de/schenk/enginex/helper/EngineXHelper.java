/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.schenk.jrtrace.helperlib.JRLog;

public class EngineXHelper {

	public static final Object lock = new Object();

	/**
	 * stores the information for a specific EngineX class keyed on the external
	 * name
	 */
	static Map<String, EngineXClassHolder> classCache = new HashMap<String, EngineXClassHolder>();

	// static Class<?> mainClass;

	// static private Map<String, EngineXMetadata> enginexclasses = new
	// HashMap<String, EngineXMetadata>();

	public static Collection<EngineXMetadata> getEngineXClasses() {
		synchronized (lock) {
			Collection<EngineXClassHolder> values = classCache.values();
			ArrayList<EngineXMetadata> result = new ArrayList<EngineXMetadata>();
			for (EngineXClassHolder value : values) {
				result.add(value.getMetadata());
			}
			return result;
		}
	}

	/**
	 * This method is called by Reflection by the {@link DynamicBinder} class to
	 * obtain the proper jrtrace class to inject
	 * 
	 * @param enginexclass
	 * @param classLoader
	 * @return the object
	 */
	public static Object getEngineXObject(String enginexclass,
			ClassLoader classLoader) {
		EngineXClassHolder o;
		synchronized (lock) {
			o = classCache.get(enginexclass);
		}
		return o.getObject(classLoader);
	}

	/**
	 * 
	 * @param enginexclass
	 *            the name of an enginex class
	 * @param classLoader
	 *            the classloader of the class that triggered the class loading
	 * @return the Class<?> object, if exactly one class can be identified.
	 *         null, if there is more than one classes (from different
	 *         classloaders) or none (e.g. because a named classloader has not
	 *         been loaded yet)
	 */
	public static Class<?> getEngineXClass(String enginexclass,
			ClassLoader classLoader) {
		EngineXClassHolder o;
		synchronized (lock) {
			o = classCache.get(enginexclass);
		}
		if (o == null)
			return null;
		return o.getEngineXClass(classLoader);
	}

	public static void addEngineXClass(EngineXMetadata metadata) {

		List<EngineXMetadata> list = new ArrayList<EngineXMetadata>();
		list.add(metadata);
		addEngineXClass(list);
	}

	public static void addEngineXClass(List<EngineXMetadata> metadatalist) {

		Set<Class<?>> modifiableClasses = clearEngineXTransformationMap();
		long start = System.nanoTime();
		synchronized (lock) {
			for (EngineXMetadata metadata : metadatalist) {
				classCache.put(metadata.getExternalClassName(),
						new EngineXClassHolder(metadata));
			}
		}
		Collection<EngineXMetadata> currentenginex = getEngineXClasses();
		Instrumentation inst = InstrumentationUtil.getInstrumentation();

		Class<?>[] Allclasses = inst.getAllLoadedClasses();

		for (Class<?> c : Allclasses) {

			if (!inst.isModifiableClass(c))
				continue;
			if (c.isInterface())
				continue;
			if (c.isArray())
				continue;
			if (c.isPrimitive())
				continue;
			if (c.isAnnotation())
				continue;
			if (c.isSynthetic())
				continue;

			if (potentialEngineXCandidate(c, currentenginex))
				modifiableClasses.add(c);

		}

		retransformClasses(modifiableClasses);
		long ende = System.nanoTime();
		JRLog.debug(String.format(
				"EngineXHelper.addEngineXClass() took %d ms.",
				(ende - start) / 1000 / 1000));

	}

	/**
	 * Perform a retransformation of the listed classes.
	 * 
	 * Note: the method will not fail and just report an error if the
	 * transformation of a class fails.
	 * 
	 * @param modifiableClasses
	 *            the list of Class<?>es that should be retransformed
	 */
	private static void retransformClasses(Set<Class<?>> modifiableClasses) {

		Instrumentation inst2 = InstrumentationUtil.getInstrumentation();

		if (modifiableClasses.size() > 0) {
			NotificationUtil.sendProgressNotification(String.format(
					"Retransforming %d classes", modifiableClasses.size()), 0,
					modifiableClasses.size());
			JRLog.debug(String.format("Retransforming %d classes.",
					modifiableClasses.size()));
			int i = 0;
			long last = System.currentTimeMillis();
			for (Class<?> m : modifiableClasses) {
				i++;
				if (checkAborted())
					break;
				try {

					inst2.retransformClasses(m);

					// inst.retransformClasses(modifiableClasses
					// .toArray(new Class<?>[modifiableClasses.size()]));
				} catch (Throwable e) {
					// just print an error on the console for now about not
					// being instrumented
					JRLog.error("Error during retransformation of "
							+ m.toString() + ". Instrumentation failed.");
					e.printStackTrace();

				}

				NotificationUtil.sendProgressNotification("", i,
						modifiableClasses.size());

			}

			if (System.currentTimeMillis() > last + 250) {
				NotificationUtil.sendProgressNotification("",
						modifiableClasses.size(), modifiableClasses.size());
				last = System.currentTimeMillis();
			}
		}
	}

	/**
	 * Check if a class might be retransformed for enginex
	 * 
	 * @param c
	 * @param currentenginex
	 * @return true, if any of the currently loaded classes may require
	 *         retranformation to apply enginex rules
	 */
	public static boolean potentialEngineXCandidate(Class<?> c,
			Collection<EngineXMetadata> currentenginex) {

		for (EngineXMetadata md : currentenginex) {
			if (md.mayMatch(c)) {

				return true;
			}
		}
		return false;
	}

	public static void clearEngineX() {

		Set<Class<?>> objects = clearEngineXTransformationMap();

		JRLog.debug(String.format("Clear retransform of %d classes.",
				objects.size()));
		retransformClasses(objects);

	}

	/**
	 * Will clear the list of all registered transfomrations and done
	 * instrumentations (without actually retransforming them)
	 * 
	 * 
	 * @return the list of all classes that have been instrumented by the
	 *         removed jrtrace classes
	 */
	private static Set<Class<?>> clearEngineXTransformationMap() {
		Set<Class<?>> objects = new HashSet<Class<?>>();

		Map<String, Set<ClassLoader>> copyOfTransformed = null;
		synchronized (lock) {

			classCache.clear();
			EngineXClassLoaderRegistry.getInstance().clear();

			copyOfTransformed = new HashMap<String, Set<ClassLoader>>();
			copyOfTransformed.putAll(transformedClassesMap);
			transformedClassesMap.clear();
		}

		for (Entry<String, Set<ClassLoader>> entry : copyOfTransformed
				.entrySet())

		{
			for (ClassLoader cl : entry.getValue()) {
				Class<?> toretransform;
				try {
					toretransform = Class.forName(entry.getKey(), false, cl);
					objects.add(toretransform);
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}

			}
		}
		return objects;
	}

	/**
	 * keep track of the classes that have been transformed.
	 */
	private static Map<String, Set<ClassLoader>> transformedClassesMap = new HashMap<String, Set<ClassLoader>>();

	public static void setTransformed(String className, ClassLoader classLoader) {

		String dotClassName = className.replace('/', '.');
		synchronized (lock) {
			Set<ClassLoader> set = transformedClassesMap.get(dotClassName);
			if (set == null) {
				set = new HashSet<ClassLoader>();
				transformedClassesMap.put(dotClassName, set);
			}
			set.add(classLoader);
		}
	}

	static private boolean abortFlagSet = false;

	static public void abort() {
		abortFlagSet = true;

	}

	/** returns and clears the abort flag */
	static public boolean checkAborted() {
		boolean returnvalue = abortFlagSet;
		abortFlagSet = false;
		return returnvalue;
	}

}
