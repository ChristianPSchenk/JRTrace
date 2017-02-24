/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.regex.Pattern;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
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
		assertTrue(machine.clearEngineX());
		machine.setLogLevel(JRLog.DEBUG);

		connectedButNoClassesTest();

		JavaUtil javautil = new JavaUtil();
		File classFile = javautil.getFileForClass("InjectForDiagnosticsTest",
				"de.schenk.jrtrace.service.enginex.testclasses");
		File classFile2 = javautil.getFileForClass(
				"InjectForDiagnosticsTestWithExclude",
				"de.schenk.jrtrace.service.enginex.testclasses");

		File classFile3 = javautil.getFileForClass(
				"InjectForDiagnosticsTestCausingException",
				"de.schenk.jrtrace.service.enginex.testclasses");

		theNotificationListener = new TestNotificationListener();
		machine.addClientListener(NotificationConstants.NOTIFY_PROBLEM,
				theNotificationListener);

		byte[][] classBytes = new byte[3][];
		classBytes[0] = Files.readAllBytes(Paths.get(classFile.toURI()));
		classBytes[1] = Files.readAllBytes(Paths.get(classFile2.toURI()));
		classBytes[2] = Files.readAllBytes(Paths.get(classFile3.toURI()));
		machine.installJRTraceClasses(classBytes);

	}

	@Test
	public void testResultForUnloadedClass() {
		InjectStatus status = machine
				.analyzeInjectionStatus("a.not.existing.ClassName");

		assertEquals(StatusEntityType.JRTRACE_SESSION, status.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, status.getInjectionState());
		assertTrue(status.getMessage().contains(
				InjectStatus.MSG_CLASS_NOT_LOADED));
	}

	private static void connectedButNoClassesTest() {
		InjectStatus status = machine.analyzeInjectionStatus("a.b.C");
		assertEquals(status.getEntityType(), (StatusEntityType.JRTRACE_SESSION));
		assertEquals(status.getInjectionState(), StatusState.DOESNT_INJECT);
		assertEquals(status.getMessage(), InjectStatus.MSG_NO_JRTRACE_CLASSES);
	}

	public static void notConnectedTest() {
		InjectStatus status = machine.analyzeInjectionStatus("a.b.C");
		assertEquals(status.getEntityType(), (StatusEntityType.JRTRACE_SESSION));
		assertEquals(status.getInjectionState(), StatusState.DOESNT_INJECT);
		assertEquals(InjectStatus.MSG_NOT_CONNECTED, status.getMessage());
	}

	@Test
	public void testSystemExcludeClasses() {
		InjectStatus status = machine.analyzeInjectionStatus(String.class
				.getName());

		assertEquals(StatusEntityType.JRTRACE_SESSION, status.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, status.getInjectionState());
		InjectStatus classStatus = getFirstChildStatus(status);
		assertEquals(StatusEntityType.JRTRACE_CHECKED_CLASS,
				classStatus.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, classStatus.getInjectionState());
		assertEquals(InjectStatus.MSG_SYSTEM_EXCLUDE, classStatus.getMessage());
		assertEquals(0, classStatus.getChildStatus().size());
	}

	private InjectStatus getChildFromSubString(InjectStatus status,
			String string) {
		Set<InjectStatus> children = status.getChildStatus();
		for (InjectStatus c : children) {
			if (c.getMessage().contains(string)
					|| c.getEntityName().contains(string)) {
				return c;
			}
		}
		fail("didn't find a child status with the substring " + string
				+ " in message or entityname");
		return null;
	}

	public InjectStatus getFirstChildStatus(InjectStatus status) {
		Set<InjectStatus> children = status.getChildStatus();
		assertTrue(children.size() > 0);
		InjectStatus classStatus = children.iterator().next();
		return classStatus;
	}

	@Test
	public void testJRTracClassCannotBeInstrumented() {
		new EngineXDiagnosticsTarget().matchingMethod("a");
		InjectStatus status = machine
				.analyzeInjectionStatus("de.schenk.jrtrace.enginex.testclasses.diagnostics.InjectForDiagnosticsTest");

		assertEquals(StatusEntityType.JRTRACE_SESSION, status.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, status.getInjectionState());

		InjectStatus jrtraceclass = getChildFromSubString(status,
				"InjectForDiagnosticsTest");
		assertTrue(jrtraceclass.getMessage().contains(
				InjectStatus.MSG_JRTRACE_CLASS_CANNOT_BE_INSTRUMENTED));
		assertEquals(StatusState.DOESNT_INJECT,
				jrtraceclass.getInjectionState());
	}

	public class ExistingClass {
	}

	@Test
	public void testClassNameDoesntMatch() {
		ExistingClass e = new ExistingClass();

		InjectStatus status = machine
				.analyzeInjectionStatus("de.schenk.jrtrace.enginex.testscripts.EngineXDiagnosticsTest$ExistingClass");

		assertEquals(StatusEntityType.JRTRACE_SESSION, status.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, status.getInjectionState());
		InjectStatus a = status
				.getChildByEntityName(Pattern.compile(".*EngineXDiagnosticsTest\\$ExistingClass.*"));
		assertEquals(StatusEntityType.JRTRACE_CHECKED_CLASS, a.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, a.getInjectionState());
		InjectStatus b = a.getChildByEntityName(Pattern.compile(".*InjectForDiagnosticsTest$.*"));
		assertEquals(StatusEntityType.JRTRACE_CLASS, b.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, b.getInjectionState());
		assertTrue(b.getMessage().contains(
				InjectStatus.MSG_CLASS_NAME_DOESNT_MATCH));
	}

	public class CandidateForFaultyInjection {
		public int method(int i) {
			return i * i;
		}
	}

	@Test
	public void testClassInstrumentationThrowsException() {
		CandidateForFaultyInjection e = new CandidateForFaultyInjection();

		InjectStatus status = machine
				.analyzeInjectionStatus("de.schenk.jrtrace.enginex.testscripts.EngineXDiagnosticsTest$CandidateForFaultyInjection");

		assertEquals(StatusEntityType.JRTRACE_SESSION, status.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, status.getInjectionState());
		InjectStatus a = status
				.getChildByEntityName(Pattern.compile(".*EngineXDiagnosticsTest\\$CandidateForFaultyInjection.*"));
		assertEquals(StatusEntityType.JRTRACE_CHECKED_CLASS, a.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, a.getInjectionState());

		InjectStatus b = a
				.getChildByEntityName(Pattern.compile(".*InjectForDiagnosticsTestCausingException.*"));
		assertEquals(StatusState.DOESNT_INJECT, b.getInjectionState());

		assertEquals(InjectStatus.MSG_EXCEPTION, b.getMessage());

	}

	@Test
	public void testCorrectMessageForExcludedClass() {
		EngineXDiagnosticsTargetForExclude x = new EngineXDiagnosticsTargetForExclude();

		InjectStatus status = machine
				.analyzeInjectionStatus("de.schenk.jrtrace.enginex.testscripts.EngineXDiagnosticsTargetForExclude");

		assertEquals(StatusEntityType.JRTRACE_SESSION, status.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, status.getInjectionState());
		InjectStatus a = status
				.getChildByEntityName(Pattern.compile(".*EngineXDiagnosticsTargetForExclude.*"));
		assertEquals(StatusEntityType.JRTRACE_CHECKED_CLASS, a.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, a.getInjectionState());
		assertEquals("", a.getMessage());
		InjectStatus b = a
				.getChildByEntityName(Pattern.compile(".*InjectForDiagnosticsTestWithExclude.*"));
		assertEquals(StatusEntityType.JRTRACE_CLASS, b.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, b.getInjectionState());

		assertEquals(InjectStatus.MSG_CLASS_IS_EXCLUDED, b.getMessage());
	}

	@Test
	public void testMatchingMethod() {
		EngineXDiagnosticsTarget x = new EngineXDiagnosticsTarget();

		InjectStatus status = machine
				.analyzeInjectionStatus("de.schenk.jrtrace.enginex.testscripts.EngineXDiagnosticsTarget");

		assertEquals(StatusEntityType.JRTRACE_SESSION, status.getEntityType());
		assertEquals(StatusState.INJECTS, status.getInjectionState());
		InjectStatus a = status
				.getChildByEntityName(Pattern.compile(".*EngineXDiagnosticsTarget .*"));
		assertEquals(StatusEntityType.JRTRACE_CHECKED_CLASS, a.getEntityType());
		assertEquals(StatusState.INJECTS, a.getInjectionState());
		assertEquals("", a.getMessage());
		InjectStatus b = a.getChildByEntityName(Pattern.compile(".*InjectForDiagnosticsTest$.*"));
		assertEquals(StatusEntityType.JRTRACE_CLASS, b.getEntityType());
		assertEquals(StatusState.INJECTS, b.getInjectionState());
		assertEquals("", b.getMessage());

		InjectStatus c = b.getChildByEntityName(Pattern.compile(".*matchingMethod.*"));
		assertEquals(StatusEntityType.JRTRACE_CHECKED_METHOD, c.getEntityType());
		assertEquals(StatusState.INJECTS, c.getInjectionState());

		InjectStatus d = b.getChildByEntityName(Pattern.compile(".*someOtherMethod.*"));
		assertEquals(StatusEntityType.JRTRACE_CHECKED_METHOD, d.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, d.getInjectionState());

		InjectStatus d2 = d.getChildByEntityName(Pattern.compile(".*injector1.*"));
		assertEquals(StatusEntityType.JRTRACE_METHOD, d2.getEntityType());
		assertEquals(StatusState.DOESNT_INJECT, d2.getInjectionState());
		assertTrue(d2.getMessage().contains(
				InjectStatus.MSG_METHODNAME_DOESNT_MATCH));
	}

}
