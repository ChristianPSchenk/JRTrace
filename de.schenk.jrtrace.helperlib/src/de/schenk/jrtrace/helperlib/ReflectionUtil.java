/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ReflectionUtil {

	/**
	 * This finds all methods on the class that can be invoked with the provided
	 * list of arguments. Note that this might be ambiguous, if the parameters
	 * are not specific enough (e.g. null values).
	 * 
	 * @param methodNames
	 *            the method name
	 * @param arguments
	 *            the arguments that are supplied
	 * @param clazz
	 *            the class which will be searched for the method
	 * @return a Collection with all the method that match the provided class,
	 *         name and parameter values
	 */
	public static Collection<Method> findMatchingMethod(String methodNames,
			final Object[] arguments, Class<?> clazz) {
		Method[] declaredMethods = clazz.getDeclaredMethods();
		List<Method> method = new ArrayList<Method>();
		for (Method m : declaredMethods) {
			if (m.getName().equals(methodNames)) {
				Class<?>[] parameterTypes = m.getParameterTypes();
				if (parameterTypes.length == arguments.length) {
					boolean match = true;
					for (int i = 0; i < arguments.length; i++) {
						if (arguments[i] == null)
							continue;

						Class<?> methodParameterType = parameterTypes[i];
						Class<?> providedParameterType = arguments[i]
								.getClass();
						methodParameterType = convertPrimitivesToWrapperType(methodParameterType);

						if (!methodParameterType
								.isAssignableFrom(providedParameterType)) {

							match = false;
							break;

						}

					}
					if (match) {

						method.add(m);

					}
				}
			}
		}
		return method;

	}

	private static Class<?> convertPrimitivesToWrapperType(
			Class<?> providedParameterType) {

		Class<?> mapped = JavaPrimitives.getWrapperType(providedParameterType);
		if (mapped != null)
			return mapped;
		else
			return providedParameterType;
	}

	public static Object invokeMethod(Class<?> clazz, String methodName,
			Object... parametersObject) {
		return invokeMethod(clazz, null, methodName, parametersObject);
	}

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
		return invokeMethod(theObject.getClass(), theObject, methodName,
				parametersObject);
	}

	private static Object invokeMethod(Class<?> clazz, Object theObject,
			String methodName, Object... parametersObject) {

		try {
			// Class<?>[] parameters = new Class<?>[parametersObject.length];
			// for (int i = 0; i < parameters.length; i++) {
			// parameters[i] = parametersObject[i].getClass();
			// }
			Collection<Method> methods = ReflectionUtil.findMatchingMethod(
					methodName, parametersObject, clazz);
			if (methods.size() != 1) {
				throw new RuntimeException(
						String.format(
								"Didn't find exactly one matching method %s. Found %d matches.",
								methodName, methods.size()));
			}
			Method method = methods.iterator().next();
			method.setAccessible(true);
			Object returnValue = method.invoke(theObject, parametersObject);
			return returnValue;
		} catch (SecurityException e) {
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
	 * @return the value of the fields
	 * @throws RuntimeException
	 *             if the field is not available or any other exception comes
	 *             up.
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

	/**
	 * @param target
	 *            the target instance or Class<?> for static
	 * @param name
	 *            the field name
	 * @param value
	 *            the value
	 */
	public static void setField(Object target, String name, Object value) {
		Class<?> theClass = null;
		if (target == null)
			throw new RuntimeException(
					"no target object or class in setField(...)");
		if (target instanceof Class<?>) {
			theClass = (Class<?>) target;
		} else {
			theClass = target.getClass();
		}
		Field theField = null;
		while (theClass != null) {

			try {
				theField = theClass.getDeclaredField(name);
				break;
			} catch (NoSuchFieldException e) {
				theClass = theClass.getSuperclass();
			}

		}

		if (theField == null) {
			throw new RuntimeException("Didn't find the field " + name + " on "
					+ target.toString() + " or any of its superclasses.");
		}
		try {
			theField.setAccessible(true);
			theField.set(target, value);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}

	}

}
