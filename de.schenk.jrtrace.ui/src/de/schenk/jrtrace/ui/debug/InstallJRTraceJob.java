package de.schenk.jrtrace.ui.debug;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.schenk.jrtrace.service.IJRTraceVM;

public class InstallJRTraceJob extends ProgressReportingJob {

	private File jarFile;

	public InstallJRTraceJob(IJRTraceVM machine, File jarFile) {
		super("Installing JRTrace Classes", machine);

		this.jarFile = jarFile;

	}

	protected IStatus run() {
		getMachine().installEngineXClass(jarFile.getAbsolutePath());
		return Status.OK_STATUS;
	}

}
