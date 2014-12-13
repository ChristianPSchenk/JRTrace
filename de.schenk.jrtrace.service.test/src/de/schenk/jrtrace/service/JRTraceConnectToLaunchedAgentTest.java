/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schenk.jrtrace.service.test.utils.JavaUtil;

public class JRTraceConnectToLaunchedAgentTest {

	private JavaUtil javaUtil = new JavaUtil();
	private JRTraceController bmController;
	private int port;

	@After
	public void tearDown() throws Exception {
		javaUtil.sendKillAndWaitForEnd();
	}

	@Before
	public void setUp() throws Exception {
		bmController = JRTraceControllerService.getInstance();
		port = javaUtil.launchJavaProcessWithAgent();

	}

	@Test
	public void testConnectToVM() throws Exception {

		IJRTraceVM theMachine = bmController.getMachine(port);

		assertTrue(theMachine.attach());
		assertNotNull(theMachine);
		assertTrue(theMachine.detach());
	}

	@Test
	public void testConnectTwiceInARow() throws Exception {

		IJRTraceVM mach = bmController.getMachine(port);
		for (int i = 0; i < 2; i++) {
			assertTrue(mach.attach());

			assertTrue(mach.detach());
		}

	}

}
