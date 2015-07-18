/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.reinstall;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;

/**
 * 
 * - Instrument a bunch of classes, use them heavily and at the same time
 * perform frequent reinstalls - also: switch between different incompatible
 * versions of JRTrace scripts
 * 
 * @author Christian Schenk
 *
 */
public class ReinstallStressTest {

	private static final int REPEAT = 10;
	protected Throwable exception;
	protected Object theFamily = new Object();

	@Test
	public void frequentReinstallSameTest() throws Exception {

		Job[] workers = startWorkerJobToDoALotOfWork();

		byte[][] classBytes = getClassBytesA();

		waitForAllJobsRunning(workers);

		for (int i = 0; i < 10 * REPEAT; i++) {
			System.out.println(i);
			machine.installJRTraceClasses(classBytes);
			if (oneWorkerHasStopped(workers)) {
				System.out.println(String.format(
						"Worker not running after %d iterations of reinstall",
						i));
				break;
			}
		}

		cancelall(workers);
		Job.getJobManager().join(theFamily, null);
		if (exception != null)

			throw new RuntimeException("Strange exceptions in worker thread.",
					exception);
	}

	private void cancelall(Job[] workers) {
		for (Job w : workers) {
			w.cancel();
		}

	}

	private boolean oneWorkerHasStopped(Job[] workers) {
		for (Job w : workers) {
			if (w.getState() != Job.RUNNING)
				return true;
		}
		return false;
	}

	private void waitForAllJobsRunning(Job[] workers)
			throws InterruptedException {
		for (Job worker : workers) {
			while (worker.getState() != Job.RUNNING) {
				Thread.sleep(100);
			}
		}
	}

	/**
	 * installs jrtrace scripts that have the same name and are injected into
	 * the same classes but have incompatible signatures.
	 * 
	 * @throws Exception
	 */
	@Test
	public void frequentReinstallIncompatibleVersions() throws Exception {

		Job[] workers = startWorkerJobToDoALotOfWork();

		byte[][] classBytesA = getClassBytesA();
		byte[][] classBytesB = getClassBytesB();

		waitForAllJobsRunning(workers);

		for (int i = 0; i < 5 * REPEAT; i++) {
			System.out.println(i);
			machine.installJRTraceClasses(classBytesB);
			machine.installJRTraceClasses(classBytesA);
			if (oneWorkerHasStopped(workers)) {
				System.out.println(String.format(
						"Worker not running after %d iterations of reinstall",
						i));
				break;
			}
		}

		cancelall(workers);
		while (true) {
			try {
				Job.getJobManager().join(theFamily, null);
				break;
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		if (exception != null)

			throw new RuntimeException("Strange exceptions in worker thread.",
					exception);
	}

	@Test
	public void frequentReinstallAndClearTest() throws Exception {

		Job[] workers = startWorkerJobToDoALotOfWork();

		byte[][] classBytes = getClassBytesA();
		waitForAllJobsRunning(workers);

		for (int i = 0; i < 5 * REPEAT; i++) {
			System.out.println(i);

			machine.clearEngineX();
			machine.installJRTraceClasses(classBytes);

			Thread.sleep(1);
			if (oneWorkerHasStopped(workers)) {
				System.out
						.println(String
								.format("Worker not running after %d iterations of clear/install",
										i));
				break;
			}
		}

		cancelall(workers);
		Job.getJobManager().join(theFamily, null);
		if (exception != null)

			throw new RuntimeException("Strange exceptions in worker thread.",
					exception);
	}

	private Job[] startWorkerJobToDoALotOfWork() {
		int njobs = 10;
		Job[] workers = new Job[njobs];
		for (int i = 0; i < njobs; i++) {
			Job worker = new Job("do a lot of Work") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						doWork(monitor);
					} catch (Throwable t) {

						exception = t;
					}
					return Status.OK_STATUS;
				}

				@Override
				public boolean belongsTo(Object family) {
					return family == theFamily;
				}

			};

			worker.setPriority(Job.SHORT);
			worker.schedule();
			workers[i] = worker;
			;
		}
		return workers;
	}

	long total = 0;
	private JRTraceController bmController;
	private IJRTraceVM machine;
	private String pid;

	private void doWork(IProgressMonitor monitor) {
		while (true) {
			Set<ReinstallBaseClass> set = new HashSet<ReinstallBaseClass>();
			while (set.size() < 100) {
				int x = (int) (Math.random() * 5);
				switch (x) {
				case 0:
					set.add(new ReinstallBaseClass());
					break;
				case 1:
					set.add(new ReinstallClass1());
					break;
				case 2:
					set.add(new ReinstallClass2());
					break;
				case 3:
					set.add(new ReinstallClass3());
					break;
				case 4:
					set.add(new ReinstallClass4());
					break;

				}
				if (monitor.isCanceled()) {
					System.out.println("worker canceldded");
					return;
				}
				total += workOn(set);
				Set<ReinstallBaseClass> remaining = new HashSet<ReinstallBaseClass>();
				for (ReinstallBaseClass s : set) {
					if (Math.random() * 100.0 > 50) {
						remaining.add(s);
					}
				}
				set = remaining;
			}

		}

	}

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
				"ReinstallScript.class", true);

		URL fileURL = entries.nextElement();
		File theFile = new File(FileLocator.toFileURL(fileURL).toURI());

		byte[] jarBytes = Files.readAllBytes(Paths.get(theFile.toURI()));
		byte[][] classBytes = new byte[1][];
		classBytes[0] = jarBytes;
		return classBytes;
	}

	private long workOn(Set<ReinstallBaseClass> set) {
		long result = 0;
		for (ReinstallBaseClass x : set) {
			if (Math.random() * 100 < 50) {
				result += x.call((int) (result % 100));
				x.method();
			}
		}

		return result;
	}
}
