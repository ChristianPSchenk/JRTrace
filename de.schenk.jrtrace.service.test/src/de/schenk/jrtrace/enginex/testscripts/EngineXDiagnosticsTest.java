/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.test.utils.JavaUtil;

/**
 * install the test enginex jar once , execute all tests and then detach.
 * 
 * Tests follow the naming scheme: "test##<comment>"
 * 
 * They usually use the test class Test## which are instrumented using
 * Test#Script
 * 
 * @author Christian Schenk
 *
 */
public class EngineXDiagnosticsTest implements testNotConnectedStatus {

	private static JRTraceController bmController;
	private static String pid;
	private static IJRTraceVM machine;
	private static TestNotificationListener theNotificationListener;

	@AfterClass
	static public void after() throws Exception {
		machine.detach();
		notConnectedTest();
	}

	@BeforeClass
	static public void before() throws Exception {

		bmController = JRTraceControllerService.getInstance();
		String javaName = ManagementFactory.getRuntimeMXBean().getName();
		String[] javaSplit = javaName.split("@");
		pid = javaSplit[0];

		machine = bmController.getMachine(pid, null);

		notConnectedTest();

		assertTrue(machine.attach());
		machine.setLogLevel(JRLog.DEBUG);

		connectedButNoClassesTest();

		JavaUtil javautil = new JavaUtil();
		File classFile = javautil
				.getFileForClass(
						"de.schenk.jrtrace.enginex.testclasses.diagnostics.InjectForDiagnosticsTest",
						"de.schenk.jrtrace.service.enginex.testclasses");

		theNotificationListener = new TestNotificationListener();
		machine.addClientListener(NotificationConstants.NOTIFY_PROBLEM,
				theNotificationListener);

		byte[] jarBytes = Files.readAllBytes(Paths.get(classFile.toURI()));
		byte[][] classBytes = new byte[1][];
		classBytes[0] = jarBytes;
		machine.installJRTraceClasses(classBytes);

	}

	@Test
	public void testResultForUnloadedClass() {
		InjectStatus status = machine.analyzeInjectionStatus(
				"a.not.existing.ClassName", "doesn't matter");

		assertEquals(InjectStatus.JRTRACE_SESSION, status.getEntityType());
		assertEquals(InjectStatus.STATE_DOESNT_INJECT,
				status.getInjectionState());
		assertEquals(InjectStatus.MSG_CLASS_NOT_LOADED, status.getMessage());
	}

	private static void connectedButNoClassesTest() {
		InjectStatus status = machine.analyzeInjectionStatus("a.b.C",
				"public void doesntmatter()");
		assertEquals(status.getEntityType(), (InjectStatus.JRTRACE_SESSION));
		assertEquals(status.getInjectionState(),
				InjectStatus.STATE_DOESNT_INJECT);
		assertEquals(status.getMessage(), InjectStatus.MSG_NO_JRTRACE_CLASSES);
	}

	public static void notConnectedTest() {
		InjectStatus status = machine.analyzeInjectionStatus("a.b.C",
				"public void doesntmatter()");
		assertEquals(status.getEntityType(), (InjectStatus.JRTRACE_SESSION));
		assertEquals(status.getInjectionState(),
				InjectStatus.STATE_DOESNT_INJECT);
		assertEquals(InjectStatus.MSG_NOT_CONNECTED, status.getMessage());
	}

	@Test
	public void testSystemExcludeClasses() {
		InjectStatus status = machine.analyzeInjectionStatus(
				String.class.getName(),
				String.class.getMethods()[0].toGenericString());

		assertEquals(InjectStatus.JRTRACE_SESSION, status.getEntityType());
		assertEquals(InjectStatus.STATE_DOESNT_INJECT,
				status.getInjectionState());
		assertEquals(InjectStatus.MSG_SYSTEM_EXCLUDE, status.getMessage());
	}

	@Test
	public void testMatchingMethod() {
		InjectStatus status = machine
				.analyzeInjectionStatus(
						"de.schenk.jrtrace.enginex.testscripts.EngineXDiagnosticsTarget",
						"public void matchingMethod(java.lang.String)");

		assertEquals(InjectStatus.JRTRACE_SESSION, status.getEntityType());
		assertEquals(InjectStatus.STATE_INJECTS, status.getInjectionState());
	}

}
