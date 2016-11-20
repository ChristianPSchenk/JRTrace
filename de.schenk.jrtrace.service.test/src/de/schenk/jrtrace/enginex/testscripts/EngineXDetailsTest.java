/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.management.Notification;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.OpenResourceAction;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;

import de.schenk.jrtrace.commonsuper.test.CSCore;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.helperlib.NotificationMessages;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.ui.util.JarByteUtil;

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
public class EngineXDetailsTest {

	private static JRTraceController bmController;
	private static String pid;
	private static IJRTraceVM machine;
	private static TestNotificationListener theNotificationListener;
	private static InstallErrorListener errorsDuringInstallErrors;

	@AfterClass
	static public void after() throws Exception {
		machine.detach();
	}

	@BeforeClass
	static public void before() throws Exception {

		bmController = JRTraceControllerService.getInstance();
		String javaName = ManagementFactory.getRuntimeMXBean().getName();
		String[] javaSplit = javaName.split("@");
		pid = javaSplit[0];
		machine = bmController.getMachine(pid, null);
		assertTrue(machine.attach());
		machine.setLogLevel(JRLog.DEBUG);

		DoneListener doneListener = new DoneListener();

		Bundle bundle = Platform
				.getBundle("de.schenk.jrtrace.service.enginex.testclasses");

		URL fileURL = FileLocator.find(bundle,
				new Path("lib/EngineXTests.jar"), null);
		File theFile = new File(FileLocator.toFileURL(fileURL).toURI());

		errorsDuringInstallErrors = new InstallErrorListener();
		machine.addClientListener(NotificationConstants.NOTIFY_PROBLEM,
				errorsDuringInstallErrors);

		byte[] jarBytes = Files.readAllBytes(Paths.get(theFile.toURI()));
		byte[][] classBytes = JarByteUtil.convertJarToClassByteArray(jarBytes);
		machine.installJRTraceClasses(classBytes);

		theNotificationListener = new TestNotificationListener();
		machine.addClientListener(NotificationConstants.NOTIFY_PROBLEM,
				theNotificationListener);

	}

	@Test
	public void test1() throws Exception {
		/* ensure that there are no problems with abstract methods */
		/* they should be ignored */
		/* and test that injection in derived classes works */
		Test1 test1 = new Test1() {

			@Override
			public void test1() {

			}

		};
		test1.test1();

		assertEquals(Test1.counter, 1);
	}

	@Test
	public void test2() throws Exception {
		/*
		 * test that injection in methods implemented from interfaces works if
		 * derived is true
		 */
		Test2 test2 = new Test2() {

			@Override
			public void doit() {

			}

		};
		TestCounterClass.counter = 0;
		test2.doit();

		assertEquals(TestCounterClass.counter, 1);
	}

	@Test
	public void test3() throws Exception {
		/*
		 * test that argument matching works
		 */

		Test3 test3 = new Test3();
		TestCounterClass.counter = 0;
		test3.test3("hallo");
		test3.test3(1.0F);

		assertEquals(1, TestCounterClass.counter);
	}

	/**
	 * Regression test for the problem, that there mayMatch didn't work for
	 * Method argument.
	 * 
	 * @throws Exception
	 */
	@Test
	public void test41MultipleArguments() throws Exception {
		/*
		 * test that argument matching works
		 */

		Test41 test41 = new Test41();
		TestCounterClass.counter = 0;
		test41.test41("hallo", new Object());

		assertEquals(1, TestCounterClass.counter);
	}

	@Test
	public void test4() throws Exception {
		/*
		 * ensure this passed to instrumented method
		 */
		Test4 test4 = new Test4();
		TestCounterClass.counter = 0;
		test4.test4(1.0f);

		assertEquals(5, TestCounterClass.counter);
	}

	@Test
	public void test5() throws Exception {
		/*
		 * ensure that classloaderpolicy=BOOT works
		 */
		Test5 test5 = new Test5();

		boolean exception = false;
		try {
			test5.test5();
		} catch (NoClassDefFoundError c) {
			exception = true;
		}
		assertTrue(exception);
	}

	@Test
	public void test6() throws Exception {
		/*
		 * ensure that classloaderpolicy=NAMED works (The instrumentation of
		 * Test6Script reads out the FrameworkEvent.INFO constant)
		 */
		Test6 test6 = new Test6();

		String msg = "";
		String result = Integer.toString(FrameworkEvent.INFO);
		try {
			test6.test6();
		} catch (RuntimeException c) {
			msg = c.getMessage();
		}
		assertEquals(result, msg);
	}

	@Test
	public void test7() throws Exception {
		/*
		 * ensure that classloaderpolicy=NAMED works (The instrumentation of
		 * Test6Script reads out the FrameworkEvent.INFO constant)
		 */
		Test7 test7 = new Test7();

		String msg = "";

		try {
			test7.test7();
		} catch (RuntimeException c) {
			msg = c.getMessage();
		}
		assertEquals("didit", msg);
	}

