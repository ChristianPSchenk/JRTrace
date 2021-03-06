/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.debug;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class DetachJRTraceJob extends ProgressReportingJob {

	public DetachJRTraceJob(JRTraceDebugTarget machine) {
		super("Detaching from machine.", machine);

	}

	@Override
	protected IStatus run() {
		getMachine().setAcknowledgementMode(0);
		getMachine().clearEngineX();
		getMachine().detach();
		return Status.OK_STATUS;
	}

}
