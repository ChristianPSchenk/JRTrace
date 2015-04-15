package de.schenk.jrtrace.helper;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Map;
import java.util.WeakHashMap;

public class InstrumentationUtil {

	/**
	 *
	 * @param name
	 *            the fully qualified classname
	 * @return
	 */
	static Map<String, ClassLoader> classLoaderCache = new WeakHashMap<String, ClassLoader>();

	static synchronized public ClassLoader getCachedClassLoader(String name) {

		if (classLoaderCache.get(name) != null)
			return classLoaderCache.get(name);
		ClassLoader cl = getClassLoaderByName(name);
		classLoaderCache.put(name, cl);
		return cl;
	}

	public static Instrumentation getInstrumentation() {
		return instrumentation;

	}

	public static void setInstrumentation(Instrumentation inst) {
		instrumentation = inst;

	}

	private static Instrumentation instrumentation;

	/**
	 * Use instrumentation to get all Class<?>es with a specific name
	 *
	 * @param name
	 *            the fully qualified classname
	 * @return an array with all classes that have the specified name
	 *         (potentially more than one, if loaded by different classloaders)
	 */
	static public Class<?>[] getClassesByName(String name) {

		Class<?>[] allLoadedClasses = instrumentation.getAllLoadedClasses();

		ArrayList<Class<?>> matchingClasses = new ArrayList<Class<?>>();
		ClassLoader cl = null;
		for (Class<?> c : allLoadedClasses) {
			if (c.getName().equals(name)) {
				matchingClasses.add(c);
			}
		}

		return matchingClasses.toArray(new Class<?>[0]);
	}

	/**
	 * Use instrumentation to get the class loader for a given class. Note: this
	 * will return the classloader for the first Class<?> that is encountered
	 * that matches the specified name.
	 *
	 * @param name
	 *            the fully qualified classname
	 * @return the classloader
	 */
	static public ClassLoader getClassLoaderByName(String name) {

		Class<?>[] allLoadedClasses = instrumentation.getAllLoadedClasses();

		ClassLoader cl = null;
		for (Class<?> c : allLoadedClasses) {
			if (c.getName().equals(name)) {
				cl = c.getClassLoader();
				break;
			}
		}

		return cl;
	}

}
