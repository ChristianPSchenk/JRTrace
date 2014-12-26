/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.objectweb.asm.addons;

import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;

public class SuperClassExtractVisitor extends ClassVisitor {

	private String theType;
	private String theSuperType;
	private String[] theInterfaces;
	private boolean isInterface;

	public SuperClassExtractVisitor(int api) {
		super(api);

	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {

		this.theType = name;
		this.theSuperType = superName;
		this.theInterfaces = (interfaces == null ? new String[0] : interfaces);
		this.isInterface = (access & Opcodes.ACC_INTERFACE) != 0;

	}

	public String getSuperClass() {
		return theSuperType;
	}

	public String getType() {
		return theType;

	}

	public boolean getIsInterface() {
		return isInterface;
	}

	public String[] getInterfaces() {
		return theInterfaces;

	}

	public Type[] getInterfacesAsTypes() {
		Type[] result = new Type[theInterfaces.length];
		for (int i = 0; i < theInterfaces.length; i++) {
			result[i] = Type.getType("L" + theInterfaces[i] + ";");
		}
		return result;
	}

	public Type getSuperClassAsType() {
		if (theSuperType == null)
			return null;
		return Type.getType("L" + theSuperType + ";");

	}

}
