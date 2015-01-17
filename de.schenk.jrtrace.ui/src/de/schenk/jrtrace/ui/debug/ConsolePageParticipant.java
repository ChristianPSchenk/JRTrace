package de.schenk.jrtrace.ui.debug;

import org.eclipse.core.resources.IProject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

import de.schenk.jrtrace.ui.Activator;
import de.schenk.jrtrace.ui.handler.RunEngineXHandler;

public class ConsolePageParticipant implements IConsolePageParticipant {

	private Action stopAction;
	private JRTraceConsole myConsole;

	@Override
	public Object getAdapter(Class adapter) {

		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {

		myConsole = (JRTraceConsole) console;

		IActionBars bar = page.getSite().getActionBars();

		stopAction = new Action() {
			@Override
			public void run() {

				JRTraceDebugTarget target = myConsole.getDebugTarget();
				if (!target.isDisconnected() && !target.isTerminated()) {
					try {
						target.terminate();
					} catch (DebugException e) {
						throw new RuntimeException(e);
					}
				}
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				return PlatformUI.getWorkbench().getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_ELCL_STOP);
			}
		};
		stopAction.setToolTipText("Terminate this JRTrace launch.");
		bar.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				stopAction);

		Action reinstallAction = new Action() {
			public void run() {

				IProject theproject = myConsole.getDebugTarget().getProject();
				if (theproject != null) {
					RunEngineXHandler.installJRTraceJar(theproject);
				}

			};

			@Override
			public ImageDescriptor getImageDescriptor() {
				return Activator.getInstance().getDescriptor(
						"upload_java_16.gif");
			}

			@Override
			public boolean isEnabled() {
				JRTraceDebugTarget target = myConsole.getDebugTarget();
				return !target.isDisconnected() && !target.isTerminated()
						&& !(target.getProject() == null);
			}
		};
		reinstallAction
				.setToolTipText("Reinstall the JRTrace project connected with this JRTrace session");

		bar.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				reinstallAction);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void activated() {

	}

	@Override
	public void deactivated() {

	}

}
