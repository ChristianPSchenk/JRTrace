package de.schenk.jrtrace.ui.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

public class JRTraceLaunchShortcut implements ILaunchShortcut {

	@Override
	public void launch(ISelection selection, String mode) {

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
			ILaunchConfigurationType configtype = launchManager
					.getLaunchConfigurationType("de.schenk.jrtrace.ui.launch");
			ILaunchConfigurationWorkingCopy configuration;
			ILaunchConfiguration savedConfig = null;
			try {
				String name = pr.getName() + "_Launch";
				String uniqueName = launchManager
						.generateLaunchConfigurationName(name);

				configuration = configtype.newInstance(null, uniqueName);
				configuration.setAttribute(ConnectionTab.BM_AUTOUPLOAD, true);
				configuration.setAttribute(ConnectionTab.BM_PROJECT_IDENT,
						pr.getName());
				configuration.setAttribute(ConnectionTab.BM_PID, "");
				configuration.setAttribute(ConnectionTab.BM_TEXT_IDENT, "");
				configuration.setAttribute(ConnectionTab.BM_DEBUG, false);
				configuration.setAttribute(ConnectionTab.BM_AUTOCONNECT, false);
				configuration.setAttribute(ConnectionTab.BM_VERBOSE, false);
				configuration.setAttribute(ConnectionTab.BM_UPLOADAGENT, true);
				configuration
						.setAttribute(ConnectionTab.BM_UPLOADAGENT_PORT, 0);
				configuration.setAttribute(ConnectionTab.BM_SERVER_MACHINE, "");
				configuration.setAttribute(
						ConnectionTab.BM_MY_NETWORK_INTERFACE, "");
				savedConfig = configuration.doSave();
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}

			DebugUITools.launch(savedConfig, mode);
		}

	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		// TODO Auto-generated method stub

	}

}
