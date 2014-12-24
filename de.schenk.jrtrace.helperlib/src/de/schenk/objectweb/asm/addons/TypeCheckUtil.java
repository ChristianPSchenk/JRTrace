/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.objectweb.asm.addons;

import de.schenk.objectweb.asm.Type;

public class TypeCheckUtil {

	/**
	 * checks whether the input type is assigneable to the output type
	 * 
	 * @param input
	 * @param output
	 * @param util
	 *            provides the possibility to calculate the common super class
	 *            of two classes.
	 * @return true, if a variable of type input can be assigned to a slot with
	 *         type output.
	 */
	public static boolean isAssignable(Type input, Type output,
			CommonSuperClassUtil util) {
		boolean basicCheck = ((input).getSort()) == (output.getSort());
		if (!basicCheck)
			return false;
		if (input.getSort() == Type.ARRAY) {
			if (input.getDimensions() != output.getDimensions()) {
				return false;
			}

			input = input.getElementType();
			output = output.getElementType();
		}
		if (input.equals(output)) {
			return true;
		}

		String result = util.getCommonSuperClass(input.getInternalName(),
				output.getInternalName());
		if (output.getInternalName().equals(result))
			return true;

		return false;

	}
}
