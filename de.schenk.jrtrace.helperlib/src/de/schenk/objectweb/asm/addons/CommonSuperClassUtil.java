/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.objectweb.asm.addons;

/**
 * (c) 2014 by Christian Schenk
 **/
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;

public class CommonSuperClassUtil {

	private ClassLoader classLoader;
	private String targetClassName;
	private String superOfTargetClass;
	private Type[] interfacesOfTargetClass;

	/**
	 * Simplified constructor. Slightly less efficient. If you know the
	 * currently instrumented class and its superclass use the other
	 * constructor.
	 * 
	 * @param classLoader
	 */
	public CommonSuperClassUtil(ClassLoader classLoader) {
		this(classLoader, "java/lang/Object", null, new Type[0]);
	}

	/**
	 * 
	 * Note: targetClassName and superClassName increase the efficiency for the
	 * case that both classes are subtypes of the currently being instrumented
	 * class. (slighty, really necessary?)
	 * 
	 * @param classLoader
	 *            the classloader to load the classes in the hierarchy
	 * @param targetClassName
	 *            the currently instrumented class (internal name:
	 *            "java/lang/Object")
	 * @param superClassName
	 *            the superclass of the currently instrumented class (internal
	 *            name: "java/lang/Object")
	 * @param theInterfaceTypes
	 */
	public CommonSuperClassUtil(ClassLoader classLoader,
			String targetClassName, String superClassName,
			Type[] theInterfaceTypes) {

		this.targetClassName = targetClassName;
		this.superOfTargetClass = superClassName;
		this.classLoader = classLoader;
		this.interfacesOfTargetClass = theInterfaceTypes;
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

	public String getCommonSuperClass(String type1, String type2) {

		Class<?> type1Class, type2Class;

		type1Class = tryLoadClass(type1);
		type2Class = tryLoadClass(type2);

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
	 * 
	 * @param type1
	 *            internal name of the object to load
	 * @return null: if the object is not loadable, the class else.
	 */
	private Class<?> tryLoadClass(String type1) {
		Class<?> type1Class;
		type1Class = null;
		try {

			type1Class = Class.forName(type1.replace('/', '.'), false,
					classLoader);

		} catch (Throwable e) {
			// don't do anything, we will handle that later
		}
		return type1Class;
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
			type1 = superOfTargetClass;
		else
			type1 = getSuperAndInterfacesFromBytes(type1).getSuperClass();

		return type1;
	}

	/**
	 * read the classbytes of the type with ASM and extract the supertype from
	 * it.
	 * 
	 * @param theType
	 * @return the sueprtype of theType
	 */
	public SuperClassExtractVisitor getSuperAndInterfacesFromBytes(
			String theType) {
		SuperClassExtractVisitor readSuperTypeVisitor = readClassFromBytes(theType);

		return readSuperTypeVisitor;
	}

	private SuperClassExtractVisitor readClassFromBytes(String theType) {
		SuperClassExtractVisitor readSuperTypeVisitor = null;
		InputStream stream = null;
		try {
			stream = getClassBytesStream(theType);

			ClassReader reader = new ClassReader(stream);

			readSuperTypeVisitor = new SuperClassExtractVisitor(Opcodes.ASM5);

			reader.accept(readSuperTypeVisitor, 0);
			if (!theType.equals(readSuperTypeVisitor.getType())) {
				throw new RuntimeException("That was unexpected");
			}

			stream.close();
		} catch (IOException e) {
			throw new RuntimeException("Failed reading classbytes for "
					+ theType);
		}
		return readSuperTypeVisitor;
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

	public boolean getIsInterface(String internalName) {

		Class<?> theClass = tryLoadClass(internalName);
		if (theClass != null)
			return theClass.isInterface();
		SuperClassExtractVisitor readSuperTypeVisitor = readClassFromBytes(internalName);

		return readSuperTypeVisitor.getIsInterface();
	}

	/**
	 * Checks if the input is the output element or an of the interfaces or
	 * supertypes of output.
	 *
	 * @param output
	 *            an object internal name
	 * @param input
	 *            an object internal name
	 * 
	 * @return true, if yes
	 */
	public boolean isObjectAssignable(Type output, Type input) {
		/* everything can be assigned to an object */
		if (output.equals(Type.getType(Object.class)))

			return true;

		if (input.equals(output))
			return true;
		Class<?> inputClass = tryLoadClass(input.getInternalName());
		Type inputSuperClassType = null;
		Type[] theInputInterfaces = new Type[0];

		if (input.getInternalName().equals(this.targetClassName)) {
			inputSuperClassType = superOfTargetClass == null ? null : Type
					.getType("L" + this.superOfTargetClass + ";");
			theInputInterfaces = this.interfacesOfTargetClass;

		} else if (inputClass != null) {

			if (inputClass.getSuperclass() != null) {
				inputSuperClassType = Type.getType(inputClass.getSuperclass());

			}

			else
				inputSuperClassType = null;

			Class<?>[] ifaces = inputClass.getInterfaces();
			theInputInterfaces = new Type[ifaces.length];
			for (int i = 0; i < ifaces.length; i++) {
				theInputInterfaces[i] = Type.getType(ifaces[i]);
			}
		}

		else {

			SuperClassExtractVisitor superAndInterfaces = getSuperAndInterfacesFromBytes(input
					.getInternalName());
			inputSuperClassType = superAndInterfaces.getSuperClassAsType();
			theInputInterfaces = superAndInterfaces.getInterfacesAsTypes();

		}

		if (inputSuperClassType != null
				&& isObjectAssignable(output, inputSuperClassType))
			return true;
		for (Type oneInputInterface : theInputInterfaces) {
			if (output.equals(oneInputInterface))
				return true;
			if (isObjectAssignable(output, oneInputInterface))
				return true;
		}
		return false;
	}

}
