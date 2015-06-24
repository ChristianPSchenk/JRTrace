/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.wizard;

import java.util.HashSet;
import java.util.List;

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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;

import de.schenk.jrtrace.ui.JRTraceUIActivator;
import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.launch.JRTraceLaunchUtils;

public class RunJavaPage extends WizardPage {

	public JRTraceDebugTarget getCurrentTargetMachineSelection() {
		ISelection sel = targetMachine.getSelection();
		if (sel instanceof IStructuredSelection) {
			return (JRTraceDebugTarget) ((IStructuredSelection) sel)
					.getFirstElement();
		}
		return null;
	}

	public class ClassLoaderSelectionListener extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {

			JRTraceDebugTarget target = getCurrentTargetMachineSelection();
			String[] loadedclasses = target.getJRTraceMachine()
					.getLoadedClasses();

			ElementListSelectionDialog dialog = new ElementListSelectionDialog(
					mainclass.getShell(), new LabelProvider());
			dialog.setTitle("Classloader Class ");
			dialog.setMessage("Select from the classes that are currently loaded in the target machine to obtain the classloader for the code execution.");

			dialog.setElements(loadedclasses);
			if (dialog.open() == ElementListSelectionDialog.OK) {
				Object[] result = dialog.getResult();
				if (result.length > 0) {
					clc.setText((String) (result[0]));
				}
			}

		}

	}

	public class JRTraceTargetContentProvider implements
			IStructuredContentProvider {

		private Object[] elements;

		@Override
		public void dispose() {
			// TODO Auto-generated method stub

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			elements = (Object[]) newInput;
			viewer.refresh();

		}

		@Override
		public Object[] getElements(Object inputElement) {
			if (elements == null)
				return new JRTraceDebugTarget[0];
			return elements;

		}

	}

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
			if (status == FilteredMethodsSelectionDialog.OK) {
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

	private ComboViewer targetMachine;

	public RunJavaPage() {
		super("Run Java");
		setTitle("Run Java");
		setDescription("Select the java project and the class loader to use for it.");
		setImageDescriptor(JRTraceUIActivator.getInstance().getDescriptor(
				"jrtrace_icon_48px.png"));

	}

	private List<JRTraceDebugTarget> getJRTraceTargets() {
		List<JRTraceDebugTarget> jrtraceTargets = JRTraceLaunchUtils
				.getJRTraceDebugTargets();
		return jrtraceTargets;
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
			description.setText("Target:");

			description
					.setToolTipText("Select the target machine to invoke the code in.");
			targetMachine = new ComboViewer(box, SWT.READ_ONLY);

			targetMachine
					.setContentProvider(new JRTraceTargetContentProvider());
			JRTraceDebugTarget[] targets = getJRTraceTargets().toArray(
					new JRTraceDebugTarget[0]);
			targetMachine.setInput(targets);

			targetMachine
					.setLabelProvider(new JRTraceDebugTargetLabelProvider());
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			targetMachine.getCombo().setLayoutData(gd);
			targetMachine
					.addSelectionChangedListener(new ISelectionChangedListener() {

						@Override
						public void selectionChanged(SelectionChangedEvent event) {
							updateWizardModel();
						}
					});

		}

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

			Link description = new Link(box, SWT.NONE);
			description
					.addSelectionListener(new ClassLoaderSelectionListener());
			description.setText("<A>Classloader-Class:</A>");
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
		if (getRunJavaWizard().getDebugTarget() != null) {
			targetMachine.setSelection(new StructuredSelection(
					getRunJavaWizard().getDebugTarget()));
		} else {
			List<JRTraceDebugTarget> targets = getJRTraceTargets();
			if (targets.size() > 0)
				targetMachine.setSelection(new StructuredSelection(targets
						.get(0)));
		}

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

		ISelection sel = targetMachine.getSelection();
		if (sel instanceof IStructuredSelection) {

			getRunJavaWizard().setDebugTarget(
					(JRTraceDebugTarget) (((IStructuredSelection) sel)
							.getFirstElement()));
		}

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
		IDialogSettings settings = JRTraceUIActivator.getInstance()
				.getDialogSettings();
		String storedLaunchName = settings.get("runjava.targetmachine");
		if (storedLaunchName != null) {
			List<JRTraceDebugTarget> targets = getJRTraceTargets();
			for (JRTraceDebugTarget t : targets) {
				String launchname = t.getLaunch().getLaunchConfiguration()
						.getName();
				if (storedLaunchName.equals(launchname)) {
					targetMachine.setSelection(new StructuredSelection(t));
				}

			}
		}

	}

	/**
	 * stores the settings of the controls for the next session
	 */
	public void storeSettings() {

		IDialogSettings settings = JRTraceUIActivator.getInstance()
				.getDialogSettings();
		settings.put("runjava.targetmachine", targetMachine.getCombo()
				.getText());
		clc.storeSettings();
		mainclass.storeSettings();
		runMethod.storeSettings();
	}

}
