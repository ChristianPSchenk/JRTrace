/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.debug;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.console.MessageConsole;

public class JRTraceConsole extends MessageConsole {

	private JRTraceDebugTarget debugTarget;

	public JRTraceConsole(String name, JRTraceDebugTarget jrTraceDebugTarget,
			ImageDescriptor imageDescriptor) {

		super(name, imageDescriptor);
		this.debugTarget = jrTraceDebugTarget;
	}

	public JRTraceDebugTarget getDebugTarget() {
		return debugTarget;

	}

}
