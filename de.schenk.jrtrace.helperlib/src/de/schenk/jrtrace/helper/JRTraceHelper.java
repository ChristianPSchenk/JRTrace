/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
import de.schenk.objectweb.asm.addons.ClassByteUtil;

public class JRTraceHelper {

	public static final Object lock = new Object();

	/**
	 * keep track of the classes that have been transformed.
	 */
	private static Map<String, Set<ClassLoader>> transformedClassesMap = new HashMap<String, Set<ClassLoader>>();

	/**
	 * stores the information for a specific EngineX class keyed on the external
	 * name for the current (currentCacheId) set of jrtrace classes
	 */
	static JRTraceClassStore classStore = new JRTraceClassStore();

	/**
	 * Whenever a set of JRTrace classes is installed, this set forms a
	 * consistent set of classes that work together and are stored in the
	 * classStore. The currentCacheId is identifies one such set. The
	 * instrumentation includes the current id into the transformed classes and
	 * it will be used for lookup during call site binding (see DynamicBinder).
	 * This id gives the engine the possibility to distinguish between
	 * instrumented classes from an OLD set of JRTrace classes (and bind to the
	 * old classes which are stored in previousClassCache) and the new
	 * set/current set of classes which will bind to the NEW set of JRTrace
	 * classes. This mechanism makes it possible to have only ONE
	 * retransformation run to replace a set of JRTrace classes by a new set.
	 * (The alternative would be: first remove the old instrumentation
	 * completely. Then reinstall the new set of classes. Basically this would
	 * double the transformation time during reinstallation)
	 * 
	 * A note on incremental behaviour: assume there are two JRTrace classes:
	 * one injects into many target classes, a second into only a few. If the
	 * second is changed, it would be nice to retransform only the classes that
	 * are affected by the second JRTrace class. This will be difficult to
	 * implement , because structural changes (adding methods/removing methods)
	 * to JRTrace classes will require the use of a new classloader. However
	 * unchanged JRTrace classes that use the same classloader must keep the old
	 * classloader (because the classes are not retransformed, thus the old
	 * classloader needs to be used). Thus for each target classloader a new
	 * classloader has to be created for the new class that delegates to the old
	 * ones. Potentially the number of classloaders accumulates after some
	 * cycles. So basically this would complicate the whole runtime behaviour a
	 * lot and won't be implemented unless absolutely necessary.
	 * 
	 * 
	 * 
	 */
	static int currentCacheId = 0;

	public static Collection<JRTraceClassMetadata> getEngineXClasses() {
		synchronized (lock) {
			return classStore.getAllForId(currentCacheId);

		}

	}

	/**
	 * This method is called by Reflection by the {@link DynamicBinder} class to
	 * obtain the proper jrtrace class to inject
	 * 
	 * @param enginexclass
	 * @param cacheId
	 *            the id of the current set of jrtrace classes.
	 * @param classLoader
	 * @return the object
	 */
	public static Object getEngineXObject(String enginexclass, int cacheId,
			ClassLoader classLoader) {
		JRTraceClassAndObjectCache o = getJRTraceClassAndObjectCache(
				enginexclass, cacheId);
		return o.getObject(classLoader);
	}

