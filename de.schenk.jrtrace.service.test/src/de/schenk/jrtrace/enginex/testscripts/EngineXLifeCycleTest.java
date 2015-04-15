/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.assertFalse;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.test.utils.JavaUtil;
import de.schenk.jrtrace.ui.util.JarByteUtil;

public class EngineXLifeCycleTest {

	private JRTraceController bmController;
	private String pid;
	private IJRTraceVM machine;
	private JavaUtil javaUtil = new JavaUtil();

	@After
	public void tearDown() throws Exception {

		machine.detach();
	}

	@Before
	public void setUp() throws Exception {

		bmController = JRTraceControllerService.getInstance();
		String javaName = ManagementFactory.getRuntimeMXBean().getName();
		String[] javaSplit = javaName.split("@");
		pid = javaSplit[0];
		machine = bmController.getMachine(pid, null);
		assertTrue(machine.attach());

	}

	/**
	 * org.eclipse.core.jobs.Job is class version V1_6 (probably like most
	 * Eclipse classes and also others). To use InvokeDynamic we need source
	 * level V1_7. Test that it works.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testClassVersion16() throws Exception {
		instrumented = false;

		DoneListener doneListener = createDoneListener();

		URL url = getFileForClassFromTestClassesBundle("JobInstrument.class");
		byte[][] classBytes = new byte[1][];
		classBytes[0] = getBytesFromURL(url);
		machine.installEngineXClass(classBytes);

		Job c = new Job("test") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				System.out.println("job");
				return null;
			}

		};

	}

	private byte[] getBytesFromURL(URL fileURL) throws URISyntaxException,
			IOException {

		File fullPath = new File(FileLocator.toFileURL(fileURL).toURI());
		byte[] bytes = Files.readAllBytes(Paths.get(fullPath.toURI()));
		return bytes;
	}

	@Test
	public void testBasicEnginexjarinstall() throws Exception {
		instrumented = false;

		DoneListener doneListener = createDoneListener();

		Bundle bundle = Platform
				.getBundle("de.schenk.jrtrace.service.enginex.testclasses");

		URL fileURL = FileLocator.find(bundle,
				new Path("lib/EngineXTests.jar"), null);

		byte[] jarBytes = getBytesFromURL(fileURL);
		machine.installEngineXClass(JarByteUtil
				.convertJarToClassByteArray(jarBytes));

		InstrumentedClass2 c = new InstrumentedClass2();
		c.doit();
		assertTrue(c.getResult());

	}

	public static boolean instrumented;

	/**
	 * asserts that it is possible to remove a script again even if the rule
	 * usedForNames have been changed by the user.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testBasicEngineX() throws Exception {
		instrumented = false;

		DoneListener doneListener = createDoneListener();

		URL url = getFileForClassFromTestClassesBundle("EngineXTestClass.class");

		byte[][] classBytes = new byte[1][];
		classBytes[0] = getBytesFromURL(url);
		machine.installEngineXClass(classBytes);

		// InstrumentedClass is not loaded yet after installing the enginex
		// script.
		assertFalse(LoadCheck.instrumentedClassLoaded);
		InstrumentedClass c = new InstrumentedClass();
		c.doit();
		assertTrue(c.getResult());

		// Ensure that clearing removes the instrumentation
		InstrumentedClass.x = false;
		DoneListener doneListener2 = createDoneListener();
		machine.clearEngineX();

		c.doit();
		assertFalse(c.getResult());

		// InstrumentedClass is already loaded. Will XClass still work?
		InstrumentedClass c2 = new InstrumentedClass();
		DoneListener doneListener3 = createDoneListener();
		machine.installEngineXClass(classBytes);

		c2.doit();
		assertTrue(c2.getResult());

	}

	public URL getFileForClassFromTestClassesBundle(String className) {
		URL url = null;
		Bundle bundle = Platform
				.getBundle("de.schenk.jrtrace.service.enginex.testclasses");

		Enumeration<URL> allEntries = bundle.findEntries("/", className, true);

		url = allEntries.nextElement();
		return url;
	}

	public DoneListener createDoneListener() {

		return new DoneListener();
	}
}
