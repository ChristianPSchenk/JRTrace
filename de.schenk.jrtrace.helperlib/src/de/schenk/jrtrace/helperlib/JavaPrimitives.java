package de.schenk.jrtrace.helperlib;

import java.util.HashMap;
import java.util.Map;

public class JavaPrimitives {

	/**
	 * 
	 * @param type
	 *            a java class
	 * @return if primitiveType is a primitive type, return the corresponding
	 *         wrapper type.
	 */
	static public Class<?> getWrapperType(Class<?> type) {
		return mapPrimitiveToWrapper.get(type);
	}

	private static final Map<Class<?>, Class<?>> mapPrimitiveToWrapper = new HashMap<>();

	static {
		mapPrimitiveToWrapper.put(boolean.class, Boolean.class);
		mapPrimitiveToWrapper.put(byte.class, Byte.class);
		mapPrimitiveToWrapper.put(char.class, Character.class);
		mapPrimitiveToWrapper.put(double.class, Double.class);
		mapPrimitiveToWrapper.put(float.class, Float.class);
		mapPrimitiveToWrapper.put(int.class, Integer.class);
		mapPrimitiveToWrapper.put(long.class, Long.class);
		mapPrimitiveToWrapper.put(short.class, Short.class);
		mapPrimitiveToWrapper.put(void.class, Void.class);

	}
}
