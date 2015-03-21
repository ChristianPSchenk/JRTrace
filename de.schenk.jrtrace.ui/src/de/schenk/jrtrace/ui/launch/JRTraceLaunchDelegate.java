/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.launch;

import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.jdk.init.Activator;
import de.schenk.jrtrace.service.ICancelable;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.VMInfo;
import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;

public class JRTraceLaunchDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		boolean upload = configuration.getAttribute(
				ConnectionTab.BM_UPLOADAGENT, false);
		IJRTraceVM vm = null;
		if (upload) {
			if (!Activator.hasJDK()) {
				showNoJDKError();
				throw new CoreException(Status.CANCEL_STATUS);
			}
			vm = launchPID(launch, monitor);

		} else {
			vm = launchPort(launch, monitor);
		}

		int level = JRLog.ERROR;
		if (configuration.getAttribute(ConnectionTab.BM_VERBOSE, false))
			level = JRLog.VERBOSE;
		if (configuration.getAttribute(ConnectionTab.BM_DEBUG, false))
			level = JRLog.DEBUG;
		vm.setLogLevel(level);
	}

	private void showNoJDKError() {
		final String javahome = System.getProperty("java.home");
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog
						.openError(
								null,
								"JRTrace: No JDK!",
								"To trace a target java application without launching it with -javaagent:... parameters, the development environment needs to be launched using a JDK. No JDK at "
										+ javahome + ".");

			}
		});

	}

	public IJRTraceVM launchPort(ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		ILaunchConfiguration launchconfig = launch.getLaunchConfiguration();
		int port;
		port = launchconfig.getAttribute(ConnectionTab.BM_UPLOADAGENT_PORT, 0);
		String targetmachine = launchconfig.getAttribute(
				ConnectionTab.BM_SERVER_MACHINE, "");
		if (targetmachine.isEmpty())
			targetmachine = null;
		JRTraceController controller = JRTraceControllerService.getInstance();
		final IJRTraceVM machine = controller.getMachine(port, targetmachine);
		if (runTarget(launch, machine, monitor))
			return machine;
		showUnableToConnectDialog(String.format("Machine %s on Port %d",
				targetmachine == null ? "localhost" : targetmachine, port),
				machine);

		throw new CoreException(Status.CANCEL_STATUS);

	}

	private boolean runTarget(ILaunch launch, final IJRTraceVM machine,
			final IProgressMonitor monitor) throws CoreException {
		final String projectName = launch.getLaunchConfiguration()
				.getAttribute(ConnectionTab.BM_PROJECT_IDENT, "");
		IProject theProject = getProject(projectName);

		ICancelable stopper = new ICancelable() {

			@Override
			public boolean isCanceled() {

				return monitor.isCanceled();
			}

		};

		if (machine.attach(stopper)) {

			boolean uploadHelperOnConnect = launch.getLaunchConfiguration()
					.getAttribute(ConnectionTab.BM_AUTOUPLOAD, false);
			final JRTraceDebugTarget dbt = new JRTraceDebugTarget(machine,
					launch, theProject, uploadHelperOnConnect);

			launch.addDebugTarget(dbt);

			machine.addFailListener(new Runnable() {

				@Override
				public void run() {
					try {
						dbt.disconnect();
					} catch (DebugException e) {
						throw new RuntimeException(e);
					}

				}
			});

			return true;

		}
		return false;
	}

	public IJRTraceVM launchPID(ILaunch launch, IProgressMonitor monitor)
			throws CoreException {

		ILaunchConfiguration launchconfig = launch.getLaunchConfiguration();
		String pid;
		pid = launchconfig.getAttribute(ConnectionTab.BM_PID, "");
		final String identifyText = launchconfig.getAttribute(
				ConnectionTab.BM_TEXT_IDENT, "");

		if (launchconfig.getAttribute(ConnectionTab.BM_AUTOCONNECT, false)) {
			String startedProcess = waitForStartedProcess(monitor);
			if (monitor.isCanceled())
				throw new CoreException(Status.CANCEL_STATUS);
			if (startedProcess != null) {
				pid = startedProcess;
			}
		}
		if ((pid == null || pid.isEmpty())) {
			pid = chooseProperProcess(identifyText);
		}

		List<JRTraceDebugTarget> jrtraceTargets = JRTraceLaunchUtils
				.getJRTraceDebugTargets();
		for (JRTraceDebugTarget btarget : jrtraceTargets) {
			if (btarget.getJRTraceMachine().getPID().equals(pid)) {
				final String pidCopy = pid;
				showProcessAlreadyConnectedDialog(pidCopy);
				throw new CoreException(Status.CANCEL_STATUS);

			}
		}

		JRTraceController controller = JRTraceControllerService.getInstance();
		String mynetwork = launchconfig.getAttribute(
				ConnectionTab.BM_MY_NETWORK_INTERFACE, "");
		if (mynetwork.isEmpty())
			mynetwork = null;
		final IJRTraceVM machine = controller.getMachine(pid, mynetwork);

		if (runTarget(launch, machine, monitor))
			return machine;

		showUnableToConnectDialog(pid, machine);

		throw new CoreException(Status.CANCEL_STATUS);

	}

	private IProject getProject(final String projectName) {
		IProject theProject = null;
		if (projectName.length() > 0) {
			theProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			if (!theProject.exists())
				theProject = null;
		}

		if (!(projectName == null) && !projectName.isEmpty()
				&& theProject == null) {
			if (!warnProjectDoesntExist(projectName))
				return theProject;
		}
		return theProject;
	}

	private void showProcessAlreadyConnectedDialog(final String pidCopy) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog msg = new MessageDialog(Display.getDefault()
						.getActiveShell(), "Launch Canceled", null,
						"Launch canceled. There is already an active jrtrace connection to "
								+ pidCopy, MessageDialog.ERROR,
						new String[] { "Ok" }, 0);
				msg.open();

			}

		});
	}

	private void showUnableToConnectDialog(final String pid,
			final IJRTraceVM machine) {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog msg = new MessageDialog(Display.getDefault()
						.getActiveShell(), "Error", null,
						"Unable to connect to JVM " + pid + ":\n"
								+ machine.getLastError().getMessage(),
						MessageDialog.ERROR, new String[] { "OK" }, 0);
				msg.open();
			}
		});
	}

	private String chooseProperProcess(final String identifyText) {
		String erg = null;
		JRTraceController controller = JRTraceControllerService.getInstance();
		VMInfo[] vms;
		if (identifyText != null && !identifyText.isEmpty()) {
			vms = controller.getVMs(identifyText);
			if (vms.length == 1)
				erg = vms[0].getId();
		}
		vms = controller.getVMs();

		if (erg == null || erg.isEmpty()) {

			final String resultpid[] = new String[1];
			final VMInfo[] usedVMs = vms;
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
						PIDSelectionDialog dialog = new PIDSelectionDialog(
								Display.getDefault().getActiveShell(), false);
						dialog.setVMs(usedVMs);
						dialog.open();
						if (dialog.getReturnCode() == IDialogConstants.OK_ID) {
							resultpid[0] = dialog.getPID();
						} else {
							resultpid[0] = "";
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			});
			erg = resultpid[0];

		}
		return erg;
	}

	/**
	 * 
	 * @param projectName
	 * @return true: continue, false: abort
	 */
	private boolean warnProjectDoesntExist(final String projectName) {
		final Integer[] result = new Integer[1];
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog msg = new MessageDialog(
						Display.getDefault().getActiveShell(),
						"Project doesn't exist",
						null,
						"The project "
								+ projectName
								+ " that was selected in the launch configuration doesn't exist in the workspace. Do you want to continue the launch?",
						MessageDialog.WARNING, new String[] { "Continue",
								"Cancel" }, 0);
				result[0] = msg.open();

			}

		});

		if (result[0] == 1)
			return true;
		else
			return false;
	}

	/**
	 * wait for a new jvm to start and return its pid.
	 * 
	 * @param monitor
	 *            wait cancels if the monitor is canceled
	 * @return null if canceled or error, pid of the newly started process
	 *         otherwise
	 */
	private String waitForStartedProcess(IProgressMonitor monitor) {
		JRTraceController controller = JRTraceControllerService.getInstance();
		VMInfo[] vms = controller.getVMs();
		HashSet<String> initialPIDs = new HashSet<String>();
		for (VMInfo vm : vms) {
			initialPIDs.add(vm.getId());
		}

		while (true) {
			if (monitor.isCanceled())
				return null;
			VMInfo[] new_vms = controller.getVMs();
			for (VMInfo vm : new_vms) {
				if (!initialPIDs.contains(vm.getId())) {
					return vm.getId();
				}
				// very short wait,just to yield and keep everybody else
				// responsive
				// since we can only poll, the poll time needs to be really
				// short.
				try {
					Thread.sleep(0, 50000);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}

	}

}
