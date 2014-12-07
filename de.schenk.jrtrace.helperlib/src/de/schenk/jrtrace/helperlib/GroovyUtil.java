/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class GroovyUtil implements IGroovy {

	static public void clearScriptCache() {
		cache.clear();
	}

	String projectRootPath;

	private String groovypath;

	/**
	 * 
	 * @param rootPath
	 *            the rootpath to resolve groovy script locations
	 */
	public GroovyUtil(String groovyPath, String rootPath) {
		this.groovypath = groovyPath;
		if (groovypath == null) {
			groovypath = System
					.getProperty(HelperLibConstants.DE_SCHENK_JRTRACE_GROOVYJAR);
		}
		projectRootPath = rootPath;
	}

	public GroovyUtil() {
		this(null, null);
	}

	static GroovyScriptCache cache = new GroovyScriptCache();

	static HashMap<Integer, String> parameterNames = new HashMap<Integer, String>();

	public Object evaluateFile(String groovyFile, ClassLoader classLoader,
			Object... parameters) {
		String groovyScriptName;
		if (projectRootPath == null) {
			groovyScriptName = groovyFile;
		} else {
			groovyScriptName = projectRootPath + "/" + groovyFile;
		}

		File script = new File(groovyScriptName);
		StringBuffer expression = new StringBuffer();
		try {
			BufferedReader scriptReader = new BufferedReader(new FileReader(
					script));
			while (true) {
				String nextLine = scriptReader.readLine();
				if (nextLine == null)
					break;
				expression.append(nextLine);
				expression.append("\n");

			}
			scriptReader.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Error accessing the script file "
					+ script.getAbsolutePath() + ".");
		} catch (IOException e) {
			throw new RuntimeException("Error accessing the script file "
					+ script.getAbsolutePath() + ".");
		}
		return evaluate(expression.toString(), classLoader, parameters);

	}

	public Object evaluate(String expression, ClassLoader classLoader,
			Object... parameters) {

		try {

			CachedGroovyScript theScript = cache.getScript(expression,
					classLoader);

			if (theScript == null) {
				theScript = createScript(expression, classLoader);
				cache.putScript(expression, classLoader, theScript);
			}

			Class<?> bindingClass = Class.forName("groovy.lang.Binding", true,
					theScript.getClassLoader());
			Object bindingObject = bindingClass.newInstance();
			Method setVariableMethod = bindingClass.getMethod("setVariable",
					String.class, Object.class);
			for (int i = 0; i < parameters.length; i++) {
				String parameterName = parameterNames.get(i);
				if (parameterName == null) {
					parameterName = String.format("arg%d", i);
					parameterNames.put(i, parameterName);
				}
				setVariableMethod.invoke(bindingObject, parameterName,
						parameters[i]);
			}

			Method runMethod = theScript.getTheClass().getMethod("run");
			Method setBindingMethod = theScript.getObject().getClass()
					.getMethod("setBinding", bindingClass);
			setBindingMethod.invoke(theScript.getObject(), bindingObject);
			Object value = runMethod.invoke(theScript.getObject());

			return value;

		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private CachedGroovyScript createScript(String expression,
			ClassLoader classLoader) throws ClassNotFoundException,
			NoSuchMethodException, InstantiationException,
			IllegalAccessException, InvocationTargetException {

		URL u;
		try {

			if (groovypath == null) {
				throw new RuntimeException("System Property "
						+ HelperLibConstants.DE_SCHENK_JRTRACE_GROOVYJAR
						+ " not set. Cannot access Groovy jar", null);
			}
			File fu = new File(groovypath);
			u = fu.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new RuntimeException("invalid url", e);
		}
		URLClassLoader jarLoader = URLClassLoader.newInstance(new URL[] { u },
				classLoader);
		CachedGroovyScript theScript;

		Class<?> gclClass = Class.forName("groovy.lang.GroovyClassLoader",
				true, jarLoader);
		Constructor<?> gclConstructor = gclClass
				.getConstructor(ClassLoader.class);
		Object gclObject = gclConstructor.newInstance(jarLoader);

		Method parseClassMethod = gclClass
				.getMethod("parseClass", String.class);
		Class<?> myScriptClass = (Class<?>) parseClassMethod.invoke(gclObject,
				expression);

		Object myScriptObject = myScriptClass.newInstance();
		theScript = new CachedGroovyScript(myScriptClass, myScriptObject,
				jarLoader);

		return theScript;
	}

}
