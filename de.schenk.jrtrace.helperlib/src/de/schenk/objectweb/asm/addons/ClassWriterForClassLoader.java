/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.objectweb.asm.addons;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassWriter;
import de.schenk.objectweb.asm.Opcodes;

public class ClassWriterForClassLoader extends ClassWriter {

	private ClassLoader classLoader;
	private String targetClassName;
	private String superClassName;

	public ClassWriterForClassLoader(ClassLoader classLoader,
			String targetClassName, ClassReader classReader, int flags) {
		super(classReader, flags);
		this.targetClassName = targetClassName;
		this.classLoader = classLoader;
	}

	/**
	 * Why is this so complicated: As a rule, all types that extend the object
	 * that is currently instrumented are not yet available as class<?>es. If
	 * the type references it's supertypes ASM will need the commonsuperclass.
	 * 
	 * Corner Case: If only one object cannot be loaded, this is definitely
	 * subtype of the instrumented class. Then we search the common subtype of
	 * the instrumented class and the loadable class. -> done.
	 * 
	 * If both classes cannot be loaded, they are both either the instrumented
	 * type or a subtype. Currently this method returns the instrumented type as
	 * "guess", but this is wrong in a case like:
	 * 
	 * A <-C <-D when class A contains something like:
	 * 
	 * <pre>
	 * 		B = new C();
	 *      if(...) B=new D();
	 *      B.methodThatExistsOnCAndDbutnotonA();
	 * </pre>
	 * 
	 * (because in this case the common supertype is C and here A is returned.
	 * 
	 * Possible options to improve: option a: load resource and parse
	 * super/subtype hierarchy. option b: skip transformation, and schedule for
	 * retransformation and hoep the subtypes are available... :-(
	 */
	@Override
	protected String getCommonSuperClass(String type1, String type2) {

		Class<?> type1Class, type2Class;
		type1Class = null;
		type2Class = null;

		try {

			type1Class = Class.forName(type1.replace('/', '.'), false,
					classLoader);

		} catch (Throwable e) {
			// don't do anything, we will handle that later
		}
		try {
			type2Class = Class.forName(type2.replace('/', '.'), false,
					classLoader);
		} catch (Throwable e) {
			// don't do anything we will handle that later.
		}

		if (type1Class == null || type2Class == null) {
			if (type2Class == null) {
				String swap = type1;
				type1 = type2;
				type2 = swap;
				Class<?> swapClass = type1Class;
				type1Class = type2Class;
				type2Class = swapClass;
			}

			if (type2Class != null) { /*
									 * case: only one of the classes cannot be
									 * loaded
									 */
				if (type2Class.isInterface()) {
					return "java/lang/Object";
				}
				type1 = getSuperClassName(type1);
				return getCommonSuperClass(type1, type2);

			}
			/* case: both types cannot be loaded */
			Set<String> allSuperTypesOfType1 = getAllSuperClassNames(type1);
			while (!allSuperTypesOfType1.contains(type2)) {
				type2 = getSuperClassName(type2);
			}
			return type2;

		} else {

			if (type1Class.isAssignableFrom(type2Class)) {
				return type1;
			}
			if (type2Class.isAssignableFrom(type1Class)) {
				return type2;
			}
			if (type1Class.isInterface() || type2Class.isInterface()) {
				return "java/lang/Object";
			} else {
				do {
					type1Class = type1Class.getSuperclass();
				} while (!type1Class.isAssignableFrom(type2Class));
				return type1Class.getName().replace('.', '/');
			}
		}
	}

	/**
	 * recursively walks up the supertype hierarchy stopping only at the
	 * instrumented type and returns everything in a hashset
	 * 
	 * @param type1
	 * @return a set containing the class itself and all the supertypes stopping
	 *         and including the currently instrumented type
	 */
	private Set<String> getAllSuperClassNames(String type1) {
		Set<String> set = new HashSet<String>();
		while (true) {
			set.add(type1);
			if (targetClassName.equals(type1))
				return set;
			type1 = getSuperClassName(type1);
		}

	}

	private String getSuperClassName(String type1) {
		if (type1.equals(targetClassName))
			type1 = superClassName;
		else
			type1 = readSuperType(type1);
		return type1;
	}

	/**
	 * read the classbytes of the type with ASM and extract the supertype from
	 * it.
	 * 
	 * @param theType
	 * @return the sueprtype of theType
	 */
	private String readSuperType(String theType) {
		SuperClassExtractVisitor readSuperTypeVisitor = null;
		InputStream stream = null;
		try {
			stream = getClassBytesStream(theType);

			ClassReader reader = new ClassReader(stream);

			readSuperTypeVisitor = new SuperClassExtractVisitor(Opcodes.ASM5,
					theType);
			reader.accept(readSuperTypeVisitor, 0);

			stream.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed reading classbyes for "
					+ theType);
		}

		return readSuperTypeVisitor.getSuperClass();
	}

	private InputStream getClassBytesStream(String theClassName) {
		InputStream stream;
		if (classLoader != null) {
			stream = classLoader
					.getResourceAsStream(convertInteralNameToResourceName(theClassName));
		} else {
			stream = ClassLoader
					.getSystemResourceAsStream(convertInteralNameToResourceName(theClassName));

		}
		return stream;
	}

	private String convertInteralNameToResourceName(String type1) {
		String result = type1;

		return result + ".class";
	}

	public void setSuperInformation(String superName) {
		this.superClassName = superName;

	}

}
