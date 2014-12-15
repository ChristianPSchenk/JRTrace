/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent.internal;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import de.schenk.jrtrace.helperlib.HelperLib;

public class RunJavaCommand {

	public void runJava(String pathToJar, String referenceClassName,
			String mainClass, String mainMethod) {
		URL u;
		try {

			File fu = new File(pathToJar);
			u = fu.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("invalid url", e);
		}
		ClassLoader classLoader = HelperLib
				.getCachedClassLoader(referenceClassName);
		URLClassLoader jarLoader = URLClassLoader.newInstance(new URL[] { u },
				classLoader);

		Class<?> gclClass;
		try {
			gclClass = Class.forName(mainClass, true, jarLoader);
			Method method = gclClass.getMethod(mainMethod);
			method.invoke(null);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Exception while invoking " + mainClass
					+ "." + mainMethod + ".", e);

		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Exception while invoking " + mainClass
					+ "." + mainMethod + ".", e);
		} catch (SecurityException e) {
			throw new RuntimeException("Exception while invoking " + mainClass
					+ "." + mainMethod + ".", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Exception while invoking " + mainClass
					+ "." + mainMethod + ".", e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Exception while invoking " + mainClass
					+ "." + mainMethod + ".", e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException("Exception while invoking " + mainClass
					+ "." + mainMethod + ".", e);
		}

	}

}
