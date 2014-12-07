package de.schenk.objectweb.asm.addons;

import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassWriter;

public class ClassWriterForClassLoader extends ClassWriter {

	private ClassLoader classLoader;

	public ClassWriterForClassLoader(ClassLoader classLoader, ClassReader classReader,
			int flags) {
		super(classReader, flags);
		this.classLoader = classLoader;
	}

	@Override
	protected String getCommonSuperClass(String type1, String type2) {

		Class<?> c, d;

		try {
			c = Class.forName(type1.replace('/', '.'), false, classLoader);
			d = Class.forName(type2.replace('/', '.'), false, classLoader);
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		if (c.isAssignableFrom(d)) {
			return type1;
		}
		if (d.isAssignableFrom(c)) {
			return type2;
		}
		if (c.isInterface() || d.isInterface()) {
			return "java/lang/Object";
		} else {
			do {
				c = c.getSuperclass();
			} while (!c.isAssignableFrom(d));
			return c.getName().replace('.', '/');
		}
	}

}
