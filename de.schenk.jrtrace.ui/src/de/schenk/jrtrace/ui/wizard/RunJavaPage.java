/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.schenk.jrtrace.ui.Activator;

public class RunJavaPage extends WizardPage {

	private Text javaProjectName;

	private LastChoicesCombo clc;

	private LastChoicesCombo mainclass;

	private LastChoicesCombo runMethod;

	public RunJavaPage() {
		super("Run Java");
		setTitle("Run Java");
		setDescription("Select the java project and the class loader to use for it.");
		setImageDescriptor(Activator.getInstance().getDescriptor(
				"jrtrace_icon_48px.gif"));

	}

	RunJavaWizard getRunJavaWizard() {
		return (RunJavaWizard) getWizard();
	}

	@Override
	public void createControl(Composite parent) {
		Composite box = new Composite(parent, SWT.FILL);
		GridLayout gl = new GridLayout(3, false);

		box.setLayout(gl);

		{
			Label description = new Label(box, SWT.NONE);
			description.setText("Java Project:");
			description
					.setToolTipText("The name of the java project to use to upload");
			javaProjectName = new Text(box, SWT.BORDER);
			javaProjectName.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					updateWizardModel();

				}

			});
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			javaProjectName.setLayoutData(gd);
		}

		{

			Label description = new Label(box, SWT.NONE);
			description.setText("Classloader-Class:");
			description
					.setToolTipText("Enter the fully qualified name of any class. The classloader of this class will be used to execute the java code. Empty will use the root classloader");
			clc = new LastChoicesCombo(box, SWT.NONE, "classloader");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			clc.setLayoutData(gd);
			clc.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					updateWizardModel();

				}
			});
		}

		{

			Label description = new Label(box, SWT.NONE);
			description.setText("Main-Class:");
			description
					.setToolTipText("The fully qualified name of the 'Main' class");
			mainclass = new LastChoicesCombo(box, SWT.NONE, "mainclass");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			mainclass.setLayoutData(gd);
			mainclass.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					updateWizardModel();

				}
			});
		}
		{

			Label description = new Label(box, SWT.NONE);
			description.setText("Run-Method:");
			description
					.setToolTipText("The name of the static method in the main class to invoke");
			runMethod = new LastChoicesCombo(box, SWT.NONE, "runmethod");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			runMethod.setLayoutData(gd);
			runMethod.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					updateWizardModel();

				}
			});
		}
		setControl(box);

		updatePageControls();
		restoreSettings();
	}

	private void updatePageControls() {
		String filePath = getRunJavaWizard().getProject().getFullPath()
				.toString();
		javaProjectName.setText(filePath);
		clc.setText(getRunJavaWizard().getTheClassLoader());
		mainclass.setText(getRunJavaWizard().getMainClass());
		runMethod.setText(getRunJavaWizard().getRunMethod());

	}

	private void updateWizardModel() {
		setErrorMessage(null);
		boolean complete = true;
		IPath p = new Path(javaProjectName.getText());
		IResource f = ResourcesPlugin.getWorkspace().getRoot().findMember(p);

		if (f instanceof IProject) {
			getRunJavaWizard().setProject((IProject) f);

		} else {
			setErrorMessage("Java project " + javaProjectName.getText()
					+ " doesn't exist.");
			getRunJavaWizard().setProject(null);
			complete = false;
		}

		getRunJavaWizard().setTheClassLoader(clc.getText());

		if (mainclass.getText().isEmpty()) {
			setErrorMessage("Plesae specify the main class to run.");
			getRunJavaWizard().setMainClass("");
			complete = false;
		} else
			getRunJavaWizard().setMainClass(mainclass.getText());

		if (runMethod.getText().isEmpty()) {
			setErrorMessage("Please specify the name of the static method to run.");
			getRunJavaWizard().setRunMethod("");
			complete = false;
		} else
			getRunJavaWizard().setRunMethod(runMethod.getText());

		setPageComplete(complete);
	}

	private void restoreSettings() {
		clc.restoreSettings();
		mainclass.restoreSettings();
		runMethod.restoreSettings();
		getRunJavaWizard().setTheClassLoader(clc.getText());
		getRunJavaWizard().setMainClass(mainclass.getText());
		getRunJavaWizard().setRunMethod(runMethod.getText());
	}

	public void storeSettings() {
		clc.storeSettings();
		mainclass.storeSettings();
		runMethod.storeSettings();
	}

}
