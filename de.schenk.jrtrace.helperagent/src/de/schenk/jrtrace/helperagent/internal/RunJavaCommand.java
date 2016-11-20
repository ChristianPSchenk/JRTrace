/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;

import de.schenk.jrtrace.helper.InstrumentationUtil;
import de.schenk.jrtrace.helper.JRTraceHelper;
import de.schenk.jrtrace.helperlib.ReflectionUtil;

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

			Collection<Method> methods = ReflectionUtil.findMatchingMethod(
					mainMethod, arguments, gclClass);

			if (methods.size() != 1) {
				StringBuilder msgb = new StringBuilder();
				msgb.append(String
						.format("The method "
								+ mainMethod
								+ " and the provided parameters do not match exactly one method from the targetclass:"));
				if (methods.size() == 0) {
					msgb.append("In this case no method matched.");
				} else {
					for (Method x : methods) {
						msgb.append("Matching Method:" + x.toGenericString());
					}
				}

				String msg = msgb.toString();
				System.err.println(msg);
				throw new RuntimeException(msg);
			}
			Method method = methods.iterator().next();
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
								+ mainClass
								+ ". Note that JRTrace objects on which a method needs to be excecuted require a public no-argument constructor. Note that non-static inner classes don't have a no-argument constructor.";
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

}
