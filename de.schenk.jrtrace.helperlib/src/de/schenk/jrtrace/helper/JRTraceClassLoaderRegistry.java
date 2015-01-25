package de.schenk.jrtrace.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This registry is not multi thread safe. It is intended to be used from the
 * {@link JRTraceClassAndObjectCache} only in a synchronized manner.
 * 
 * @author Christian Schenk
 *
 */
public class JRTraceClassLoaderRegistry {

	
	
	private Map<ClassLoader,JRTraceClassLoader> classLoaderMap=new HashMap<ClassLoader,JRTraceClassLoader>();
	private static JRTraceClassLoaderRegistry instance;

	private JRTraceClassLoaderRegistry() {
		// private constructor
	}

	public static JRTraceClassLoaderRegistry getInstance() {
		if (instance == null) {
			instance = new JRTraceClassLoaderRegistry();
		}
		return instance;
	}

	public JRTraceClassLoader getClassLoader(ClassLoader classLoader) {
		
		JRTraceClassLoader enginexclassloader =classLoaderMap.get(classLoader);
		if(enginexclassloader==null)
		{
			
		
		enginexclassloader= new JRTraceClassLoader(
				classLoader);
		classLoaderMap.put(classLoader, enginexclassloader);
		}
		return enginexclassloader;
	}

	public void clear() {
		classLoaderMap.clear();
		
	}

}
