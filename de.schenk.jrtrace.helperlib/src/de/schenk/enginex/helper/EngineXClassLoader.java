/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

public class EngineXClassLoader extends ClassLoader {

	private EngineXMetadata entry;

	public EngineXClassLoader(ClassLoader classLoader, EngineXMetadata entry) {
		super(classLoader);
		this.entry = entry;
	}

	@Override
	public Class<?> findClass(String className) throws ClassNotFoundException {

		if (className.equals(entry.getExternalClassName())) {
			return defineClass(entry.getExternalClassName(),
					entry.getClassBytes(), 0, entry.getClassBytes().length);
		}
		Class<?> c = EngineXHelper.getEngineXClass(className);
		if (c != null)
			return c;
		throw new ClassNotFoundException(className);

	}

}
