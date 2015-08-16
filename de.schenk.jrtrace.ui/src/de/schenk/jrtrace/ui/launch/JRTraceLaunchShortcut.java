/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

public class JRTraceLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, final String mode) {

		Object element = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			element = ssel.getFirstElement();
		}

		IProject pr = (IProject) Platform.getAdapterManager().getAdapter(
				element, IProject.class);
		if (pr != null) {

			ILaunchManager launchManager = DebugPlugin.getDefault()
					.getLaunchManager();
			ILaunchConfigurationType jrtraceLaunchType = launchManager
					.getLaunchConfigurationType("de.schenk.jrtrace.ui.launch");

			ILaunchConfiguration savedConfig = null;
			ILaunchConfigurationWorkingCopy configuration = null;
			try {
				String name = pr.getName() + " JRTrace Launch";

				ILaunchConfiguration[] existingLaunchConfigs = launchManager
						.getLaunchConfigurations(jrtraceLaunchType);
				for (ILaunchConfiguration l : existingLaunchConfigs) {
					if (name.equals(l.getName())) {
						savedConfig = l;
						break;
					}
				}
				if (savedConfig == null) {

					String uniqueName = launchManager
							.generateLaunchConfigurationName(name);

					configuration = jrtraceLaunchType.newInstance(null,
							uniqueName);
					configuration.setAttribute(ConnectionTab.BM_AUTOUPLOAD,
							true);
					configuration.setAttribute(ConnectionTab.BM_PROJECT_IDENT,
							pr.getName());
					configuration.setAttribute(ConnectionTab.BM_PID, "");
					configuration.setAttribute(ConnectionTab.BM_TEXT_IDENT, "");
					configuration.setAttribute(ConnectionTab.BM_DEBUG, false);
					configuration.setAttribute(ConnectionTab.BM_AUTOCONNECT,
							false);
					configuration.setAttribute(ConnectionTab.BM_VERBOSE, false);
					configuration.setAttribute(ConnectionTab.BM_COM_MODE, 0);
					configuration.setAttribute(ConnectionTab.BM_UPLOADAGENT,
							true);
					configuration.setAttribute(
							ConnectionTab.BM_UPLOADAGENT_PORT, 0);
					configuration.setAttribute(ConnectionTab.BM_SERVER_MACHINE,
							"");
					configuration.setAttribute(
							ConnectionTab.BM_MY_NETWORK_INTERFACE, "");
					savedConfig = configuration.doSave();
				}
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}

			try {
				final ILaunchConfiguration config = savedConfig;
				Job launchIt = new Job("Launch") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							config.launch(mode, monitor);
						} catch (CoreException e) {
							return e.getStatus();
						}
						return Status.OK_STATUS;
					}

				};

				launchIt.schedule();

			} catch (Throwable e) {
				try {
					if (configuration != null)
						configuration.delete();
				} catch (CoreException e1) {
					throw new RuntimeException(
							"Problem removing the launch configuration after launch failed",
							e1);
				}
				throw e;
			}
		}

	}

	@Override
	public void launch(IEditorPart editor, String mode) {

	}

}
