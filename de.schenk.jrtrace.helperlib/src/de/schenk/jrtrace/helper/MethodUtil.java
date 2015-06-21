/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.commons.Method;

public class MethodUtil {

	/**
	 * Returns the parameters of the given method descriptor as classname (e.g.
	 * java.lang.Object) or primitive names (e.g. boolean)
	 * 
	 * @param desc
	 *            the descriptor
	 * @return
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

	/**
	 * Formats a method name and descriptor in a human readable form similar to
	 * how JDT does it: methodname(Ljava/lang/Object;)Z
	 * 
	 * The result remains stable but is not intended for processing and may not
	 * capture all details
	 * 
	 * methodname (Object) : boolean
	 * 
	 * @param methodName
	 * @param desc
	 * @return
	 */
	public static String getHumanReadableName(String methodName, String desc) {
		StringBuilder result = new StringBuilder();
		result.append(methodName + " (");
		Type[] arguments = Type.getArgumentTypes(desc);
		boolean first = true;
		for (Type a : arguments) {
			if (!first) {
				result.append(", ");
			} else
				first = false;
			result.append(toShortForm(a));
		}

		result.append(") : ");
		result.append(toShortForm(Type.getReturnType(desc)));
		return result.toString();
	}

	/**
	 * Returns the short form of a type e.g. for my.package.Object$Sub -> Sub ,
	 * for my: java.lang.Object -> Object
	 * 
	 * To be used for Display purposes only
	 * 
	 * @param type
	 * @return
	 */
	public static String toShortForm(Type type) {
		String name = type.getClassName();
		int lastDot = name.lastIndexOf(".");
		int lastDollar = name.lastIndexOf("$");
		int last = -1;
		if (lastDot != -1)
			last = lastDot;
		if (lastDollar > lastDot)
			last = lastDollar;
		if (last == -1)
			return name;
		return name.substring(last + 1, name.length());

	}

}
