/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.management.ManagementFactory;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.actions.OpenResourceAction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;

import de.schenk.jrtrace.commonsuper.test.CSCore;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;

/**
 * install the test enginex jar once , execute all tests and then detach.
 * 
 * @author
 *
 */
public class EngineXDetailsTest {

	private JRTraceController bmController;
	private String pid;
	private IJRTraceVM machine;

	@After
	public void after() throws Exception {
		machine.detach();
	}

	@Before
	public void before() throws Exception {

		bmController = JRTraceControllerService.getInstance();
		String javaName = ManagementFactory.getRuntimeMXBean().getName();
		String[] javaSplit = javaName.split("@");
		pid = javaSplit[0];
		machine = bmController.getMachine(pid);
		assertTrue(machine.attach());
		machine.setLogLevel(JRLog.DEBUG);

		DoneListener doneListener = new DoneListener();

		Bundle bundle = Platform
				.getBundle("de.schenk.jrtrace.service.enginex.testclasses");

		URL fileURL = FileLocator.find(bundle,
				new Path("lib/EngineXTests.jar"), null);
		String fullPath = FileLocator.resolve(fileURL).toURI().toASCIIString()
				.replace("file:/", "");
		machine.installEngineXClass(fullPath);

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
	public void test8() throws Exception {

		Test8 test8 = new Test8();

		String msg = "";

		try {
			test8.test8(1, 2.0D, "abc");
		} catch (RuntimeException c) {
			msg = c.getMessage();
		}
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
		assertEquals("", msg);
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
		assertTrue(true);
	}

}
