package de.schenk.jrtrace.ui.debug;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class InstallJRTraceJob extends ProgressReportingJob {

	private File jarFile;

	public InstallJRTraceJob(JRTraceDebugTarget btarget, File jarFile) {
		super("Installing JRTrace Classes", btarget);

		this.jarFile = jarFile;

	}

	protected IStatus run() {
		getTarget().installEngineX(jarFile);
		return Status.OK_STATUS;
	}

}