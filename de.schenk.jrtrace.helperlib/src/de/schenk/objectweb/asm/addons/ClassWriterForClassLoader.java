/**
 * (c) 2014 by Christian Schenk
 **/
/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.objectweb.asm.addons;

import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassWriter;

public class ClassWriterForClassLoader extends ClassWriter {

	private CommonSuperClassUtil superClassUtil;

	public ClassWriterForClassLoader(ClassReader classReader,
			CommonSuperClassUtil superClassUtil, int flags) {
		super(classReader, flags);
		this.superClassUtil = superClassUtil;
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
		return superClassUtil.getCommonSuperClass(type1, type2);
	}

}
