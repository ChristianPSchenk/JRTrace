/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
	 */
	public void runJava(String referenceClassName, String mainClass,
			String mainMethod) {

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
			Method method = gclClass.getMethod(mainMethod);
			method.invoke(null);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

	}

}
