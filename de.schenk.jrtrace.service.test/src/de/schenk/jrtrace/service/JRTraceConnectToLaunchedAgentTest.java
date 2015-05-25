/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.test.utils.JavaUtil;
import de.schenk.jrtrace.service.test.utils.TestProcessCommunication;
import de.schenk.jrtrace.service.test.utils.TestProcessInstrumenter;

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

		File tempFile = File.createTempFile("abc", "def");
		tempFile.deleteOnExit();
		port = javaUtil.launchJavaProcessWithAgent(tempFile.getAbsolutePath());
		IJRTraceVM theMachine = bmController.getMachine(port, null);

		attachToMachineAndInstallTestProcessInstrumenter(theMachine);

		if (!waitForCorrectValueInFile(tempFile.getAbsolutePath(), 1, 10000))
			fail("not the proper value");

		assertTrue(theMachine.detach());

		int x = JavaUtil.readIntegerFromFile(tempFile.getAbsolutePath());
		assertEquals(1, x);

	}

	private void attachToMachineAndInstallTestProcessInstrumenter(
			IJRTraceVM theMachine) throws IOException, InterruptedException {
		attachToMachineAndInstallJRTraceClass(theMachine,
				TestProcessInstrumenter.class);
	}

	private void attachToMachineAndInstallJRTraceClass(IJRTraceVM theMachine,
			Class<?> clazz) throws IOException, InterruptedException {
		boolean result = false;
		for (int i = 0; i < 10; i++) {
			result = theMachine.attach();
			if (result)
				break;
			Thread.sleep(100);
		}
		assertTrue(result);
		assertNotNull(theMachine);

		File theClass = (new JavaUtil()).getFileForClass(clazz,
				"de.schenk.jrtrace.service.test");

		byte[][] classBytes = new byte[1][];
		classBytes[0] = Files.readAllBytes(Paths.get(theClass.toURI()));
		assertTrue(theMachine.installJRTraceClasses(classBytes));

	}

	@Test
	public void testTwoWayCommunication() throws Exception {

		port = javaUtil.launchJavaProcessWithAgent(null);
		IJRTraceVM mach = bmController.getMachine(port, null);
		attachToMachineAndInstallJRTraceClass(mach,
				TestProcessCommunication.class);

		final CyclicBarrier barrier = new CyclicBarrier(2);

		final String[] receivedData = new String[1];
		final int[] receivedInt = new int[1];

		NotificationListener streamReceiver = new NotificationListener() {

			@Override
			public void handleNotification(Notification notification,
					Object handback) {
				AttributeChangeNotification anot = (AttributeChangeNotification) notification;
				Object userData = anot.getOldValue();
				if (userData instanceof Object[]) {
					Object[] objarr = (Object[]) userData;
					receivedData[0] = (String) objarr[0];
					receivedInt[0] = (int) objarr[1];
				}

				try {
					barrier.await();
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} catch (BrokenBarrierException e) {
					throw new RuntimeException(e);
				}

			}
		};
		mach.addClientListener(NotificationConstants.NOTIFY_MESSAGE,
				streamReceiver);
		if (!mach
				.invokeMethodAsync(
						null,
						"de.schenk.jrtrace.service.test.utils.TestProcessCommunication",
						"callTrigger", null) /*
											 * on purpose using null as argument
											 * here to test that this is
											 * properly wrapped to a
											 * Object[]{null}
											 */) {
			mach.getLastError().printStackTrace();
			fail("runjava callTrigger failed");
		}

		boolean result = mach
				.invokeMethodAsync(
						null,
						"de.schenk.jrtrace.service.test.utils.TestProcessCommunication",
						"called", "ping", new int[] { 1, 2 });
		if (!result) {
			mach.getLastError().printStackTrace();
			fail("runjava failed");
		}
		barrier.await();

		assertEquals("pong", receivedData[0]);
		assertTrue(mach.detach());
	}

	@Test
	public void testConnectTwiceInARow() throws Exception {
		File tempFile = File.createTempFile("abc", "def");
		tempFile.deleteOnExit();

		port = javaUtil.launchJavaProcessWithAgent(tempFile.getAbsolutePath());
		IJRTraceVM mach = bmController.getMachine(port, null);

		boolean result = false;
		for (int i = 0; i < 10; i++) {
			result = mach.attach();
			if (result)
				break;
			Thread.sleep(100);
		}
		Throwable lastException = mach.getLastError();
		if (lastException != null)
			lastException.printStackTrace();
		assertTrue(result);

		assertTrue(mach.detach());

		attachToMachineAndInstallTestProcessInstrumenter(mach);

		if (!waitForCorrectValueInFile(tempFile.getAbsolutePath(), 1, 10000))
			fail("not the proper value");

		assertTrue(mach.detach());

	}

	/**
	 * Wait for the value value to appear in the file absolutePath and fail
	 * after timeout
	 * 
	 * @param absolutePath
	 * @param value
	 * @param timeout
	 */
	private boolean waitForCorrectValueInFile(String absolutePath, int value,
			int timeout) throws Exception {

		long starttime = System.currentTimeMillis();
		while (starttime + 10000 > System.currentTimeMillis()) {
			try {
				int x = JavaUtil.readIntegerFromFile(absolutePath);
				if (x == value)
					return true;
			} catch (Exception e) {
				Thread.sleep(10);
				continue;
			}

		}
		return false;

	}

	@After
	public void tearDown() throws Exception {
		javaUtil.sendKillAndWaitForEnd();
	}

}
