package de.schenk.jrtrace.ui.views;

import de.schenk.jrtrace.helperlib.status.InjectStatus;

/**
 * 
 * 
 * @author Christian Schenk
 *
 */
public interface IDiagnosticJobCompletedListener {

	public void completed(InjectStatus result);
}
