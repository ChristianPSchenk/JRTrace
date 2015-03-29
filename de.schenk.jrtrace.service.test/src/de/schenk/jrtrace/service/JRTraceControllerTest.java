/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.internal.JRTraceVMImpl;
import de.schenk.jrtrace.service.test.utils.JavaUtil;
import de.schenk.jrtrace.service.test.utils.TestProcessRegexScript;
import de.schenk.objectweb.asm.addons.ClassByteUtil;

public class JRTraceControllerTest {

	public class ErrorReceiver implements NotificationListener {

		boolean error = false;

		@Override
		public void handleNotification(Notification notification,
				Object handback) {
			error = true;

		}

		public boolean hadErrors() {
			return error;
		}

	}

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

		IJRTraceVM mach = bmController.getMachine(vm.getId(), null);
		JRTraceVMImpl theMachine = (JRTraceVMImpl) mach;
		assertTrue(theMachine.attach());
		assertNotNull(theMachine);
		assertTrue(theMachine.detach());
	}

	@Test
	public void testUploadJRTraceAgent() throws Exception {
		VMInfo vm = getJavaVMInfo();

		IJRTraceVM mach = bmController.getMachine(vm.getId(), null);
		assertTrue(mach.attach());

		assertTrue(mach.detach());

	}

	/**
	 * Test to ensure that no exception is thrown due to
	 * ClassCircularityException when loading the Pattern class.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInstallAScriptWithRegex() throws Exception {
		VMInfo vm = getJavaVMInfo();

		IJRTraceVM mach = bmController.getMachine(vm.getId(), null);

		assertTrue(mach.attach());

		mach.setLogLevel(JRLog.DEBUG);
		ErrorReceiver receiver = new ErrorReceiver();
		mach.addClientListener(NotificationConstants.NOTIFY_ERROR, receiver);

		byte[][] bytes = new byte[1][];
		bytes[0] = ClassByteUtil.getBytes(TestProcessRegexScript.class);

		mach.installEngineXClass(bytes);

		System.out.println("done");

		assertTrue(mach.detach());

		mach.removeClientListener(NotificationConstants.NOTIFY_ERROR, receiver);

		assertFalse(receiver.hadErrors());
	}

	@Test
	public void testUploadJRTraceAgentTwice() throws Exception {
		VMInfo vm = getJavaVMInfo();

		IJRTraceVM mach = bmController.getMachine(vm.getId(), null);
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
