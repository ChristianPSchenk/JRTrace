/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class HelperLib {

  
  
	private static Instrumentation instrumentation;

	
	/**
	 * Utility method to set the value of any object field (especially on  private ones using reflection)
	 * @param target the target object or target Class<?> (for static method calls)
	 * @param name the name of the field
	 * @param value the target value

	 */
	public void setField(Object target,String name,Object value)
	{
	  ReflectionUtil.setField(target,name,value);
	  
	}
	
	private String getCallerClassName() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int i = triggerIndex(stack);
		StackTraceElement triggerElement = stack[i];
		String className = triggerElement.getClassName();
		return className;
	}

	private int triggerIndex(StackTraceElement[] stack) {
		for (int i = 1; i < stack.length; i++) {
			String className = stack[i].getClassName();
			if (!className.startsWith("de.schenk.jrtrace.helperlib")
					&& stack[i].getClass().getClassLoader() != null)
				return i;

		}
		return 0;
	}

	public void traceStack() {
		traceStack(Integer.MAX_VALUE);
	}

	public void traceStack(int depth) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		for (int i = 0; i < trace.length && i < depth; i++) {
			System.out.println(trace[i].toString());
		}
	}

	/*
	 * Utility to get any classloader.
	 * 
	 * The classloader lookup strategy is: (a) o is an classloader -> use it (b)
	 * o is an Object -> get the classloader from its class (c) o is a Class ->
	 * get the classloader from the class (d) if the classloader is still null
	 * (i.e. boot classloader) and o is a string, Instrumentation is used to
	 * determine the class with this name and obtain its classloader.
	 */
	public ClassLoader getClassLoader(Object o) {
		ClassLoader x;
		if (o instanceof ClassLoader) {
			x = (ClassLoader) o;
		} else if (o instanceof Class) {
			x = ((Class<?>) o).getClassLoader();
		} else {
			x = o.getClass().getClassLoader();
		}
		if (x == null && o instanceof String) {
			String className = (String) o;
			x = getCachedClassLoader(className);
		}
		return x;
	}

	public void inspect(Object o) {
		inspect(o, 2, "", "", false, null);
	}

	public void inspect(Object o, int depth) {
		inspect(o, depth, "", "", false, null);
	}

	public void inspect(Object o, int depth, String toStringClasses) {
		inspect(o, depth, toStringClasses, "", false);
	}

	public void inspect(Object o, int depth, String toStringClasses,
			String skipFields) {
		inspect(o, depth, toStringClasses, skipFields, false);
	}

	public void inspect(Object o, int depth, String toStringClasses,
			String skipFields, boolean includeStatics) {
		inspect(o, depth, toStringClasses, skipFields, includeStatics, null);
	}

	/**
	 * A utility class to dump the contents of a variable reflectively,
	 * following references. - fields with value null will be omitted
	 *
	 *
	 * @param o
	 *            the object to dump
	 * @param depth
	 *            recursion depth
	 * @param toStringClasses
	 *            a comma-separated list of simplenames (not qualified) classes
	 *            that should be printed as "toString()". If this is the name of
	 *            an interface then all classes implementing this interface will
	 *            be printed as toString()
	 * @param skipFields
	 *            usedForNames of fields that should not be printed.
	 * @param includeStatics
	 *            include static fields
	 * @param detailFormatters
	 *            string "fieldName=methodname,fieldName2=methodName2" : 
	 *            will invoke a java method on the HelperLib (or its subclasses) and use it to format the field.
	 */
	public void inspect(Object o, int depth, String toStringClasses,
			String skipFields, boolean includeStatics, String detailFormatters) {
		String[] erg = toStringClasses.split(",");
		String[] erg2 = skipFields.split(",");

		HashMap<String, String> formatterMap = new HashMap<String, String>();

		if (detailFormatters != null) {
			String[] formatter = detailFormatters.split(",");
			for (String format : formatter) {
				String[] oneFormat = format.split("=");
				if (oneFormat.length != 2)
					throw new RuntimeException(
							"Invalid detail formatter syntax: Has to be like 'fieldName=methodName,fieldName2=...");
				formatterMap.put(oneFormat[0], oneFormat[1]);
			}
		}
		ClassLoader cl = null;
		/*
		 * performance optimization: only try to determine the classloader if it
		 * is required. This is only the case if a detailformatter is set.
		 */
		if (!formatterMap.isEmpty()) {
			cl = getCachedClassLoader(getCallerClassName());
		}
		System.out.println(new InspectUtil(this)
				.inspect(o, depth, Arrays.asList(erg), Arrays.asList(erg2),
						includeStatics, formatterMap));
	}

	/**
	 * Just intermediate demo code how to install a bundle into a target machine.
	 * @param o
	 */
	private void initPluginUpload(Object o) {
		ClassLoader swt_classloader = (o.getClass().getClassLoader());
		System.out.println(swt_classloader.toString());

		Object configurationField = ReflectionUtil.getPrivateField(
				swt_classloader, null, "configuration");
		ClassLoader equinoxClassLoader = configurationField.getClass()
				.getClassLoader();

		try {
			Object context = ReflectionUtil.getPrivateField(null, Class
					.forName("org.eclipse.core.runtime.adaptor.EclipseStarter",
							true, equinoxClassLoader), "context");
			System.out.println(context);
			ReflectionUtil
					.invokeMethod(
							context,
							"installBundle",
							"file:/c:/temp/plugins/de.schenk.jrtrace.bootstrap.bundle_1.0.0.201407262021.jar");
		} catch (ClassNotFoundException e) {

			e.printStackTrace();
		}

	}

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

	/**
	 * Use instrumentation to get the class loader for a given class.
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

	public static void setInstrumentation(Instrumentation inst) {
		instrumentation = inst;

	}

	public static Instrumentation getInstrumentation() {
		return instrumentation;

	}

}
