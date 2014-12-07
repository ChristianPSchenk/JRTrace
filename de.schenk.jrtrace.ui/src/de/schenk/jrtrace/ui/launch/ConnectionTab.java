/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.schenk.jrtrace.service.JRTraceControllerService;

public class ConnectionTab extends AbstractLaunchConfigurationTab {

	public static final String BM_PID = "pid";
	public static final String BM_VERBOSE = "verbose";
	public static final String BM_DEBUG = "debug";
	public static final String BM_AUTOUPLOAD = "upload";
	public static final String BM_TEXT_IDENT = "textident";
	public static final String BM_PROJECT_IDENT = "project";
	public static final String BM_AUTOCONNECT = "autoconnect";
	private Text pidText;
	private Text identifyText;
	private Text rulesProjectName;

	private Button verboseButton;
	private Button debugButton;
	private Button autoconnectButton;
	private Button autouploadButton;

	@Override
	public void createControl(final Composite parent) {
		Composite box = new Composite(parent, SWT.FILL);
		GridLayout gl = new GridLayout(3, false);
		box.setLayout(gl);

		{
			Label description = new Label(box, SWT.NONE);
			description.setText("Identify Process by Text:");
			description
					.setToolTipText("If no process id is specified, the process to connect to will be determined by scanning the process description for the text specified in this field. ");
			identifyText = new Text(box, SWT.BORDER);
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			identifyText.setLayoutData(gd);
			ModifyListener mod = new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					setDirty();

				}

			};
			identifyText.addModifyListener(mod);

		}
		{
			Label pidLabel = new Label(box, SWT.NONE);
			pidLabel.setText("PID:");
			pidLabel.setToolTipText("The process id of the JVM to connect to. If this is specified, it will be used. If it is not specified, the identify text or the auto connect feature will be used.");
			pidText = new Text(box, SWT.NONE | SWT.BORDER);
			GridData gd = new GridData();
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			pidText.setLayoutData(gd);
			pidText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					setDirty();
				}
			});

			Button pidSelectButton = new Button(box, SWT.NONE);
			pidSelectButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PIDSelectionDialog pidDialog = new PIDSelectionDialog();
					pidDialog.setVMs(JRTraceControllerService.getInstance()
							.getVMs());
					pidDialog.show(parent.getShell());
					pidText.setText(pidDialog.getPID());
					setDirty();
				}
			});
			pidSelectButton.setText("Select");

			{
				Label l = new Label(box, SWT.NONE);
				l.setText("Rules/Helper Project:");
				rulesProjectName = new Text(box, SWT.BORDER);
				GridData gd2 = new GridData();
				gd2.grabExcessHorizontalSpace = true;
				gd2.horizontalAlignment = SWT.FILL;
				rulesProjectName.setLayoutData(gd2);
				rulesProjectName.addModifyListener(new ModifyListener() {

					@Override
					public void modifyText(ModifyEvent e) {
						setDirty();

					}
				});
				Button selectRules = new Button(box, SWT.NONE);
				selectRules.setText("Select Project");
				selectRules
						.setToolTipText("Rules (*.btml) files from the selected project are automatically synchronized with the target connected to the project. I.e. changes of rules are reflected immediately.");
				selectRules.addSelectionListener(new SelectionAdapter() {

					@Override
					public void widgetSelected(SelectionEvent e) {
						ElementListSelectionDialog selectRules = new ElementListSelectionDialog(
								parent.getShell(), new WorkbenchLabelProvider());
						selectRules.setElements(ResourcesPlugin.getWorkspace()
								.getRoot().getProjects(0));
						selectRules
								.setTitle("Rules and Helper Project Selection");
						selectRules
								.setMessage("Select the project that will hold the rules and helper jar files that you need for this launch configuration.");
						selectRules.setMultipleSelection(false);
						selectRules.open();

						Object project = selectRules.getFirstResult();
						if (project != null) {
							IProject theProject = (IProject) project;
							rulesProjectName.setText(theProject.getName());
							setDirty();
						}
					}
				});
			}
			{
				Label l = new Label(box, SWT.NONE);
				l.setText("Auto-Upload:");
				l.setToolTipText("If Auto-Upload is enabled, the project will automatically be deployed as JRTrace project into the target VM.");
				autouploadButton = new Button(box, SWT.CHECK);
				GridData gd2 = new GridData();
				gd2.horizontalSpan = 2;
				autouploadButton.setLayoutData(gd2);
				autouploadButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						setDirty();
					}
				});

			}
			{
				Label l = new Label(box, SWT.NONE);
				l.setText("Autoconnect:");
				l.setToolTipText("If autoconnect is enabled, the launch will block until a new java process is launched and then connect to this process.");
				autoconnectButton = new Button(box, SWT.CHECK);
				GridData gd2 = new GridData();
				gd2.horizontalSpan = 2;
				autoconnectButton.setLayoutData(gd2);
				autoconnectButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						setDirty();
					}
				});

			}
			{
				Label l = new Label(box, SWT.NONE);
				l.setText("Verbose:");
				verboseButton = new Button(box, SWT.CHECK);
				GridData gd2 = new GridData();
				gd2.horizontalSpan = 2;
				verboseButton.setLayoutData(gd2);
				verboseButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						setDirty();
					}
				});

			}
			{
				Label l = new Label(box, SWT.NONE);
				l.setText("Debug:");
				debugButton = new Button(box, SWT.CHECK);
				GridData gd2 = new GridData();
				gd2.horizontalSpan = 2;
				debugButton.setLayoutData(gd2);
				debugButton.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						setDirty();
					}
				});

			}
		}
		setControl(box);

	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(BM_PID, "");
		configuration.setAttribute(BM_TEXT_IDENT, "");
		configuration.setAttribute(BM_PROJECT_IDENT, "");
		configuration.setAttribute(BM_DEBUG, false);
		configuration.setAttribute(BM_AUTOUPLOAD, false);
		configuration.setAttribute(BM_AUTOCONNECT, false);
		configuration.setAttribute(BM_VERBOSE, false);
	}

	private void setDirty() {
		setDirty(true);
		getLaunchConfigurationDialog().updateButtons();
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String att = configuration.getAttribute(BM_PID, "");
			pidText.setText(att);
			String ident = configuration.getAttribute(BM_TEXT_IDENT, "");
			identifyText.setText(ident);
			String project = configuration.getAttribute(BM_PROJECT_IDENT, "");
			rulesProjectName.setText(project);
			debugButton.setSelection(configuration
					.getAttribute(BM_DEBUG, false));
			autoconnectButton.setSelection(configuration.getAttribute(
					BM_AUTOCONNECT, false));
			autouploadButton.setSelection(configuration.getAttribute(
					BM_AUTOUPLOAD, false));
			verboseButton.setSelection(configuration.getAttribute(BM_VERBOSE,
					false));
		} catch (CoreException e) {
			throw new RuntimeException(
					"Problem in ConnectionTab.intializeFrom", e);
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(BM_TEXT_IDENT, identifyText.getText());
		configuration.setAttribute(BM_PID, pidText.getText());

		configuration
				.setAttribute(BM_PROJECT_IDENT, rulesProjectName.getText());
		configuration.setAttribute(BM_VERBOSE, verboseButton.getSelection());
		configuration.setAttribute(BM_AUTOUPLOAD,
				autouploadButton.getSelection());
		configuration.setAttribute(BM_AUTOCONNECT,
				autoconnectButton.getSelection());
		configuration.setAttribute(BM_DEBUG, debugButton.getSelection());

	}

	@Override
	public String getName() {
		return "JRTrace";
	}

}
