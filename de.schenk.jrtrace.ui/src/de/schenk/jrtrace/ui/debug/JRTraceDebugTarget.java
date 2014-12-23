/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.debug;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.DebugElement;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.swt.widgets.Display;

import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.ui.markers.JRTraceMarkerManager;
import de.schenk.jrtrace.ui.util.JarUtil;

public class JRTraceDebugTarget extends DebugElement implements IDebugTarget {

	public static final String JRTRACE_DEBUG_MODEL = "bytemam.debug.model";
	private String pid;

	private IJRTraceVM machine;
	private ILaunch launch;
	private boolean isDisconnected = false;
	private boolean isTerminated = false;
	private JRTraceConsoleConnector JRTraceConsole;
	IProcess process;
	private JRTraceMarkerManager markerManager;

	public JRTraceDebugTarget(IJRTraceVM vm, ILaunch launch,
			final IProject theProject, boolean uploadHelperOnConnect) {
		super(null);
		this.launch = launch;

		machine = vm;
		process = new JRTraceProcess(this);
		createConsole();

		markerManager = new JRTraceMarkerManager(this);

		if (theProject != null) {
			if (uploadHelperOnConnect) {
				final File jarFile[] = new File[1];
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						jarFile[0] = JarUtil.createJar(theProject, Display
								.getDefault().getActiveShell());

					}

				});

				Job installEngineXJob = new InstallJRTraceJob(this, jarFile[0]);
				installEngineXJob.schedule();

			}

		}

	}

	private void createConsole() {
		JRTraceConsole = new JRTraceConsoleConnector(machine);
		JRTraceConsole.start();

	}

	public IJRTraceVM getJRTraceMachine() {
		return machine;
	}

	@Override
	public IDebugTarget getDebugTarget() {
		return this;
	}

	@Override
	public ILaunch getLaunch() {
		return launch;
	}

	@Override
	public String getModelIdentifier() {

		return JRTRACE_DEBUG_MODEL;
	}

	@Override
	public boolean canTerminate() {
		return true;
	}

	@Override
	public boolean isTerminated() {
		return isTerminated;
	}

	@Override
	public void terminate() throws DebugException {
		disconnect();
		JRTraceConsole.close();
		isTerminated = true;
		fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));

	}

	@Override
	public boolean canResume() {

		return false;
	}

	@Override
	public boolean canSuspend() {

		return false;
	}

	@Override
	public boolean isSuspended() {

		return false;
	}

	@Override
	public void resume() throws DebugException {

	}

	@Override
	public void suspend() throws DebugException {

	}

	@Override
	public void breakpointAdded(IBreakpoint breakpoint) {

	}

	@Override
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean canDisconnect() {
		return true;
	}

	@Override
	public void disconnect() throws DebugException {

		DetachJRTraceJob job = new DetachJRTraceJob(this);
		job.schedule();

		while (true)
			try {
				job.join();
				break;
			} catch (InterruptedException e) {
				//
			}

		JRTraceConsole.stop();

		if (markerManager != null) {
			markerManager.close();
			markerManager = null;
		}

		isDisconnected = true;

		fireEvent(new DebugEvent(this, DebugEvent.CHANGE));
	}

	@Override
	public boolean isDisconnected() {
		return isDisconnected;
	}

	@Override
	public boolean supportsStorageRetrieval() {

		return false;
	}

	@Override
	public IMemoryBlock getMemoryBlock(long startAddress, long length)
			throws DebugException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProcess getProcess() {
		return process;
	}

	@Override
	public IThread[] getThreads() throws DebugException {
		return new IThread[0];
	}

	@Override
	public boolean hasThreads() throws DebugException {

		return false;
	}

	@Override
	public String getName() throws DebugException {

		return machine.getPID();
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {

		return false;
	}

	public void installJar(File jar) {

		machine.installJar(jar.toString());

	}

	public void installJar(IFile jar) {
		try {
			installJar(new File(FileLocator.toFileURL(
					jar.getLocationURI().toURL()).toURI()));
		} catch (MalformedURLException e) {
			throw new RuntimeException(
					"Error installing jar " + jar.toString(), e);
		} catch (IOException e) {
			throw new RuntimeException(
					"Error installing jar " + jar.toString(), e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(
					"Error installing jar " + jar.toString(), e);
		}

	}

	/**
	 * runs the supplied groovy script on the target
	 * 
	 * @param f
	 *            the groovy script to run
	 * @param classLoaderClass
	 *            the class to use to determine the classloader for the script
	 */
	public void runGroovy(IFile f, String classLoaderClass) {

		machine.runGroovy(f.getLocation().toOSString(), classLoaderClass);
	}

	public void runJava(File jarFile, String theClassLoader, String className,
			String methodName) {
		machine.runJava(jarFile, theClassLoader, className, methodName);

	}

	public void installEngineX(File jarFile) {
		markerManager.clearAllMarkers();
		machine.installEngineXClass(jarFile.getAbsolutePath());

	}

}
