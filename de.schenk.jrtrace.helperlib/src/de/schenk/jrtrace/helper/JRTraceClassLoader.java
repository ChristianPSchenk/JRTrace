/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

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
public class JRTraceClassLoader extends ClassLoader {

	private Map<String, JRTraceClassMetadata> entries = new HashMap<String, JRTraceClassMetadata>();
	private int classSetId;

	public JRTraceClassLoader(ClassLoader classLoader, int jrtraceClassSetId) {
		super(classLoader);
		this.classSetId = jrtraceClassSetId;

	}

	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {

		JRTraceClassMetadata entry = entries.get(className);
		if (entry != null) {
			return defineClass(entry.getExternalClassName(),
					entry.getClassBytes(), 0, entry.getClassBytes().length);
		}
		if (className.startsWith("de.schenk.jrtrace.helperlib")) {
			return ClassLoader.getSystemClassLoader().loadClass(className);
		}
		Class<?> c = JRTraceHelper.getEngineXClass(className, classSetId,
				getParent());
		if (c != null)
			return c;
		throw new ClassNotFoundException(className);

	}

	public void addMetadata(JRTraceClassMetadata metadata) {
		this.entries.put(metadata.getExternalClassName(), metadata);
	}

}
