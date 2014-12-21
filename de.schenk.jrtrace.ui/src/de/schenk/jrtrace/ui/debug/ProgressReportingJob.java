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

	private IJRTraceVM machine;

	public ProgressReportingJob(String string, IJRTraceVM machine2) {
		super(string);
		this.machine = machine2;
	}

	protected IJRTraceVM getMachine() {
		return machine;
	}

	@Override
	final protected IStatus run(IProgressMonitor monitor) {
		this.monitor = monitor;
		machine.addClientListener(NotificationConstants.NOTIFY_PROGRESS, this);
		return run();

	}

	abstract protected IStatus run();

	int work;

	@Override
	public void handleNotification(Notification notification, Object handback) {
		AttributeChangeNotification a = (AttributeChangeNotification) notification;
		Integer current = (Integer) a.getOldValue();
		Integer total = (Integer) a.getNewValue();

		if (monitor.isCanceled()) {
			machine.abort();
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
