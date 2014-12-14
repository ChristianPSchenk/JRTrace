/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.service.test.utils.JavaUtil;
import de.schenk.jrtrace.service.test.utils.TestUtils;

public class JRTraceConnectToLaunchedAgentTest {

	private JavaUtil javaUtil = new JavaUtil();
	private JRTraceController bmController;
	private int port;

	@Before
	public void setUp() throws Exception {

		bmController = JRTraceControllerService.getInstance();
	}

	@Test
	public void testConnectToVM() throws Exception {

		port = javaUtil.launchJavaProcessWithAgent();
		IJRTraceVM theMachine = bmController.getMachine(port);

		SynchronousWaitListener waiter = attachToMachineAndInstallTestProcessInstrumenter(theMachine);
		waiter.waitForDone(10);
		assertNotNull("msg", waiter.getResult());
		assertTrue(theMachine.detach());

	}

	private SynchronousWaitListener attachToMachineAndInstallTestProcessInstrumenter(
			IJRTraceVM theMachine) {
		assertTrue(theMachine.attach());
		assertNotNull(theMachine);

		SynchronousWaitListener waiter = new SynchronousWaitListener(
				theMachine, TraceSender.TRACECLIENT_TESTMESSAGES_ID, "msg");

		File theClass = TestUtils
				.getResource("bin/de/schenk/jrtrace/service/test/utils/TestProcessInstrumenter.class");

		theMachine.installEngineXClass(theClass.getAbsolutePath());
		return waiter;
	}

	@Test
	public void testConnectTwiceInARow() throws Exception {

		port = javaUtil.launchJavaProcessWithAgent();
		IJRTraceVM mach = bmController.getMachine(port);

		assertTrue(mach.attach());

		assertTrue(mach.detach());

		SynchronousWaitListener waiter = attachToMachineAndInstallTestProcessInstrumenter(mach);

		waiter.waitForDone(10);
		assertEquals("msg", waiter.getResult());
		assertTrue(mach.detach());

	}

	@After
	public void tearDown() throws Exception {
		javaUtil.sendKillAndWaitForEnd();
	}

}
