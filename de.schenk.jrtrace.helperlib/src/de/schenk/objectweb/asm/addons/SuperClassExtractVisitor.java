/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.objectweb.asm.addons;

import de.schenk.objectweb.asm.ClassVisitor;

public class SuperClassExtractVisitor extends ClassVisitor {

	private String theType;
	private String theSuperType;

	public SuperClassExtractVisitor(int api, String theType) {
		super(api);
		this.theType = theType;

	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		if (theType.equals(name)) {
			this.theSuperType = superName;
		}

	}

	public String getSuperClass() {
		return theSuperType;
	}

}
