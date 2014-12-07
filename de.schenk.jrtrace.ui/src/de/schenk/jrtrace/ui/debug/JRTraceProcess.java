/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.debug;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;

public class JRTraceProcess implements IProcess {

	private JRTraceDebugTarget debugTarget;

	public JRTraceProcess(JRTraceDebugTarget JRTraceDebugTarget) {
		debugTarget = JRTraceDebugTarget;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean canTerminate() {
		return debugTarget.canTerminate();
	}

	@Override
	public boolean isTerminated() {
		return debugTarget.isTerminated();
	}

	@Override
	public void terminate() throws DebugException {
		debugTarget.terminate();

	}

	@Override
	public String getLabel() {
		String label;
		try {
			label = "JVM Process " + debugTarget.getName();
		} catch (DebugException e) {
			label = "<Exception>";
		}
		return label;
	}

	@Override
	public ILaunch getLaunch() {
		return debugTarget.getLaunch();
	}

	@Override
	public IStreamsProxy getStreamsProxy() {
		return null;
	}

	@Override
	public void setAttribute(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAttribute(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getExitValue() throws DebugException {

		return 0;
	}

}
