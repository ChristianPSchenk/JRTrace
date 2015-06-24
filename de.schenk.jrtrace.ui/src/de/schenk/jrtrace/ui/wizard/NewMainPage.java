/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.schenk.jrtrace.ui.JRTraceUIActivator;

public class NewMainPage extends WizardPage {

	private Text projectName;
	private Text packageName;
	private Text className;

	protected NewMainPage(String pageName) {
		super(pageName);
		setImageDescriptor(JRTraceUIActivator.getInstance().getDescriptor(
				"jrtrace_icon_48px.png"));

	}

	@Override
	public void createControl(Composite parent) {

		this.setTitle("General");

		Composite bag = new Composite(parent, SWT.NONE);
		GridLayout grid = new GridLayout(2, false);
		this.setPageComplete(false);

		bag.setLayout(grid);
		{
			Label projectNameLabel = new Label(bag, SWT.RIGHT);
			projectNameLabel.setText("Project Name:");
		}
		{
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			projectName = new Text(bag, SWT.BORDER);
			projectName.setLayoutData(gd);
			projectName.setText("exampleJRTraceProject");
			projectName.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (projectName.getText().length() > 0) {
						setPageComplete(true);
					} else {
						setPageComplete(false);
					}

				}
			});
		}
		{
			Label projectNameLabel = new Label(bag, SWT.RIGHT);
			projectNameLabel.setText("Java Package:");
		}
		{
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			packageName = new Text(bag, SWT.BORDER);
			packageName.setLayoutData(gd);
			packageName.setText("de.schenk.jrtrace.libs");
			packageName
					.setToolTipText("Provide a valid java package e.g. my.java.package");
			packageName.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (packageName.getText().length() > 0) {
						setPageComplete(true);
					} else {
						setPageComplete(false);
					}

				}
			});
		}
		{
			Label projectNameLabel = new Label(bag, SWT.RIGHT);
			projectNameLabel.setText("Java Helper Class Name:");
		}
		{
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			className = new Text(bag, SWT.BORDER);
			className.setLayoutData(gd);
			className.setText("SampleHelperLib");
			className
					.setToolTipText("Provide a valid name for a java class, e.g. MyClass");
			className.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					if (checkPage()) {
						setPageComplete(true);
					} else {
						setPageComplete(false);
					}

				}

			});
		}
		setControl(bag);
		this.setPageComplete(true);
	}

	public String getClassName() {
		return className.getText();
	}

	public String getPackageName() {
		return packageName.getText();
	}

	public String getProjectName() {
		return projectName.getText();
	}

	private boolean checkPage() {
		if (projectName.getText().isEmpty())
			return false;

		if (className.getText().isEmpty())
			return false;
		return true;
	}

}
