/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.wizard;

import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.SelectionDialog;

import de.schenk.jrtrace.ui.Activator;

public class RunJavaPage extends WizardPage {

	public class TypeSelection extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {

			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
			try {
				SelectionDialog typeDialog = JavaUI.createTypeDialog(
						mainclass.getShell(), null,

						scope, IJavaElementSearchConstants.CONSIDER_CLASSES,
						false, "*", null);
				int result = typeDialog.open();
				if (result == SelectionDialog.OK) {
					Object[] o = typeDialog.getResult();
					if (o.length < 1) {
						return;
					}
					if (o[0] instanceof IType) {
						IType theType = (IType) (o[0]);
						mainclass.setText(theType.getFullyQualifiedName());
					}
				}

			} catch (JavaModelException e1) {
				throw new RuntimeException(e1);
			}

		}
	}

	public class MethodSelection extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {

			final HashSet<IType> foundTypes = new HashSet<IType>();
			findTypesMatchingTheClassName(foundTypes);

			HashSet<IMethod> foundMethods = new HashSet<IMethod>();
			for (IType t : foundTypes) {
				IMethod[] methods;
				try {
					methods = t.getMethods();
				} catch (JavaModelException e1) {
					throw new RuntimeException(e1);
				}
				for (IMethod method : methods) {
					try {
						if (method.getNumberOfParameters() == 0
								&& !method.isConstructor()) {
							foundMethods.add(method);
						}
					} catch (JavaModelException e1) {
						throw new RuntimeException(e1);
					}
				}

			}

			FilteredMethodsSelectionDialog dialog = new FilteredMethodsSelectionDialog(
					runMethod.getShell(), getRunJavaWizard().getMainClass(),
					foundMethods);
			int status = dialog.open();
			if (status == dialog.OK) {
				Object[] result = dialog.getResult();
				if (result.length > 0) {
					IMethod method = (IMethod) result[0];
					runMethod.setText(method.getElementName());
				}

			}

		}

		public void findTypesMatchingTheClassName(
				final HashSet<IType> foundTypes) {
			SearchPattern types = SearchPattern.createPattern(
					getRunJavaWizard().getMainClass(),
					IJavaSearchConstants.TYPE,
					IJavaSearchConstants.DECLARATIONS,
					SearchPattern.R_EXACT_MATCH);
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
			SearchRequestor requestor = new SearchRequestor() {
				public void acceptSearchMatch(SearchMatch match) {
					foundTypes.add((IType) match.getElement());
				}
			};
			SearchEngine engine = new SearchEngine();
			try {
				engine.search(types, new SearchParticipant[] { SearchEngine
						.getDefaultSearchParticipant() }, scope, requestor,
						null);
			} catch (CoreException e1) {
				throw new RuntimeException(e1);
			}
		}
	}

	private LastChoicesCombo clc;

	private LastChoicesCombo mainclass;

	private LastChoicesCombo runMethod;

	private boolean noupdate;

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

			Link description = new Link(box, SWT.NONE);
			description.setText("<A>Invoked Class</A>:");
			description.addSelectionListener(new TypeSelection());
			description
					.setToolTipText("The fully qualified name of the JRTrace class on which to invoke code.");
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

			Link description = new Link(box, SWT.NONE);
			description.setText("<A>Invoked Method:</A>");
			description.addSelectionListener(new MethodSelection());
			description
					.setToolTipText("The name of the no-argument method that will be invoked");
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

		{

			Label info = new Label(box, SWT.NONE);
			GridData infogd = new GridData();
			infogd.verticalSpan = 1;
			infogd.horizontalSpan = 3;
			info.setLayoutData(infogd);
			info.setText("Only required to execute code from classes that use XClassloaderPolicy.TARGET:");
			Label description = new Label(box, SWT.NONE);
			description.setText("Classloader-Class:");
			description
					.setToolTipText("Only required if the invoked class is of XClassLoaderPolicy.TARGET type. Specify the fully qualified name of the class that is used to get the classloader for executing the code.");
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

	/**
	 * Updates the page with the current settings from the wizard model
	 */
	protected void updatePageControls() {

		noupdate = true;
		mainclass.setText(getRunJavaWizard().getMainClass());
		runMethod.setText(getRunJavaWizard().getRunMethod());
		clc.setText(getRunJavaWizard().getTheClassLoader());
		noupdate = false;
		updateWizardModel();

	}

	private void updateWizardModel() {
		if (noupdate)
			return;
		setErrorMessage(null);
		boolean complete = true;

		getRunJavaWizard().setTheClassLoader(clc.getText());

		if (mainclass.getText().isEmpty()) {
			setErrorMessage("Plesae specify the main class to run.");

			complete = false;
		} else
			getRunJavaWizard().setMainClass(mainclass.getText());

		if (runMethod.getText().isEmpty()) {
			setErrorMessage("Please specify the name of the static method to run.");

			complete = false;
		} else
			getRunJavaWizard().setRunMethod(runMethod.getText());

		setPageComplete(complete);
	}

	/**
	 * restores the stored/persistet settings to the controls
	 */
	private void restoreSettings() {
		clc.restoreSettings();
		mainclass.restoreSettings();
		runMethod.restoreSettings();
		getRunJavaWizard().setTheClassLoader(clc.getText());
		getRunJavaWizard().setMainClass(mainclass.getText());
		getRunJavaWizard().setRunMethod(runMethod.getText());
	}

	/**
	 * stores the settings of the controls for the next session
	 */
	public void storeSettings() {
		clc.storeSettings();
		mainclass.storeSettings();
		runMethod.storeSettings();
	}

}