	/**
	 * creates a new enginex object using the no-arg-constructor.
	 * 
	 * @param enginexclass
	 *            the fully qualified name of the JRTrace class
	 * @param cacheId
	 *            the id of the current set of JRTrace classes
	 * @param classLoader
	 *            the classloader to use (required for
	 *            classloaderPolicy=XClassloaderPolicy.TARGET)
	 * @return a newly created object
	 */
	public static Object createEngineXObject(String enginexclass, int cacheId,
			ClassLoader classLoader) {
		Class<?> clazz = getEngineXClass(enginexclass, cacheId, classLoader);
		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(
					String.format(
							"Failed to create new instance of JRTrace class %s for classLoader %s.",
							enginexclass, (classLoader == null ? "null"
									: classLoader.toString())));
		}
	}

	/**
	 * 
	 * @param enginexclass
	 *            the name of an enginex class
	 * @param cacheId
	 *            the id of the set of JRTrace classes that this refers to.
	 * @param classLoader
	 *            the classloader of the class that triggered the class loading
	 * @return the Class<?> object, if exactly one class can be identified.
	 *         null, if there is more than one classes (from different
	 *         classloaders) or none (e.g. because a named classloader has not
	 *         been loaded yet)
	 */
	public static Class<?> getEngineXClass(String enginexclass, int cacheId,
			ClassLoader classLoader) {
		JRTraceClassAndObjectCache o;
		o = getJRTraceClassAndObjectCache(enginexclass, cacheId);
		if (o == null)
			return null;
		return o.getEngineXClass(classLoader);
	}

	private static JRTraceClassAndObjectCache getJRTraceClassAndObjectCache(
			String enginexclass, int cacheId) {

		synchronized (lock) {
			return classStore.get(cacheId, enginexclass);

		}

	}

	/**
	 * 
	 * @param className
	 *            a fully qualified classname
	 * @return true, if the fully qualified name is the name of one of the
	 *         installed JRTrace script classes
	 */
	public static boolean isJRTraceClass(String className) {
		synchronized (lock) {
			return null != classStore.get(currentCacheId, className);
		}

	}

	public static void addEngineXClass(List<JRTraceClassMetadata> metadatalist) {
		if (!validateClasses(metadatalist))
			return;
		clearAbortFlag();
		Set<Class<?>> modifiableClasses = null;
		long start = System.nanoTime();
		synchronized (lock) {

			modifiableClasses = clearEngineXTransformationMap();

			for (JRTraceClassMetadata metadata : metadatalist) {
				classStore
						.put(currentCacheId, metadata.getExternalClassName(),
								new JRTraceClassAndObjectCache(metadata,
										currentCacheId));
			}
		}
		Collection<JRTraceClassMetadata> currentenginex = getEngineXClasses();
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
			if (BuiltInExcludes.isExcludedClassName(c.getName(), null))
				continue;
			if (potentialEngineXCandidate(c, currentenginex))
				modifiableClasses.add(c);

		}
		
		transformJavaLangDouble();

		retransformClasses(modifiableClasses);

		long ende = System.nanoTime();
		JRLog.debug(String.format(
				"JRTraceHelper.addEngineXClass() took %d ms.",
				(ende - start) / 1000 / 1000));

	}

	private static void transformJavaLangDouble() {
		try {
			InstrumentationUtil.getInstrumentation().retransformClasses(LambdaMetafactory.class);
		} catch (UnmodifiableClassException e) {
			
			JRLog.error("transforming java.lang.Object to include the DynamicBinder delegate methods failed.");
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * Perform some pre validations to avoid errors later on.
	 * 
	 * @param metadatalist
	 * @return false if an error was encountered in the jrtrace class metadata
	 */
	private static boolean validateClasses(

	List<JRTraceClassMetadata> metadatalist) {
		boolean result = true;
		for (JRTraceClassMetadata cmd : metadatalist) {
			if (cmd.getUseRegEx()) {
				List<String> patterns = cmd.getClasses();
				for (String pattern : patterns) {
					if (!isPatternValid(cmd, null, pattern))
						result = false;
				}
				List<JRTraceMethodMetadata> methods = cmd.getMethods();
				for (JRTraceMethodMetadata method : methods) {
					for (String tmn : method.getTargetMethodNames())
						if (!isPatternValid(cmd, method, tmn))
							result = false;
					if (!isPatternValid(cmd, method,
							method.getInvokedMethodClass()))
						result = false;
					if (!isPatternValid(cmd, method,
							method.getInvokedMethodName()))
						result = false;
					if (!isPatternValid(cmd, method,
							method.getFieldAccessClass()))
						result = false;
					if (!isPatternValid(cmd, method,
							method.getFieldAccessName()))
						result = false;

				}
			}
		}
		return result;
	}

	public static boolean isPatternValid(

	JRTraceClassMetadata cmd, JRTraceMethodMetadata method, String pattern) {
		try {
			Pattern p = Pattern.compile(pattern);
		} catch (PatternSyntaxException e) {

			String msg = String.format(
					"Syntaxerror in regular expression %s: %s", pattern,
					e.getMessage());
			NotificationUtil.sendProblemNotification(msg,
					JRTraceNameUtil.getExternalName(cmd.getClassName()),
					method == null ? null : method.getMethodName(),
					method == null ? null : method.getDescriptor());
			return false;
		}
		return true;
	}

	private static void clearAbortFlag() {
		abortFlagSet = false;
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

		Instrumentation instrumentation = InstrumentationUtil
				.getInstrumentation();

		if (modifiableClasses.size() > 0) {
			NotificationUtil.sendProgressNotification(String.format(
					"Retransforming %d classes", modifiableClasses.size()), 0,
					modifiableClasses.size());
			JRLog.debug(String.format("Retransforming %d classes.",
					modifiableClasses.size()));
			int i = 0;
			long last = System.currentTimeMillis();
			Set<Class<?>> remaining = new HashSet<Class<?>>();
			remaining.addAll(modifiableClasses);
			for (Class<?> m : modifiableClasses) {
				i++;
				if (checkAborted()) {
					synchronized (lock) {
						for (Class<?> remainingClass : remaining) {
							setTransformed(remainingClass);

						}
					}
					break;
				}
				try {

					instrumentation.retransformClasses(m);
					remaining.remove(m);

					/*
					 * just a try to use redefine instead of retransform / but
					 * doesn't improve the behaviour byte[] classBytes =
					 * ClassByteUtil.getBytes(m); if (classBytes == null )
					 * 
					 * { JRLog.debug(
					 * "It was not possible to get the class byte[] for class "
					 * + m.getName() +
					 * ". Only retransforming. Existing class instances will not be instrumented"
					 * );
					 * 
					 * } else { ClassDefinition def = new ClassDefinition(m,
					 * classBytes); instrumentation.redefineClasses(def); }
					 */

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

	private static void setTransformed(Class<?> remainingClass) {
		setTransformed(remainingClass.getName(),
				remainingClass.getClassLoader());

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
			Collection<JRTraceClassMetadata> currentenginex) {

		for (JRTraceClassMetadata md : currentenginex) {
			if (md.mayMatch(c)) {

				return true;
			}
		}
		return false;
	}

	public static void clearEngineX() {
		JRLog.debug(String.format("Clearing JRTrace classes"));

		addEngineXClass(new ArrayList<JRTraceClassMetadata>());
	}

	/**
	 * Will clear the list of all registered transfomrations and done
	 * instrumentations (without actually retransforming them)
	 * 
	 * Clears the current classStore and provides a new id for the classset to
	 * be installed.
	 * 
	 * 
	 * @return the list of all classes that have been instrumented by the
	 *         removed jrtrace classes
	 */
	private static Set<Class<?>> clearEngineXTransformationMap() {
		Set<Class<?>> objects = new HashSet<Class<?>>();

		Map<String, Set<ClassLoader>> copyOfTransformed = null;
		synchronized (lock) {

			JRTraceClassLoaderRegistry.getInstance(currentCacheId).clear();
			currentCacheId++;
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

	public static int getCurrentClassSetId() {
		synchronized (lock) {
			return currentCacheId;
		}
	}

	public static InjectStatus analyzeInjectionStatus(String className) {
		Collection<JRTraceClassMetadata> classes = classStore
				.getAllForId(getCurrentClassSetId());
		InjectStatus status = new InjectStatus(StatusEntityType.JRTRACE_SESSION);
		if (classes.size() == 0) {
			status.setInjected(StatusState.DOESNT_INJECT);
			status.setMessage(InjectStatus.MSG_NO_JRTRACE_CLASSES);
			return status;
		}
		Instrumentation instr = InstrumentationUtil.getInstrumentation();
		Class<?>[] loadedClasses = instr.getAllLoadedClasses();
		HashSet<Class<?>> testClasses = new HashSet<Class<?>>();
		for (Class<?> clazz : loadedClasses) {
			if (className.equals(clazz.getName())) {
				testClasses.add(clazz);
			}
		}
		if (testClasses.size() == 0) {
			status.setInjected(StatusState.DOESNT_INJECT);
			status.setMessage(className + " : "
					+ InjectStatus.MSG_CLASS_NOT_LOADED);
			return status;
		}

		StatusState sumStatus = StatusState.DOESNT_INJECT;
		for (Class<?> testClass : testClasses) {
			InjectStatus childStatus = new InjectStatus(
					StatusEntityType.JRTRACE_CHECKED_CLASS);
			childStatus.setEntityName(testClass.getName()
					+ " ["
					+ ((testClass.getClassLoader() == null ? "BootclassLoader"
							: testClass.getClassLoader().toString()) + "]"));
			status.addChildStatus(childStatus);

			JRTraceOneClassTransformer transformer = new JRTraceOneClassTransformer(
					testClass.getClassLoader(), testClass.getName(), testClass,
					ClassByteUtil.getBytes(testClass));
			transformer.setStatus(childStatus);
			transformer.doTransform();
			if (childStatus.getInjectionState() == StatusState.INJECTS) {
				sumStatus = StatusState.INJECTS;
			}

		}
		status.setInjected(sumStatus);
		return status;
	}

}
