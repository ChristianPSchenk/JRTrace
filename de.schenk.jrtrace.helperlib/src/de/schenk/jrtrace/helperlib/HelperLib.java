/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

import java.util.Arrays;
import java.util.HashMap;

import de.schenk.jrtrace.helper.InstrumentationUtil;
import de.schenk.jrtrace.helper.NotificationUtil;

public class HelperLib {

	/**
	 * This will send a message (consisting of any plain primitive java Object).
	 * These message will be sent back to the development machine using the id
	 * {@link de.schenk.jrtrace.helperlib.NotificationConstants#NOTIFY_MESSAGE}.
	 * 
	 * <p>
	 * Clients can register to this message via
	 * {@link de.schenk.jrtrace.service.IJRTraceVM#addClientListener} .
	 * 
	 * </p>
	 * 
	 * @param msg
	 *            any java object that can be serialized and deserialized
	 */
	public void sendMessage(Object msg) {

		NotificationUtil.sendMessageNotification(msg);
	}

	/**
	 * Utility method to set the value of any object field.
	 * 
	 * @param target
	 *            the target object or target Class<?> (for static method calls)
	 * @param name
	 *            the name of the field
	 * @param value
	 *            the target value
	 */
	public void setField(Object target, String name, Object value) {
		ReflectionUtil.setField(target, name, value);

	}

	/**
	 * Utility method to get the value of an instance variable (even private)
	 * using reflection.
	 * 
	 * @param target
	 *            the target object or target Class<?> (for static method
	 *            calls), must not be null.
	 * @param name
	 *            the name of the field * @return the value of the field or a
	 *            RuntimeException object if there was a problem.
	 * */
	public Object getField(Object target, String name) {
		try {
			Object o = ReflectionUtil.getPrivateField(target,
					target.getClass(), name);
			return o;
		} catch (RuntimeException e) {
			return e;
		}

	}

	/**
	 * Utility method to get the value of a static class variable (even private)
	 * using reflection.
	 * 
	 * @param target
	 *            the Class<?> for which the field should be fetched.
	 * @param name
	 *            the name of the field @ *
	 * @return the value of the field or a RuntimeException object if there was
	 *         a problem.
	 */
	public Object getField(Class<?> target, String name) {
		try {
			Object o = ReflectionUtil.getPrivateField(null, target, name);
			return o;
		} catch (RuntimeException e) {
			return e;
		}

	}

	/**
	 * Utility method to get the value of a static class variable (even private)
	 * using reflection.
	 * 
	 * @param target
	 *            the fully qualified name of the class
	 * @param name
	 *            the name of the field
	 * @return the value of the field or a RuntimeException object if there was
	 *         a problem.
	 * */
	public Object getField(String target, String name) {
		try {
			Class<?>[] classes = InstrumentationUtil.getClassesByName(target);
			if (classes.length != 1) {
				throw new RuntimeException(
						String.format("getField: Not exactly one class that matches name "
								+ target));
			}
			Object o = ReflectionUtil.getPrivateField(null, classes[0], name);
			return o;
		} catch (RuntimeException e) {
			return e;
		}

	}

	public String getCallerClassName() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		int i = triggerIndex(stack);
		StackTraceElement triggerElement = stack[i + 1];
		String className = triggerElement.getClassName();
		return className;
	}

	private int triggerIndex(StackTraceElement[] stack) {
		for (int i = 1; i < stack.length; i++) {
			String className = stack[i].getClassName();
			if (!className.startsWith("de.schenk.jrtrace.helperlib")
					&& stack[i].getClass().getClassLoader() != null)
				return i;

		}
		return 0;
	}

	public void traceStack() {
		traceStack(Integer.MAX_VALUE);
	}

	public void traceStack(int depth) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();

		for (int i = 3; i < trace.length && i < depth + 3; i++) {
			System.out.println(trace[i].toString());
		}
	}

	public void inspect(Object o) {
		inspect(o, 2, "", "", false, null);
	}

	public void inspect(Object o, int depth) {
		inspect(o, depth, "", "", false, null);
	}

	public void inspect(Object o, int depth, String toStringClasses) {
		inspect(o, depth, toStringClasses, "", false);
	}

	public void inspect(Object o, int depth, String toStringClasses,
			String skipFields) {
		inspect(o, depth, toStringClasses, skipFields, false);
	}

	public void inspect(Object o, int depth, String toStringClasses,
			String skipFields, boolean includeStatics) {
		inspect(o, depth, toStringClasses, skipFields, includeStatics, null);
	}

	/**
	 * A utility class to dump the contents of a variable reflectively,
	 * following references. - fields with value null will be omitted
	 *
	 *
	 * @param o
	 *            the object to dump
	 * @param depth
	 *            recursion depth
	 * @param toStringClasses
	 *            a comma-separated list of simplenames (not qualified) classes
	 *            that should be printed as "toString()". If this is the name of
	 *            an interface then all classes implementing this interface will
	 *            be printed as toString()
	 * @param skipFields
	 *            usedForNames of fields that should not be printed.
	 * @param includeStatics
	 *            include static fields
	 * @param detailFormatters
	 *            string "fieldName=methodname,fieldName2=methodName2" : will
	 *            invoke a java method on the HelperLib (or its subclasses) and
	 *            use it to format the field.
	 */
	public void inspect(Object o, int depth, String toStringClasses,
			String skipFields, boolean includeStatics, String detailFormatters) {
		String[] erg = toStringClasses.split(",");
		String[] erg2 = skipFields.split(",");

		HashMap<String, String> formatterMap = new HashMap<String, String>();

		if (detailFormatters != null) {
			String[] formatter = detailFormatters.split(",");
			for (String format : formatter) {
				String[] oneFormat = format.split("=");
				if (oneFormat.length != 2)
					throw new RuntimeException(
							"Invalid detail formatter syntax: Has to be like 'fieldName=methodName,fieldName2=...");
				formatterMap.put(oneFormat[0], oneFormat[1]);
			}
		}
		System.out.println(new InspectUtil(this).inspect(o, depth,
				Arrays.asList(erg), Arrays.asList(erg2), includeStatics,
				formatterMap));
	}

}
