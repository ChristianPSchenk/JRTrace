/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperagent;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class RunJavaListener implements IJRTraceClientListener {

	@Override
	public void messageReceived(String clientSentence) {
		String[] args = clientSentence.split(",");
		if (args.length != 4)
			throw new RuntimeException(
					"RunJava: expects 4 arguments separated with comma");
		String pathToJar = args[0];
		String referenceClassName = args[1];
		String mainClass = args[2];
		String mainMethod = args[3];

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
