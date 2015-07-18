/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.views;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.launch.JRTraceLaunchUtils;

/**
 * Performs an analysis of injection results for the provided class on all
 * connected
 * 
 * @author CDLK
 *
 */
public class JRTraceDiagnosticsJob extends Job {

	private String clazz;
	private IDiagnosticJobCompletedListener completionlistener;

	public JRTraceDiagnosticsJob(String string,
			IDiagnosticJobCompletedListener result) {
		super(String.format("Injection Analysis for %s.", string));
		this.clazz = string;
		this.completionlistener = result;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		InjectStatus total = new InjectStatus(StatusEntityType.JRTRACE_MACHINE);

		total.setInjected(StatusState.DOESNT_INJECT);
		List<JRTraceDebugTarget> allTargets = JRTraceLaunchUtils
				.getJRTraceDebugTargets();
		monitor.beginTask("Analysing...", allTargets.size());
		for (JRTraceDebugTarget target : allTargets) {
			IJRTraceVM machine = target.getJRTraceMachine();
			InjectStatus status = machine.analyzeInjectionStatus(clazz);
			if (status.getInjectionState() == (StatusState.INJECTS)) {
				total.setInjected(StatusState.INJECTS);
			}
			monitor.worked(1);
			total.addChildStatus(status);
		}

		if (allTargets.size() == 0) {
			total.setMessage(InjectStatus.MSG_NO_JRTRACE_SESSION);
		}
		if (completionlistener != null) {
			completionlistener.completed(total);
		}
		return Status.OK_STATUS;

	}

}
