/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * An enginexclassloader object is responsible for loading all jrtrace classes
 * that are injected or run in the scope of one specific target system class
 * loader.
 * 
 * For each classloader in the target system there must be exactly one
 * enginexclassloader
 * 
 * @author Christian Schenk
 *
 */
public class EngineXClassLoader extends ClassLoader {

	private Map<String, EngineXMetadata> entries = new HashMap<String, EngineXMetadata>();

	public EngineXClassLoader(ClassLoader classLoader) {
		super(classLoader);

	}

	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {

		EngineXMetadata entry = entries.get(className);
		if (entry != null) {
			return defineClass(entry.getExternalClassName(),
					entry.getClassBytes(), 0, entry.getClassBytes().length);
		}
		Class<?> c = EngineXHelper.getEngineXClass(className, getParent());
		if (c != null)
			return c;
		throw new ClassNotFoundException(className);

	}

	public void addMetadata(EngineXMetadata metadata) {
		this.entries.put(metadata.getExternalClassName(), metadata);
	}

}
