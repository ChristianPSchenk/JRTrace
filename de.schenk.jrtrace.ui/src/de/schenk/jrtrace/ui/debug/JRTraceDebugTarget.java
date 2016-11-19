/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.debug;

import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
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
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.ui.JRTraceUIActivator;
import de.schenk.jrtrace.ui.markers.JRTraceMarkerManager;
import de.schenk.jrtrace.ui.util.JarByteUtil;
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
	private IProject theProject;

	public JRTraceDebugTarget(IJRTraceVM vm, ILaunch launch,
			final IProject theProject, boolean uploadHelperOnConnect) {
		super(null);
		this.launch = launch;

		machine = vm;
		process = new JRTraceProcess(this);
		createConsole();

		markerManager = new JRTraceMarkerManager(this);

		this.theProject = theProject;
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
		JRTraceConsole = new JRTraceConsoleConnector();
		JRTraceConsole.start(this);

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
		try {
			disconnect();
		} finally {
			JRTraceConsole.close();
			isTerminated = true;
			fireEvent(new DebugEvent(this, DebugEvent.TERMINATE));
		}

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

		return machine.toString();
	}

	@Override
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {

		return false;
	}

	public void installJar(byte[] bytes) {

		if (!machine.installJar(bytes)) {
			disconnectAfterConnectionProblem();
		}

	}

	/**
	 * 
	 * @param theClassLoader
	 *            for classloaderpolicy TARGET: the name of the class to use for
	 *            the invocation.
	 * @param className
	 *            name of the class to invoke a method on
	 * @param methodName
	 *            static method name (void void)
	 */
	public void runJava(String theClassLoader, final String className,
			final String methodName) {
		if (!machine.invokeMethodAsync(theClassLoader, className, methodName)) {

			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MultiStatus m = createStatusFromThrowableForDialog("Error during java call.");
					Shell shell = Display.getDefault().getShells()[0];
					ErrorDialog.openError(shell, "Execution Problem",
							"It was not possible to run the method "
									+ methodName + " of class " + className
									+ " on the target.", m);

				}

			});
		}

	}

	private void disconnectAfterConnectionProblem() {

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				
				MultiStatus m = createStatusFromThrowableForDialog("Connection to target lost.");
				ErrorDialog
						.openError(
								Display.getDefault().getActiveShell(),
								"Connection Problem",
								"The connection to the target machine "
										+ pid
										+ " is broken. Disconnecting from target.",
								m);

			}

		});
		try {
			disconnect();
		} catch (DebugException e) {
			throw new RuntimeException(e);
		}

	}

	public void installEngineX(File jarFile) {
		markerManager.clearAllMarkers();

		byte[][] classFileBytes = JarByteUtil
				.convertJarToClassByteArray(jarFile);
		if (!machine.installJRTraceClasses(classFileBytes)) {
			disconnectAfterConnectionProblem();
		}

	}

	public IProject getProject() {
		return theProject;

	}

	/**
	 * create a multistatus taking into accound that some exceptions have additional stacktraces 
	 * in other places. (UndeclaredThrowableException).
	 * 
	 * @param msg
	 * @return
	 */
	private MultiStatus createStatusFromThrowableForDialog(String msg) {
		MultiStatus m;
		m=new MultiStatus(
				de.schenk.jrtrace.ui.JRTraceUIActivator.BUNDLE_ID,
				IStatus.ERROR, msg, machine
						.getLastError());
		Throwable t= machine.getLastError();
		if(t!=null) m.add(createMultiStatus("Last Exception Thrown:",t));
		
		if (machine.getLastError() instanceof UndeclaredThrowableException) {
			
			Throwable ex = ((UndeclaredThrowableException)t).getUndeclaredThrowable();
			if(ex!=null)
			m.add(createMultiStatus("Undeclared Throwable:", ex));

		} 
		return m;
	}
	
	/**
	 * Create a multistatus with the specified message and the substatus such that they show the throwable stacktrace
	 * suitable for showing in an ErrorDialog
	 * @param msg
	 * @param t
	 * @return
	 */
	private static MultiStatus createMultiStatus(String msg, Throwable t) {

		List<Status> childStatuses = new ArrayList<>();
        

        for (StackTraceElement tr: t.getStackTrace()) {
               Status status = new Status(IStatus.ERROR,
                               JRTraceUIActivator.BUNDLE_ID, tr.toString());
               childStatuses.add(status);
       }

        MultiStatus ms = new MultiStatus(JRTraceUIActivator.BUNDLE_ID,
                        IStatus.ERROR, childStatuses.toArray(new Status[0]),
                        msg, t);
        return ms;
}

}
