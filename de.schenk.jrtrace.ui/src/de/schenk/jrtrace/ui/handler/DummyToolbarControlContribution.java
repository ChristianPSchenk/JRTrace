/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.handler;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class DummyToolbarControlContribution extends
		WorkbenchWindowControlContribution {

	public DummyToolbarControlContribution() {
		super("de.schenk.jrtrace.dummy.toolbar.control");

	}

	@Override
	protected Control createControl(Composite parent) {
		Composite l = new OnePixelWidthWidget(parent, SWT.NONE);

		return l;
	}
}
