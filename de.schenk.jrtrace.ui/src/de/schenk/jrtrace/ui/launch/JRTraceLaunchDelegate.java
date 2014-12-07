/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.launch;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.schenk.jrtrace.helperlib.HelperLibConstants;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JarLocator;
import de.schenk.jrtrace.service.internal.VMInfo;
import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;

public class JRTraceLaunchDelegate implements ILaunchConfigurationDelegate {

	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		ILaunchConfiguration launchconfig = launch.getLaunchConfiguration();
		final String[] pid = new String[1];
		pid[0] = launchconfig.getAttribute(ConnectionTab.BM_PID, "");
		final String identifyText = launchconfig.getAttribute(
				ConnectionTab.BM_TEXT_IDENT, "");
		final String projectName = launchconfig.getAttribute(
				ConnectionTab.BM_PROJECT_IDENT, "");
		IProject theProject = null;
		if (projectName.length() > 0) {
			theProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			if (!theProject.exists())
				theProject = null;
		}

		if (!(projectName == null) && !projectName.isEmpty()
				&& theProject == null) {
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
				return;
		}
		if (launchconfig.getAttribute(ConnectionTab.BM_AUTOCONNECT, false)) {
			String startedProcess = waitForStartedProcess(monitor);
			if (monitor.isCanceled())
				throw new CoreException(Status.CANCEL_STATUS);
			if (startedProcess != null) {
				pid[0] = startedProcess;
			}
		}

		if ((pid[0] == null || pid[0].isEmpty())) {

			JRTraceController controller = JRTraceControllerService
					.getInstance();
			final VMInfo[] vms;
			if (identifyText != null && !identifyText.isEmpty()) {
				vms = controller.getVMs(identifyText);
				if (vms.length == 1)
					pid[0] = vms[0].getId();
			} else {
				vms = controller.getVMs();
			}
			;

			if (pid[0] == null || pid[0].isEmpty()) {

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						PIDSelectionDialog dialog = new PIDSelectionDialog();
						dialog.setVMs(vms);
						dialog.show(Display.getDefault().getActiveShell());
						pid[0] = dialog.getPID();

					}

				});
			}

		}

		List<JRTraceDebugTarget> jrtraceTargets = JRTraceLaunchUtils
				.getJRTraceDebugTargets();
		for (JRTraceDebugTarget btarget : jrtraceTargets) {
			if (btarget.getJRTraceMachine().getPID().equals(pid[0])) {
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						MessageDialog msg = new MessageDialog(Display
								.getDefault().getActiveShell(),
								"Launch Canceled", null,
								"Launch canceled. There is already an active jrtrace connection to "
										+ pid[0], MessageDialog.ERROR,
								new String[] { "Ok" }, 0);
						msg.open();

					}

				});
				throw new CoreException(Status.CANCEL_STATUS);

			}
		}

		JRTraceController controller = JRTraceControllerService.getInstance();
		final IJRTraceVM machine = controller.getMachine(pid[0]);
		if (machine.attach()) {

			boolean debugFlag = launchconfig.getAttribute(
					ConnectionTab.BM_DEBUG, false);
			boolean verboseFlag = launchconfig.getAttribute(
					ConnectionTab.BM_VERBOSE, false);
			if (machine.loadJRTraceAgent(verboseFlag, debugFlag)) {

				if (prepareGroovy(machine, theProject)) {

					boolean uploadHelperOnConnect = launchconfig.getAttribute(
							ConnectionTab.BM_AUTOUPLOAD, false);
					JRTraceDebugTarget dbt = new JRTraceDebugTarget(machine,
							launch, theProject, uploadHelperOnConnect);

					launch.addDebugTarget(dbt);
				}

				return;
			}

		}
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				MessageDialog msg = new MessageDialog(Display.getDefault()
						.getActiveShell(), "Error", null,
						"Unable to connect to JVM " + pid[0] + ":\n"
								+ machine.getLastError().getMessage(),
						MessageDialog.ERROR, new String[] { "OK" }, 0);
				msg.open();
			}
		});

		throw new CoreException(Status.CANCEL_STATUS);

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

	private boolean prepareGroovy(IJRTraceVM machine, IProject theProject) {

		String jar = null;
		try {
			jar = JarLocator.getGroovyLibJar();
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Properties p = new Properties();
		p.put(HelperLibConstants.DE_SCHENK_JRTRACE_GROOVYJAR, jar);
		if (theProject != null) {
			p.put(HelperLibConstants.DE_SCHENK_JRTRACE_PROJECTDIR, theProject
					.getLocation().toOSString());
		}
		return machine.setSystemProperties(p);

	}

}
