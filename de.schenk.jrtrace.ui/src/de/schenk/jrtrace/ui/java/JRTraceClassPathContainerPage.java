/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.java;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.schenk.jrtrace.ui.JRTraceUIActivator;

public class JRTraceClassPathContainerPage extends WizardPage implements
		IClasspathContainerPage {

	private IClasspathEntry fSelection;

	public JRTraceClassPathContainerPage() {
		super("JRTrace Libraries");
		ImageDescriptor des = JRTraceUIActivator.getInstance().getDescriptor(
				"jrtrace_icon_48px.png");
		super.setImageDescriptor(des);
		super.setTitle("JRTrace Agent and Helper Libraries");

	}

	@Override
	public void createControl(Composite parent) {
		Composite g = new Composite(parent, SWT.NONE);
		g.setLayout(new GridLayout(1, false));
		g.setFont(parent.getFont());
		GridData gd = new GridData();
		gd.horizontalSpan = 1;

		Label label = new Label(g, SWT.WRAP);
		gd.widthHint = 300;
		label.setLayoutData(gd);

		label.setText("This class path library will add the paths to the JRTrace agent and helper libraries. You will need that to create your own helper libraries that extend Helper");
		setControl(g);

	}

	@Override
	public boolean finish() {
		IPath path = new Path(
				JRTraceClassPathContainer.JRTRACE_CLASSPATH_CONTAINER_ID);
		fSelection = JavaCore.newContainerEntry(path);
		return true;
	}

	@Override
	public IClasspathEntry getSelection() {

		return fSelection;
	}

	@Override
	public void setSelection(IClasspathEntry containerEntry) {
		// nothing to do

	}

}
