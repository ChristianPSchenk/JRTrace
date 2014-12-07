/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.wizard;

import org.eclipse.core.resources.IFile;
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

public class GroovyScriptPage extends WizardPage {

	private Text fileName;

	private LastChoicesCombo clc;

	public GroovyScriptPage() {
		super("Groovy Script");
		setTitle("Groovy Script");
		setDescription("Select groovy script and classloader to use");
		setImageDescriptor(Activator.getInstance().getDescriptor(
				"jrtrace_icon_48px.gif"));

	}

	RunGroovyWizard getGroovyWizard() {
		return (RunGroovyWizard) getWizard();
	}

	@Override
	public void createControl(Composite parent) {
		Composite box = new Composite(parent, SWT.FILL);
		GridLayout gl = new GridLayout(3, false);

		box.setLayout(gl);

		{
			Label description = new Label(box, SWT.NONE);
			description.setText("Scriptfile:");
			description
					.setToolTipText("The workspace relative path to the script file to execute. ");
			fileName = new Text(box, SWT.BORDER);
			fileName.addModifyListener(new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					updateWizardModel();

				}

			});
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			fileName.setLayoutData(gd);
		}

		{

			Label description = new Label(box, SWT.NONE);
			description.setText("Classloader-Class:");
			description
					.setToolTipText("Enter the fully qualified name of any class. The classloader of this class will be used to execute the groovy script. Empty will use the root classloader");
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
		setControl(box);

		updatePageControls();
		restoreSettings();
	}

	private void updatePageControls() {
		String filePath = getGroovyWizard().getTheFile().getFullPath()
				.toString();
		fileName.setText(filePath);
		clc.setText(getGroovyWizard().getTheClassLoader());

	}

	private void updateWizardModel() {
		setErrorMessage(null);
		boolean complete = true;
		IPath p = new Path(fileName.getText());
		IResource f = ResourcesPlugin.getWorkspace().getRoot().findMember(p);

		if (f instanceof IFile) {
			getGroovyWizard().setTheFile((IFile) f);

		} else {
			setErrorMessage("Groovy script file not found.");
			getGroovyWizard().setTheFile(null);
			complete = false;
		}

		getGroovyWizard().setTheClassLoader(clc.getText());
		setPageComplete(complete);
	}

	private void restoreSettings() {
		clc.restoreSettings();
		getGroovyWizard().setTheClassLoader(clc.getText());
	}

	public void storeSettings() {
		clc.storeSettings();

	}

}
