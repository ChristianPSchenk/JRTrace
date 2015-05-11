package de.schenk.jrtrace.enginex.reinstall;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;

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

	@Test
	public void testModifiedScriptsAreWorkingProperly() throws Exception {

		machine.clearEngineX();
		assertEquals(0, new SimpleReinstallClassUnderTest().method());

		byte[][] classBytes = getClassBytesA();

		machine.installEngineXClass(classBytes);

		assertEquals(1, new SimpleReinstallClassUnderTest().method());

		byte[][] classBytesB = getClassBytesB();
		machine.installEngineXClass(classBytesB);

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

	private byte[][] getClassBytesB() throws URISyntaxException, IOException {
		Bundle bundle = Platform.getBundle("de.schenk.jrtrace.service.test");

		return getClassBytes(bundle);
	}

	private byte[][] getClassBytesA() throws URISyntaxException, IOException {
		Bundle bundle = Platform
				.getBundle("de.schenk.jrtrace.service.enginex.testclasses");

		return getClassBytes(bundle);
	}

	private byte[][] getClassBytes(Bundle bundle) throws URISyntaxException,
			IOException {
		Enumeration<URL> entries = bundle.findEntries("/",
				"SimpleReinstallScript.class", true);

		URL fileURL = entries.nextElement();
		File theFile = new File(FileLocator.toFileURL(fileURL).toURI());

		byte[] jarBytes = Files.readAllBytes(Paths.get(theFile.toURI()));
		byte[][] classBytes = new byte[1][];
		classBytes[0] = jarBytes;
		return classBytes;
	}

}
