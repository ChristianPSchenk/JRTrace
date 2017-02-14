package de.schenk.jrtrace.helperlib.bind;

/**
 * (c) 2014 by Christian Schenk
 **/

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;

import de.schenk.jrtrace.helper.NotificationUtil;

/**
 * 
 * This class is specified by the instrumentation to look up the dynamic
 * call-sites for the INVOKE_DYNAMIC statements that will eventually execute the
 * tracing code.
 * 
 * Therefore it needs to be available to all classes in the system (that are
 * instrumented). When the jrtrace agent is started all the classes in the
 * helperlib plugin are added to the boot classpath.
 * 
 * Eclipse: All classes in the bootclass path are available to all plugins. No
 * problem here.
 * 
 * JBoss: JBoss restricts the access to the bootclass path to "known" paths (all
 * known JRE paths and those specified via -Djboss.modules.system.pkgs=...). By
 * placing the DynamicBinder in the sun.invoke package it is visible to all
 * classes. (Though of course this is a hack and might break at some time).
 * 
 * 
 * @author Christian Schenk
 *
 */
public class DynamicBinder {

	private static final BootstrapMethodError bootsTrapException = new BootstrapMethodError();
	private static Method getEngineXObjectMethod;
	private static Method getEngineXClassMethod;
	private static ThreadLocal<HashSet<String>> currentlyProcessed = new ThreadLocal<HashSet<String>>() {
		protected java.util.HashSet<String> initialValue() {
			return new HashSet<String>();
		};
	};

	static void initHelper() {
		if (getEngineXObjectMethod != null)
			return;
		try {

			Class<?> o = Class.forName(
					"de.schenk.jrtrace.helper.JRTraceHelper", false, null);

			getEngineXObjectMethod = o.getMethod("getEngineXObject",
					String.class, int.class, ClassLoader.class);

			getEngineXClassMethod = o.getMethod("getEngineXClass",
					String.class, int.class, ClassLoader.class);
		} catch (ClassNotFoundException e) {

			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param caller
	 * @param name
	 * @param type
	 * @param enginexclassname
	 * @param jrtraceClasssetId
	 * @param enginexmethodname
	 * @param enginexmethoddescriptor
	 * @return a CallSite which represents a MethodHandle for a Virtual call.
	 */
	public static CallSite bindEngineXMethodsToVirtual(
			MethodHandles.Lookup caller, String name, MethodType type,
			String enginexclassname, int jrtraceClasssetId,
			String enginexmethodname, String enginexmethoddescriptor) {

		initHelper();
		final String callerclassname = caller.toString();
		
		currentlyProcessed.get().add(callerclassname);
		MethodHandles.Lookup lookup = MethodHandles.lookup();

		Class<?> enginexclass;
		try {

			enginexclass = (Class<?>) getEngineXClassMethod.invoke(null,
					enginexclassname, jrtraceClasssetId, caller.lookupClass()
							.getClassLoader());
		} catch (IllegalArgumentException | InvocationTargetException
				| IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		if (enginexclass == null) {

			throw new JRTraceClassLookupException("Fatal: Lookup of enginex class "
					+ enginexclassname + " failed!");
		}

		MethodHandle enginexMethod = null;
		try {
			enginexMethod = lookup.findVirtual(enginexclass, enginexmethodname,
					MethodType.fromMethodDescriptorString(
							enginexmethoddescriptor, caller.lookupClass()
									.getClassLoader()));

		} catch (IllegalAccessException | NoSuchMethodException e) {

			NotificationUtil
					.sendProblemNotification(
							String.format(
									"It is not possible to inject into %s due to an IllegalAccessException. This indicates that the injected method requires classes that are either not present or not accessible. Check your classloader settings.",
									caller.toString()), enginexclassname,
							enginexmethodname, enginexmethoddescriptor);
			e.printStackTrace();
		}

		currentlyProcessed.get().remove(callerclassname);
		return new ConstantCallSite(enginexMethod.asType(type));

	}

	/**
	 * 
	 * @param caller
	 * @param name
	 * @param type
	 * @param enginexclass
	 * @param jrtraceClasssetId
	 * @param enginexmethodname
	 * @param enginexmethoddescriptor
	 * @return a callsite which returns a pre-bound method handle: a method that
	 *         can be invoked without putting the instance on the stack.
	 */
	public static CallSite bindEngineXMethods(MethodHandles.Lookup caller,
			String name, MethodType type, String enginexclass,
			int jrtraceClasssetId, String enginexmethodname,
			String enginexmethoddescriptor) {

		initHelper();
		final String callerclassname = caller.toString();
		

		MethodHandles.Lookup lookup = MethodHandles.lookup();

		Object object;
		try {

			object = getEngineXObjectMethod.invoke(null, enginexclass,
					jrtraceClasssetId, caller.lookupClass().getClassLoader());
		} catch (IllegalArgumentException | InvocationTargetException
				| IllegalAccessException e) {
			e.printStackTrace();
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

		} catch (IllegalAccessException | NoSuchMethodException e) {

			NotificationUtil
					.sendProblemNotification(
							String.format(
									"It is not possible to inject into %s due to an IllegalAccessException. This indicates that the injected method requires classes that are either not present or not accessible. Check your classloader settings.",
									caller.toString()), enginexclass,
							enginexmethodname, enginexmethoddescriptor);
			e.printStackTrace();
		}

		ConstantCallSite callsite = new ConstantCallSite(
				enginexMethod.asType(type));
		currentlyProcessed.get().remove(callerclassname);
		return callsite;

	}

	
}
