/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.reinstall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.test.utils.JavaUtil;

/**
 * 
 * Installs two different versions of a class and ensures that the changes
 * become active
 * 
 * @author Christian Schenk
 *
 */
public class SimpleReinstallTest {

	protected Throwable exception;
	protected Object theFamily = new Object();

	/**
	 * Test that: first clears all JRTrace classes. Installs a firs
	 * 
	 * @throws Exception
	 */
	@Test
	public void testModifiedScriptsAreWorkingProperly() throws Exception {
		
		machine.clearEngineX();
		assertEquals(0, new SimpleReinstallClassUnderTest().method());

		byte[][] classBytes = getClassBytesA();

		machine.installJRTraceClasses(classBytes);

		assertEquals(1, new SimpleReinstallClassUnderTest().method());

		byte[][] classBytesB = getClassBytesB();
		if (classBytesB[0].length == classBytes[0].length) {
			boolean diff = false;
			for (int i = 0; i < classBytesB[0].length; i++)
				if (classBytes[0][i] != classBytesB[0][i]) {
					diff = true;
					break;
				}
			if (!diff)
				fail("same classbytes for a and b");
		}
		machine.installJRTraceClasses(classBytesB);
		assertEquals(2, new SimpleReinstallClassUnderTest().method());

	}

	long total = 0;
	private JRTraceController bmController;
	private IJRTraceVM machine;
	private String pid;

	@After
	public void after() throws Exception {
		assertTrue(machine.detach());
	}

	@Before
	public void before() throws Exception {

		exception = null;
		bmController = JRTraceControllerService.getInstance();
		String javaName = ManagementFactory.getRuntimeMXBean().getName();
		String[] javaSplit = javaName.split("@");
		pid = javaSplit[0];
		machine = bmController.getMachine(pid, null);
		assertTrue(machine.attach());
		// machine.setLogLevel(JRLog.DEBUG);

	}

	private byte[][] getClassBytesB() throws Exception {
		Bundle bundle = Platform.getBundle("de.schenk.jrtrace.service.test");

		return getClassBytes(bundle);
	}

	private byte[][] getClassBytesA() throws Exception {
		Bundle bundle = Platform
				.getBundle("de.schenk.jrtrace.service.enginex.testclasses");

		return getClassBytes(bundle);
	}

	private byte[][] getClassBytes(Bundle bundle) throws Exception {

		byte[] classFileBytes = new JavaUtil().getClassBytes(bundle
				.loadClass(SimpleReinstallScript.class.getName()));

		byte[][] classBytes = new byte[1][];
		classBytes[0] = classFileBytes;
		return classBytes;
	}

}
