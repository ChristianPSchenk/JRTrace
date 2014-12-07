/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;

public class JRTraceLaunchUtils {

	static public List<JRTraceDebugTarget> getJRTraceDebugTargets() {
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();
		IDebugTarget[] targets = launchMgr.getDebugTargets();
		List<JRTraceDebugTarget> jrtraceTargets = new ArrayList<JRTraceDebugTarget>();
		for (IDebugTarget t : targets) {

			if (t.getModelIdentifier().equals(
					JRTraceDebugTarget.JRTRACE_DEBUG_MODEL)) {
				JRTraceDebugTarget btarget = (JRTraceDebugTarget) t;
				if (!btarget.isDisconnected() && !btarget.isTerminated()) {
					jrtraceTargets.add(btarget);
				}

			}

		}
		return jrtraceTargets;
	}
}
