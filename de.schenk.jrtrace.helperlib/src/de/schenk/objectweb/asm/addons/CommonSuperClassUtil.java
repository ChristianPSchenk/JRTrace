/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.objectweb.asm.addons;

/**
 * (c) 2014 by Christian Schenk
 **/
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.schenk.jrtrace.helper.InstrumentationUtil;
import de.schenk.jrtrace.helperlib.JRLog;
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
	 * retransformation and hope the subtypes are available... :-(
	 */

	public String getCommonSuperClass(String type1, String type2) {

		Class<?> type1Class, type2Class;

		type1Class = tryGetLoadedClass(type1);
		type2Class = tryGetLoadedClass(type2);

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
	 * Check if a particular Type is already loaded by the classloader
	 * 
	 * @param type1 the Type name a.b.C of the class 
	 * @return
	 */
	private boolean  isAlreadyLoaded(String type1)
	{
		if("java.lang.Object".equals(type1)) return true;
	     Method methodFindLoadedClass;
		try {
			methodFindLoadedClass = ClassLoader.class.getDeclaredMethod("findLoadedClass", new Class[] { String.class });
		
	     methodFindLoadedClass.setAccessible(true);
	 
	     boolean x= methodFindLoadedClass.invoke(classLoader, type1 )!=null;

	     return x;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException("Problem while checking if class "+type1+" is already loaded.",e);
		}
		
	}
	/**
	 * Checks if the type1 is already loaded and returns the Class<?> if it is.
	 * 
	 * @param type1
	 *            internal name of the object to load
	 * @return null: if the object is already loaded or cannot be loaded for any reason
	 */
	private Class<?> tryGetLoadedClass(String type1) {
		Class<?> type1Class;
		type1Class = null;
		try {	
			String name=type1.replace('/', '.');
			
		     // Avoid getting classes that are not loaded yet. Two reasons: a) don't trigger unwanted classloading. b) classes loaded during transformation will not be transformed, see JDK-6469492. 
			if(isAlreadyLoaded(name)) type1Class = Class.forName(name, false,
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
		while (type1!=null) {
			set.add(type1);
		
			type1 = getSuperClassName(type1);
		}
		set.add("java/lang/Object");
		return set;

	}

	private String getSuperClassName(String type1) {
		
		if (type1.equals(targetClassName))
			type1 = superOfTargetClass;
		else
		{
			
			Class<?> tryLoad=tryGetLoadedClass(type1);
			if(tryLoad!=null)
			{
				Class<?> superClass=tryLoad.getSuperclass();
				if(superClass!=null)
				{
					return superClass.getName().replace(".","/");
				} else
				{
					return null;
				}
			}
			
			SuperClassExtractVisitor visitor = createSuperClassVisitorFromBytes(type1);
					if(visitor!=null) type1=visitor.getSuperClass(); else
						type1= null;
					
		}

		return type1;
	}


	/**
	 * Tries to create a visitor and extract superclass/interface information from the byte's of the class.
	 * 
	 * Returns the SuperClassExtractVisitor that can be used to access the extracted information
	 * 
	 * @param theType,
	 * @return the visitor or null if the bytes cannot be accessed by the classloader.
	 */
	private SuperClassExtractVisitor createSuperClassVisitorFromBytes(String theType) {
		SuperClassExtractVisitor readSuperTypeVisitor = null;
		InputStream stream = null;
		try {
			stream = ClassByteUtil.getClassBytesStream(theType, classLoader); 
			

			ClassReader reader = new ClassReader(stream);

			readSuperTypeVisitor = new SuperClassExtractVisitor(Opcodes.ASM5);

			reader.accept(readSuperTypeVisitor, 0);
			if (!theType.equals(readSuperTypeVisitor.getType())) {
				throw new RuntimeException("That was unexpected");
			}

			stream.close();
		} catch (IOException e) {
			JRLog.error(String.format("classloader:%s, targetclass:%s, superOfTarget:%s",classLoader,targetClassName,superOfTargetClass));
			return null;
		}
		return readSuperTypeVisitor;
	}

	public boolean getIsInterface(String internalName) {

		Class<?> theClass = tryGetLoadedClass(internalName);
		if (theClass != null)
			return theClass.isInterface();
		if(internalName.startsWith("[")) return false; // array type is never an interface.
		SuperClassExtractVisitor readSuperTypeVisitor = createSuperClassVisitorFromBytes(internalName);
		if(readSuperTypeVisitor==null) return false;
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
		Class<?> inputClass = tryGetLoadedClass(input.getInternalName());
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

			SuperClassExtractVisitor superAndInterfaces = createSuperClassVisitorFromBytes(input
					.getInternalName());
			if(superAndInterfaces!=null)
			{
				inputSuperClassType = superAndInterfaces.getSuperClassAsType();
				theInputInterfaces = superAndInterfaces.getInterfacesAsTypes();
			} else
			{
				inputSuperClassType=Type.getType("Ljava/lang/Object;");
				theInputInterfaces=new Type[0];
			}

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
