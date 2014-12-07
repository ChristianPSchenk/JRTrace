/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class ReflectionUtil {
	/**
	 * reflectively invokes the method on the object and catches all exceptions
	 * and wraps them in RuntimeExceptions
	 * 
	 * @param theObject
	 *            the object on which to invoke the method
	 * @param methodName
	 *            the methods name
	 * @param parameters
	 *            the parameters
	 * @return the return value
	 */
	public static Object invokeMethod(Object theObject, String methodName,
			Object... parametersObject) {

		try {
			Class<?>[] parameters = new Class<?>[parametersObject.length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = parametersObject[i].getClass();
			}
			Method method = theObject.getClass().getMethod(methodName,
					parameters);
			Object returnValue = method.invoke(theObject, parametersObject);
			return returnValue;
		} catch (NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);

		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Looks up a field and returns the value. Searches also private fields and
	 * super classes.
	 * 
	 * @param theInstance
	 *            the instance of class of which you need the field, or null for
	 *            static fields
	 * @param theClass
	 *            the class or null if you have provided an instance (then the
	 *            class will be taken from the instance)
	 * @param name
	 *            the field name
	 * @return
	 */
	public static Object getPrivateField(Object theInstance, Class<?> theClass,
			String name) {
		if (theClass == null && theInstance != null) {
			theClass = theInstance.getClass();
		}
		Object fieldValue = null;
		try {
			while (theClass != null) {
				Field[] fields = theClass.getDeclaredFields();

				for (int i = 0; i < fields.length; i++) {
					if (fields[i].getName().equals(name)) {
						fields[i].setAccessible(true);
						fieldValue = fields[i].get(theInstance);
						return fieldValue;
					}
				}
				theClass = theClass.getSuperclass();
			}

		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		throw new RuntimeException("Field " + name + " not found");

	}

	/**
	 * 
	 * @param theInstance
	 *            the instance of class of which you need the field, or null for
	 *            static fields
	 * @param theClass
	 *            the class or null if you have provided an instance (then the
	 *            class will be taken from the instance)
	 * @param name
	 *            the field name
	 * @return
	 */
	public static Object getPrivateField(Object theInstance,
			Field privateStringField) {

		Object fieldValue = null;
		try {

			privateStringField.setAccessible(true);
			fieldValue = privateStringField.get(theInstance);

		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
		return fieldValue;
	}

	/**
	 * 
	 * @param o
	 *            an instance
	 * @return all fields of this objects class and all superclasses
	 */
	public static Field[] getFields(Object o) {
		Class<? extends Object> theClass = o.getClass();
		ArrayList<Field> fields = new ArrayList<Field>();
		while (theClass != null) {
			Field[] f = theClass.getDeclaredFields();
			for (Field theF : f)
				fields.add(theF);
			theClass = theClass.getSuperclass();

		}
		return fields.toArray(new Field[fields.size()]);

	}

}