	@Test
	public void test40InvokeMethodOfHelperLib() throws Exception {

		Test40 test40 = new Test40();

		String msg = "";

		msg = test40.test40();

		assertEquals("1String", msg);
	}

	@Test
	public void test8() throws Exception {

		Test8 test8 = new Test8();

		String msg = "";

		try {
			test8.test8(1, 2.0D, "abc");
		} catch (RuntimeException c) {
			msg = c.getMessage();
		}
		assertTrue(Test8.success);
		assertEquals("", msg);
	}

	@Test
	public void test9() throws Exception {

		String msg = "";

		try {
			Test9.test9(1, 2.0D, "abc");
		} catch (RuntimeException c) {
			msg = c.getMessage();
		}
		assertEquals(true, Test9.success);
		assertEquals("", msg);
	}

	@Test
	public void test10() throws Exception {

		String msg = "";

		Test10 test10 = new Test10();
		try {
			test10.test10();
		} catch (RuntimeException c) {
			msg = c.getMessage();
		}
		assertEquals("good return value", msg);
	}

	@Test
	public void test11() throws Exception {

		Test11 test11 = new Test11();
		Object result = test11.test11();
		assertEquals(1235, result);

		Object erg = test11.test11b();
		assertNull(erg);
	}

	@Test
	public void test12() throws Exception {

		Test12 test12 = new Test12();
		boolean resul2 = test12.test12();
		assertEquals(true, resul2);

	}

	/**
	 * Test that constructor injection works
	 * 
	 * @throws Exception
	 */
	@Test
	public void test13() throws Exception {

		Test13 test13 = new Test13();
		assertEquals(1, Test13.a);
		test13 = new Test13(5);
		assertEquals(2, Test13.a);

	}

	@Test
	public void test14injectFields() throws Exception {

		Test14 test14 = new Test14();

		test14.test14();
		assertTrue(Test14.success);

		Test14.staticTest14();
		assertTrue(Test14.staticsuccess);
	}

	@Test
	public void test15unannotatedClassesAeInBootClassPath() throws Exception {

		Test15 test15 = new Test15();
		test15.test15();
		assertTrue(Test15.success);

	}

	@Test
	public void test16privateMethodInstrumentation() throws Exception {

		Test16 test16 = new Test16();
		test16.test16();
		assertTrue(Test16.success);

	}

	@Test
	public void test17ReturnValueIsLong() throws Exception {

		Test17 test17 = new Test17();
		test17.test17();
		assertTrue(Test17.success);

	}

	@Test
	public void test18InvokeBefore() throws Exception {

		Test18 test18 = new Test18();
		long stage = test18.test18();
		assertEquals(3, Test18.stage);

	}

	@Test
	public void test19InvokeReplace() throws Exception {

		Test19 test19 = new Test19();
		long stage = test19.test19();
		assertEquals(1002, stage);

	}

	@Test
	public void test20InvokeAfter() throws Exception {

		Test20 test20 = new Test20();
		long result = test20.test20();
		assertEquals(15, result);

	}

	@Test
	public void test21UseInvokeClassName() throws Exception {

		Test21 test21 = new Test21();
		long result = test21.test21();
		assertEquals(1, result);

	}

	@Test
	public void test21InvokeReplaceWithSpecificInvokeClass() throws Exception {

		Test21 test21 = new Test21();
		int result = test21.test21();
		assertEquals(1, result);

	}

	@Test
	public void test22XClassExclude() throws Exception {

		Test22 test22 = new Test22();
		int result = test22.test22();
		assertEquals(1, result);

	}

	@Test
	public void test23FieldAccess() throws Exception {

		Test23 test23 = new Test23();
		int result = test23.test23();
		assertEquals(25, result);
		assertEquals(11, Test23.hitpoint);

	}

	@Test
	public void test25BugfixDerivedDoesntIncludeClassItself() throws Exception {

		Test25 test25 = new Test25();
		String result = test25.test25();
		assertEquals("instrumented", result);

	}

	@Test
	public void test26NoXClassHasNoArgConstructor() throws Exception {

		Test26 test26 = new Test26();
		String result = test26.test26();
		assertEquals("1", result);

	}

	@Test
	public void test28AnonymousClasses() throws Exception {

		Test28 test28 = new Test28();
		String result = test28.test28();
		assertEquals("instrumented", result);

	}

	@Test
	public void test29AnonymousClassesWithClassloaderPolicyTarget()
			throws Exception {

		Test29 test29 = new Test29();
		String result = test29.test29();
		assertEquals("instrumented", result);

	}

	@Test
	public void test30TargetClassPolicyInjectionUsesJRTraceOnBootClassPath()
			throws Exception {

		Test30 test30 = new Test30();
		String result = test30.test30();
		assertEquals("instrumented", result);

	}

