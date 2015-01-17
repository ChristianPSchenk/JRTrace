package de.schenk.enginex.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This registry is not multi thread safe. It is intended to be used from the
 * {@link EngineXClassHolder} only in a synchronized manner.
 * 
 * @author Christian Schenk
 *
 */
public class EngineXClassLoaderRegistry {

	
	
	private Map<ClassLoader,EngineXClassLoader> classLoaderMap=new HashMap<ClassLoader,EngineXClassLoader>();
	private static EngineXClassLoaderRegistry instance;

	private EngineXClassLoaderRegistry() {
		// private constructor
	}

	public static EngineXClassLoaderRegistry getInstance() {
		if (instance == null) {
			instance = new EngineXClassLoaderRegistry();
		}
		return instance;
	}

	public EngineXClassLoader getClassLoader(ClassLoader classLoader) {
		
		EngineXClassLoader enginexclassloader =classLoaderMap.get(classLoader);
		if(enginexclassloader==null)
		{
			
		
		enginexclassloader= new EngineXClassLoader(
				classLoader);
		classLoaderMap.put(classLoader, enginexclassloader);
		}
		return enginexclassloader;
	}

	public void clear() {
		classLoaderMap.clear();
		
	}

}
