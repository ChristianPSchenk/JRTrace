/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.debug;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.IJRTraceVM;

abstract public class ProgressReportingJob extends Job implements
		NotificationListener {

	private IProgressMonitor monitor;

	private JRTraceDebugTarget machine;

	public ProgressReportingJob(String string, JRTraceDebugTarget btarget) {
		super(string);
		this.machine = btarget;
	}

	protected IJRTraceVM getMachine() {
		return machine.getJRTraceMachine();
	}

	protected JRTraceDebugTarget getTarget() {
		return machine;
	}

	@Override
	final protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;
		IStatus result = null;
		getMachine().addClientListener(NotificationConstants.NOTIFY_PROGRESS,
				this);
		try {
			result = run();
		} finally {
			getMachine().removeClientListener(
					NotificationConstants.NOTIFY_PROGRESS, this);
		}
		return result;

	}

	abstract protected IStatus run();

	int work;

	@Override
	public void handleNotification(Notification notification, Object handback) {

		AttributeChangeNotification a = (AttributeChangeNotification) notification;
		Integer current = (Integer) a.getOldValue();
		Integer total = (Integer) a.getNewValue();

		if (monitor.isCanceled()) {
			getMachine().abort();
		}
		if (current == 0) {
			monitor.beginTask(a.getMessage(), total);
			work = 0;
		}
		if (total == current) {
			monitor.done();
		} else {
			monitor.worked(current - work);
			work = current;
		}

	}

}
