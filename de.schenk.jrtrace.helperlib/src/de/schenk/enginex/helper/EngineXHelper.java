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

import de.schenk.jrtrace.helperlib.HelperLib;
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
	 * @return the Class<?> object, if exactly one class can be identified.
	 *         null, if there is more than one classes (from different
	 *         classloaders) or none (e.g. because a named classloader has not
	 *         been loaded yet)
	 */
	public static Class<?> getEngineXClass(String enginexclass) {
		EngineXClassHolder o;
		synchronized (lock) {
			o = classCache.get(enginexclass);
		}
		if (o == null)
			return null;
		return o.getEngineXClass();
	}

	public static void addEngineXClass(EngineXMetadata metadata) {
		List<EngineXMetadata> list = new ArrayList<EngineXMetadata>();
		list.add(metadata);
		addEngineXClass(list);
	}

	public static void addEngineXClass(List<EngineXMetadata> metadatalist) {
		long start = System.nanoTime();
		synchronized (lock) {
			for (EngineXMetadata metadata : metadatalist) {
				classCache.put(metadata.getExternalClassName(),
						new EngineXClassHolder(metadata));
			}
		}
		Collection<EngineXMetadata> currentenginex = getEngineXClasses();
		Instrumentation inst = HelperLib.getInstrumentation();

		Class<?>[] Allclasses = inst.getAllLoadedClasses();

		List<Class<?>> modifiableClasses = new ArrayList<Class<?>>();
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

		if (modifiableClasses.size() > 0) {
			JRLog.debug(String.format("Retransforming %d classes.",
					modifiableClasses.size()));
			for (Class<?> m : modifiableClasses) {
				try {
					JRLog.debug("Retransform on: " + m.toString());
					inst.retransformClasses(m);

					// inst.retransformClasses(modifiableClasses
					// .toArray(new Class<?>[modifiableClasses.size()]));
				} catch (Throwable e) {
					// just print an error on the console for now about not
					// being instrumented
					JRLog.error("Error during retransformation of "
							+ m.toString() + ". Instrumentation failed.");
					e.printStackTrace();

				}

			}

		}
		long ende = System.nanoTime();
		JRLog.debug(String.format(
				"EngineXHelper.addEngineXClass() took %d ms.",
				(ende - start) / 1000 / 1000));

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

		List<Class<?>> objects = new ArrayList<Class<?>>();
		Map<String, Set<ClassLoader>> copyOfTransformed = null;
		synchronized (lock) {

			classCache.clear();

			copyOfTransformed = new HashMap<String, Set<ClassLoader>>();
			copyOfTransformed.putAll(transformedClassesMap);
			transformedClassesMap.clear();
		}

		JRLog.debug(String.format("Clear retransform of %d classes.",
				copyOfTransformed.size()));
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

		if (objects.size() > 0) {
			try {

				HelperLib.getInstrumentation().retransformClasses(
						objects.toArray(new Class<?>[objects.size()]));

			} catch (Throwable e) {
				e.printStackTrace();

			}
			JRLog.debug("JRTrace transformations cleared (disabled)");
		}

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
}
