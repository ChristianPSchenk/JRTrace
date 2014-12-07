/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.commons.Method;

public class MethodUtil {

	/*
	 * returns true if the specified descriptor matches the argumentList exactly
	 */
	public static String[] getParametersAsString(String desc) {

		Method method = new Method("dummy", desc);
		Type[] argumentTypes = method.getArgumentTypes();

		String[] argumentStrings = new String[argumentTypes.length];
		for (int i = 0; i < argumentTypes.length; i++) {
			argumentStrings[i] = argumentTypes[i].getClassName();
		}
		return argumentStrings;
	}

}
