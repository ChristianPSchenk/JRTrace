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

		boolean arrayToObjectAssignment = (input.getSort() == Type.ARRAY
				&& output.getSort() == Type.OBJECT && output.equals(Type
				.getType(Object.class)));
		if (arrayToObjectAssignment)
			return true;

		boolean basicCheck = ((input).getSort()) == (output.getSort());

		if (!basicCheck)
			return false;
		if (input.getSort() == Type.ARRAY) {
			boolean outputIsObject = output.getElementType().equals(
					Type.getType(Object.class));
			if (!outputIsObject) {
				if (input.getDimensions() != output.getDimensions()) {
					return false;
				}
			} else {
				if (output.getDimensions() > input.getDimensions()) {
					return false;
				}
				return true;
			}

			return isAssignable(input.getElementType(),
					output.getElementType(), util);
		}
		if (input.equals(output)) {
			return true;
		}

		return util.isObjectAssignable(output, input);

	}

	public static boolean isInterface(Type t,
			CommonSuperClassUtil superClassUtil) {

		boolean is = superClassUtil.getIsInterface(t.getInternalName());

		return is;

	}
}
