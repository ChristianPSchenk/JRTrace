/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.debug;

import javax.management.Notification;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;

import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.NotificationAndErrorListener;
import de.schenk.jrtrace.ui.JRTraceUIActivator;
import de.schenk.jrtrace.ui.handler.RunEngineXHandler;

public class ConsolePageParticipant implements IConsolePageParticipant,
		IDebugEventSetListener {

	private int errorCount = 0;
	private Action stopAction;
	private Action errorAction;
	private JRTraceConsole myConsole;
	private Action reinstallAction;

	@Override
	public Object getAdapter(Class adapter) {

		return null;
	}

	@Override
	public void init(IPageBookViewPage page, IConsole console) {

		myConsole = (JRTraceConsole) console;

		IActionBars bar = page.getSite().getActionBars();

		createErrorIndicatorAction(bar);
		createStopAction(bar);

		addReinstallAction(bar);

		DebugPlugin.getDefault().addDebugEventListener(this);

	}

	public void createStopAction(IActionBars bar) {
		stopAction = new Action() {
			@Override
			public void run() {

				final JRTraceDebugTarget target = myConsole.getDebugTarget();

				String name;
				try {
					name = target.getName();
				} catch (DebugException e1) {
					throw new RuntimeException(e1);
				}

				if (!target.isDisconnected() && !target.isTerminated()) {
					Job terminateJob = new Job("Terminating connection " + name) {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							try {
								target.terminate();
							} catch (DebugException e) {
								throw new RuntimeException(e);
							}
							return Status.OK_STATUS;
						}

					};

					terminateJob.setSystem(true);
					terminateJob.schedule();

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
	}

	public void createErrorIndicatorAction(IActionBars bar) {
		errorAction = new Action() {
			@Override
			public void run() {
				ErrorDialog
						.openError(
								null,
								"Transmission Problems",
								String.format(
										"%d messages from the target JVM to the development environment were lost. This can happen under heavy load conditions, e.g. if 10000nd of messages like big System.out are transmitted to the development machine. Consider using the BLOCKING mode for this launch.",
										errorCount), new Status(IStatus.ERROR,
										"de.schenk.jrtracce.ui",
										"Transmission Problem"));
			}

			@Override
			public ImageDescriptor getImageDescriptor() {
				return PlatformUI.getWorkbench().getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_OBJS_ERROR_TSK);
			}
		};
		errorAction.setEnabled(false);
		errorAction.setDisabledImageDescriptor(null);
		errorAction.setToolTipText("Status: No Transmission Problems.");
		bar.getToolBarManager().appendToGroup(IConsoleConstants.LAUNCH_GROUP,
				errorAction);
		NotificationAndErrorListener errorListener = new NotificationAndErrorListener() {

			@Override
			public void sendMessage(Notification notification) {
				// do nothing

			}

			@Override
			public void handleError() {
				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						errorAction.setEnabled(true);
						errorCount++;
						errorAction
								.setToolTipText("Error: Messages (Standard Output or others) have been lost.");

					}
				});
			}
		};
		myConsole
				.getDebugTarget()
				.getJRTraceMachine()
				.addClientListener(NotificationConstants.NOTIFY_MESSAGE,
						errorListener);

	}

	public void addReinstallAction(IActionBars bar) {
		reinstallAction = new Action() {
			public void run() {

				IProject theproject = myConsole.getDebugTarget().getProject();
				if (theproject != null) {
					RunEngineXHandler.installJRTraceJar(theproject);
				}

			};

			@Override
			public ImageDescriptor getImageDescriptor() {
				return JRTraceUIActivator.getInstance().getDescriptor(
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

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (DebugEvent e : events) {
			if (e.getSource() == myConsole.getDebugTarget()) {
				if (myConsole.getDebugTarget().isDisconnected()) {

					reinstallAction.setEnabled(false);
					stopAction.setEnabled(false);
				}
			}
		}

	}

}
