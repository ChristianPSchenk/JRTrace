/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

import java.net.URLClassLoader;

public class CachedGroovyScript {
	private final Class<?> scriptClass;
	private final Object scriptObject;
	private URLClassLoader scriptClassLoader;
	public CachedGroovyScript(Class<?> c,Object o, URLClassLoader jarLoader) {
		scriptClass=c; scriptObject=o; scriptClassLoader=jarLoader;
	}
	public ClassLoader getClassLoader() { return scriptClassLoader; }
	public Class<?> getTheClass() { return scriptClass; }
	public Object getObject() { return scriptObject; }
}
