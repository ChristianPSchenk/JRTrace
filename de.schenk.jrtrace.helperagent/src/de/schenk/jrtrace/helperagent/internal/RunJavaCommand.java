/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import de.schenk.jrtrace.helper.InstrumentationUtil;
import de.schenk.jrtrace.helper.JRTraceHelper;

public class RunJavaCommand {

	/**
	 * 
	 * @param referenceClassName
	 *            the fully qualified name of a class that shall be used to
	 *            identify the classloader. (only applicable for
	 *            XClassLoaderPolicy.TARGET)
	 * 
	 * @param mainClass
	 * @param mainMethod
	 * @param arguments
	 *            the parameter array.
	 * 
	 */
	public void runJava(String referenceClassName, String mainClass,
			String mainMethod, final Object[] arguments) {

		ClassLoader classLoader = InstrumentationUtil
				.getCachedClassLoader(referenceClassName);

		Class<?> gclClass;
		try {
			gclClass = JRTraceHelper.getEngineXClass(mainClass,
					JRTraceHelper.getCurrentClassSetId(), classLoader);
			if (gclClass == null) {
				throw new RuntimeException(String.format(
						"Unable to obtain class %s for execution.", mainClass));
			}

			Method method = findMatchingMethod(mainMethod, arguments, gclClass);

			if (method == null) {
				System.err.println("Method " + mainMethod + " not found.");
				throw new RuntimeException("Method " + mainMethod
						+ " not found.");
			}
			Object targetObject;
			if (!Modifier.isStatic(method.getModifiers())) {
				targetObject = JRTraceHelper.getEngineXObject(mainClass,
						JRTraceHelper.getCurrentClassSetId(), classLoader);

				if (targetObject == null) {

					try {
						targetObject = gclClass.newInstance();
					} catch (InstantiationException | IllegalAccessException e) {
						String msg = "No JRTrace Object could be created to executed the method "
								+ method.getName()
								+ " on the JRTrace class "
								+ mainClass;
						System.err.println(msg);

						throw new RuntimeException(msg, e);
					}

				}

			} else {
				targetObject = null;
			}
			final Method themethod = method;
			final Object theTargetObject = targetObject;
			Thread runnerThread = new Thread("runJRTRaceCode") {
				@Override
				public void run() {
					try {
						themethod.setAccessible(true);

						themethod.invoke(theTargetObject, arguments);

					} catch (Throwable e) {
						// currently no feedback to jrtrace UI. Just report
						// anything on the console.
						e.printStackTrace();
					}

				}
			};
			runnerThread.start();

		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * This finds a method on the class that can be invoked with the provided
	 * list of arguments. Note that this might be ambiguous, if the parameters
	 * are not specific enough (e.g. null values)
	 * 
	 * @param methodNames
	 *            the method name
	 * @param arguments
	 *            the arguments that are supplied
	 * @param clazz
	 *            the class which will be searched for the method
	 * @return the first method that matches name and parameters. Note: the
	 *         class might have multiple methods that match.
	 */
	private Method findMatchingMethod(String methodNames,
			final Object[] arguments, Class<?> clazz) {
		Method[] declaredMethods = clazz.getDeclaredMethods();
		Method method = null;
		for (Method m : declaredMethods) {
			if (m.getName().equals(methodNames)) {
				Class<?>[] parameterTypes = m.getParameterTypes();
				if (parameterTypes.length == arguments.length) {
					boolean match = true;
					for (int i = 0; i < arguments.length; i++) {
						if (arguments[i] == null)
							continue;

						Class<?> methodParameterType = parameterTypes[i];
						Class<?> providedParameterType = arguments[i]
								.getClass();
						if (!methodParameterType
								.isAssignableFrom(providedParameterType)) {
							match = false;
							break;
						}

					}
					if (match) {

						method = m;
						break;
					}
				}
			}
		}
		return method;
	}

}
