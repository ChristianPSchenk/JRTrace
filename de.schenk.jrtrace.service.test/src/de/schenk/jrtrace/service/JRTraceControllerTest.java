/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schenk.jrtrace.service.internal.JRTraceVMImpl;
import de.schenk.jrtrace.service.test.utils.JavaUtil;

public class JRTraceControllerTest {

	private JavaUtil javaUtil = new JavaUtil();
	private JRTraceController bmController;

	@After
	public void tearDown() throws Exception {
		javaUtil.sendKillAndWaitForEnd();
	}

	@Before
	public void setUp() throws Exception {
		javaUtil.launchJavaProcess();
		bmController = JRTraceControllerService.getInstance();
	}

	@Test
	public void testCanFindVM() throws InterruptedException {
		getJavaVMInfo();

	}

	@Test
	public void testConnectToVM() throws Exception {
		VMInfo vm = getJavaVMInfo();

		IJRTraceVM mach = bmController.getMachine(vm.getId());
		JRTraceVMImpl theMachine = (JRTraceVMImpl) mach;
		assertTrue(theMachine.attach());
		assertNotNull(theMachine);
		assertTrue(theMachine.detach());
	}

	@Test
	public void testUploadJRTraceAgent() throws Exception {
		VMInfo vm = getJavaVMInfo();

		IJRTraceVM mach = bmController.getMachine(vm.getId());
		assertTrue(mach.attach());

		assertTrue(mach.detach());

	}

	@Test
	public void testUploadJRTraceAgentTwice() throws Exception {
		VMInfo vm = getJavaVMInfo();

		IJRTraceVM mach = bmController.getMachine(vm.getId());
		for (int i = 0; i < 2; i++) {
			System.out.println(i);
			boolean erg = mach.attach();
			if (!erg) {
				mach.getLastError().printStackTrace();
				fail(mach.getLastError().getMessage());
			}

			assertTrue(mach.detach());
		}

	}

	private VMInfo getJavaVMInfo() throws InterruptedException {
		boolean notfound = true;
		int tries = 0;
		VMInfo result = null;
		while (notfound) {
			VMInfo[] vms = bmController.getVMs();
			for (VMInfo vm : vms) {
				if (vm.getName().contains(javaUtil.getQualifiedTestClassName())) {
					notfound = false;
					result = vm;
					break;
				}
			}
			Thread.sleep(500);
			tries++;
			if (tries > 20)
				fail("The java process with name"
						+ javaUtil.getQualifiedTestClassName()
						+ " never appeared in the list of VMs");
		}
		return result;
	}

}