	@Test
	public void test31testQualifierRestrictions() throws Exception {

		Test31 test31 = new Test31();
		int result = test31.test31();
		assertEquals(12, result);

	}

	@Test
	public void test32INVOKEBEFOREandINVOKEAFTERForTheSameMethod()
			throws Exception {

		Test32 test32 = new Test32();
		int result = test32.test32();
		assertEquals(2, result);

	}

	@Test
	public void test33InjectMethodSignature() throws Exception {

		Test33 test33 = new Test33();
		String result = test33.test33(null, 5);
		assertEquals(
				"java.lang.String de.schenk.jrtrace.enginex.testscripts.Test33.test33(de.schenk.jrtrace.enginex.testscripts.Test33,int)",
				result);

	}

	@Test
	public void test34ThrowsException() throws Exception {

		Test34 test34 = new Test34();
		try {
			String result = test34.test34();
		} catch (Throwable e) {
			assertTrue(e instanceof RuntimeException);
			RuntimeException r = (RuntimeException) e;
			assertEquals("test2", r.getMessage());
		}

		assertEquals(true, test34.exceptionNoted);
	}

	@Test
	public void test36methodinstance() throws Exception {

		Test36 test36 = new Test36();

		int erg = test36.test36();
		assertEquals(40, erg);
	}

	@Test
	public void test37multipleFieldAccessesSomeMatchSomeNot() throws Exception {

		Test37 test37 = new Test37();

		int erg = test37.test37();
		assertEquals(16, erg);
	}

	@Test
	public void test38testAfterInvokeTraceOnStaticMethod() throws Exception {

		Test38 test38 = new Test38();

		long erg = test38.test38();
		assertEquals(6, erg);
	}

	@Test
	public void test35EnsureTypeOfXExceptionIsChecked() throws Exception {

		theNotificationListener.reset();
		Test35 test35 = new Test35();
		try {
			String result = test35.test35();
		} catch (Throwable e) {

		}

		Notification not = theNotificationListener.waitForNotification();
		assertNotNull(not);
		assertTrue("Notificationdoesn't contain text 'cannot be assigned'", not
				.getMessage().contains("cannot be assigned"));

	}

	@Test
	public void test24ErrorMessageForXThisOnStaticMethod() throws Exception {
		theNotificationListener.reset();

		Test24.test24();

		Notification lastNotification = theNotificationListener
				.waitForNotification();
		assertNotNull(lastNotification);

	}

	@Test
	public void test27ErrorMessageForXClassWithoutPublicConstructor()
			throws Exception {
		theNotificationListener.reset();
		try {
			Test27 test27 = new Test27();
			test27.test27();
		} catch (BootstrapMethodError e) {
			// today: throws exception. Would be better to inject a dummy method
			// with fitting signature...

		}

		Notification lastNotification = theNotificationListener
				.waitForNotification();
		assertNotNull(lastNotification);
		assertTrue(lastNotification.getMessage().contains(
				NotificationMessages.MESSAGE_MISSING_NO_ARGUMENT_CONSTRUCTOR));

	}

	@Test
	public void testcommonSuperProblem() throws Exception {

		CSCore cscore = new CSCore();
		cscore.doit();
		assertTrue(cscore.success);

	}

	@Test
	public void verifyErrorTest() throws Exception {
		// just asserts that there is no exception while instrumenting
		// the OpenResourceAction.class. (This uses the VerifyErrorScript rule
		// which injects in all IResourceChangeListeners)
		Class<?> c = OpenResourceAction.class;
		Class<?> c2 = Display.class;
		Class<?> jsv = JavaSourceViewerConfiguration.class;
		assertTrue(true);
	}

	@Test
	public void test39ErrorMessageForInvalidReturnTypeOnReplaceInvoke()
			throws Exception {
		theNotificationListener.reset();
		try {
			Test39 test39 = new Test39();
			test39.test39();
		} catch (Throwable e) {

		}

		Notification lastNotification = theNotificationListener
				.waitForNotification();
		assertNotNull(lastNotification);
		assertTrue(lastNotification.getMessage().contains(
				"REPLACE_INVOCATION requires that the return type"));

	}

	@Test
	public void test42ErrorMessageForXInvokeReturnOnExit() throws Exception {

		assertTrue(errorsDuringInstallErrors
				.messageContains(
						"Test42",
						"test42",
						"The method specifies @XInvokeReturn. This is only allowed for location AFTER_INVOCATION and REPLACE_INVOCATION"));

	}

	@Test
	public void test43ErrorMessageForXReturnOnAfterInvocation()
			throws Exception {

		assertTrue(errorsDuringInstallErrors
				.messageContains(
						"Test43",
						"test43",
						"The method specifies @XReturn. This is only allowed for the location EXIT. This annotation will be ignored."));

	}

}
