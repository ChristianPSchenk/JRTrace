/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.launch.JRTraceLaunchUtils;

public class JRTraceSourceProvider extends AbstractSourceProvider implements
		ILaunchesListener, IDebugEventSetListener {

	public static final String JRTRACE_SESSION_ACTIVE = "jrtraceSessionActive";

	public JRTraceSourceProvider() {
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	@Override
	public void dispose() {
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	@Override
	public Map<?, ?> getCurrentState() {
		Boolean state = getJRTraceSessionActive();

		HashMap<String, Object> result = new HashMap<String, Object>();
		result.put(JRTRACE_SESSION_ACTIVE, state);

		return result;
	}

	private Boolean getJRTraceSessionActive() {
		List<JRTraceDebugTarget> jrtraceTargets = JRTraceLaunchUtils
				.getJRTraceDebugTargets();
		Boolean state = jrtraceTargets.size() > 0;

		return state;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { JRTRACE_SESSION_ACTIVE };
	}

	@Override
	public void launchesRemoved(ILaunch[] launches) {
		refreshProperties();

	}

	@Override
	public void launchesAdded(ILaunch[] launches) {
		refreshProperties();

	}

	@Override
	public void launchesChanged(ILaunch[] launches) {
		refreshProperties();

	}

	private void refreshProperties() {
		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				fireSourceChanged(ISources.WORKBENCH, JRTRACE_SESSION_ACTIVE,
						getJRTraceSessionActive());
			}
		});

	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {

		boolean update = false;
		for (DebugEvent e : events) {
			int kind = e.getKind();
			if (e.getSource() instanceof JRTraceDebugTarget) {
				switch (kind) {
				case DebugEvent.CREATE:
				case DebugEvent.TERMINATE:
				case DebugEvent.RESUME:
				case DebugEvent.SUSPEND:
				case DebugEvent.STATE:
					update = true;

					break;
				default:

				}
			}
			if (update)
				break;

		}

		refreshProperties();
	}

}
