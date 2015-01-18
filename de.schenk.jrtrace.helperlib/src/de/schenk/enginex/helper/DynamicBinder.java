/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DynamicBinder {

	private static Method method;

	static void initHelper() {
		if (method != null)
			return;
		try {

			Class<?> o = Class.forName(
					"de.schenk.enginex.helper.EngineXHelper", false, null);

			method = o.getMethod("getEngineXObject", String.class,
					ClassLoader.class);

		} catch (ClassNotFoundException e) {

			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public static CallSite bindEngineXMethods(MethodHandles.Lookup caller,
			String name, MethodType type, String enginexclass,
			String enginexmethodname, String enginexmethoddescriptor)
			throws NoSuchMethodException, IllegalAccessException {

		initHelper();
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		Object object;
		try {
			object = method.invoke(null, enginexclass, caller.lookupClass()
					.getClassLoader());
		} catch (IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		if (object == null) {
			throw new RuntimeException("Fatal: Lookup of enginex class "
					+ enginexclass + " failed!");
		}

		MethodHandle enginexMethod = null;
		try {
			enginexMethod = lookup.bind(object, enginexmethodname, MethodType
					.fromMethodDescriptorString(enginexmethoddescriptor, caller
							.lookupClass().getClassLoader()));
		} catch (IllegalAccessException e) {

			NotificationUtil
					.sendProblemNotification(
							String.format(
									"It is not possible to inject into %s due to an IllegalAccessException. This indicates that the injected method requires classes that are either not present or not accessible. Check your classloader settings.",
									caller.toString()), enginexclass,
							enginexmethodname, enginexmethoddescriptor);
			e.printStackTrace();
		}
		// System.out.println("bootstrapping " + lookup.getClass().toString()
		// + " to " + enginexclass + " / " + enginexmethodname);
		return new ConstantCallSite(enginexMethod.asType(type));

	}

}
