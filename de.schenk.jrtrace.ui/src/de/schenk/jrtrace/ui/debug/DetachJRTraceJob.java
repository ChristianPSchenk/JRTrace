package de.schenk.jrtrace.ui.debug;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.schenk.jrtrace.service.IJRTraceVM;

public class DetachJRTraceJob extends ProgressReportingJob {

	public DetachJRTraceJob(IJRTraceVM machine) {
		super("Detaching from machine.", machine);

	}

	@Override
	protected IStatus run() {
		getMachine().detach();
		return Status.OK_STATUS;
	}

}
