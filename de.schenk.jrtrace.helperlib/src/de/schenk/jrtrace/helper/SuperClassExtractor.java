/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helper;

import java.util.ArrayList;
import java.util.List;

import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;

/*
 * extract the superclass and the super interfaces from a class that is only available as bytecode
 */
public class SuperClassExtractor {

	/* in */
	private ClassLoader classLoader;
	private byte[] classBytes;

	/* out */
	private Class<?> superclass;
	private List<Class<?>> interfaceList = new ArrayList<Class<?>>();

	public SuperClassExtractor(ClassLoader classLoader, byte[] classbytes) {
		this.classLoader = classLoader;
		this.classBytes = classbytes;

	}

	public void analyze() {

		ClassReader reader = new ClassReader(classBytes);
		ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5) {

			@Override
			public void visit(int version, int access, String name,
					String signature, String superName, String[] interfaces) {

				superclass = getClassFromInternalName(superName);

				for (String iface : interfaces) {
					interfaceList.add(getClassFromInternalName(iface));
				}
				return;
			}

			public Class<?> getClassFromInternalName(String superName) {
				Type superType = Type.getObjectType(superName);
				Class<?> aclass = null;
				try {
					aclass = Class.forName(superType.getClassName(), false,
							classLoader);

				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
				return aclass;
			}
		};
		reader.accept(visitor, 0);

	}

	public Class<?> getSuperclass() {
		return superclass;
	}

	public List<Class<?>> getInterfaces() {
		return interfaceList;
	}

}
