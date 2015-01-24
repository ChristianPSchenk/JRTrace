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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import de.schenk.enginex.helper.NetworkUtil;
import de.schenk.jrtrace.jdk.init.Activator;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.JarLocator;

public class ConnectionTab extends AbstractLaunchConfigurationTab {

	public static final String BM_PID = "pid";
	public static final String BM_VERBOSE = "verbose";
	public static final String BM_DEBUG = "debug";
	public static final String BM_AUTOUPLOAD = "upload";
	public static final String BM_TEXT_IDENT = "textident";
	public static final String BM_PROJECT_IDENT = "project";
	public static final String BM_AUTOCONNECT = "autoconnect";
	public static final String BM_UPLOADAGENT = "uploadagent";
	public static final String BM_UPLOADAGENT_PORT = "uploadagentport";
	public static final String BM_SERVER_MACHINE = "targetservermachine";
	public static final String BM_MY_NETWORK_INTERFACE = "mynetwork";
	private Text pidText;
	private Text identifyText;
	private Text rulesProjectName;

	private Button uploadAgent;

	private Button verboseButton;
	private Button debugButton;
	private Button autoconnectButton;
	private Button autouploadButton;
	private Button connectAgent;
	private Text portText;
	private Button pidSelectButton;
	private Button copyJavaParameterButton;
	private Text serverText;
	private Combo networkCombo;

	@Override
	public void createControl(final Composite parent) {

		final Composite box = new Composite(parent, SWT.FILL);
		GridLayout gl = new GridLayout(3, false);
		box.setLayout(gl);

		if (!Activator.hasJDK()) {
			createNoJDKWarningText(box);
		}
		String[] networkAddressNames = NetworkUtil
				.getNonLoopbackAndNonLinkLocalAddresses();
		if (networkAddressNames.length > 1) {
			createSelectNetworkAddressNameCombo(box, networkAddressNames);
		}
		createUploadAgentButton(box);
		createIdentifyByTextText(box);
		createPIDText(box);
		createAutoConnectCheckBox(box);

		createConnectAgentButton(box);
		createServerText(box);
		createPortText(box);

		createSelectProject(box);
		createAutoUploadCheckBox(box);

		createVerboseCheckbox(box);
		createDebugCheckbox(box);
		setControl(box);

		connectAgent.setSelection(!uploadAgent.getSelection());
		updateUI();

	}

	private void createSelectNetworkAddressNameCombo(Composite box,
			String[] networkAddressNames) {
		Label description = new Label(box, SWT.READ_ONLY);
		description.setText("Select network interface:");
		description
				.setToolTipText("Your computes has more than 1 network addresses. In order to reach the agent from a remote computer you need to select the network address through which the request will come. If JRTrace is used locally, any address will do.");
		networkCombo = new Combo(box, SWT.BORDER);
		networkCombo.setItems(networkAddressNames);
		networkCombo.select(0);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		networkCombo.setLayoutData(gd);

	}

	private void createNoJDKWarningText(Composite parent) {
		Label jdkLabel = new Label(parent, SWT.NONE);
		jdkLabel.setText(" Upload Option requires JDK. JDK is not available!");
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.RIGHT;
		jdkLabel.setLayoutData(gd);

		final ControlDecoration jdkLabelDecorator = new ControlDecoration(
				jdkLabel, SWT.LEFT | SWT.CENTER);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_WARNING);
		Image image = fieldDecoration.getImage();
		jdkLabelDecorator.setImage(image);
		jdkLabelDecorator.setDescriptionText("Pls enter only numeric fields");

	}

	private void createDebugCheckbox(final Composite box) {
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

	private void createVerboseCheckbox(final Composite box) {
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
	}

	private void createAutoConnectCheckBox(final Composite box) {
		{
			Label l = new Label(box, SWT.NONE);
			l.setText("Autoconnect:");
			l.setToolTipText("If autoconnect is enabled, the launch will poll until a new java process is launched that fits the search criteria and then connect to this process.");
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
	}

	private void createAutoUploadCheckBox(final Composite box) {
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
	}

	private void createSelectProject(final Composite box) {
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
							box.getShell(), new WorkbenchLabelProvider());
					selectRules.setElements(ResourcesPlugin.getWorkspace()
							.getRoot().getProjects(0));
					selectRules.setTitle("Rules and Helper Project Selection");
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
	}

	private void createPortText(final Composite box) {
		{
			Label pidLabel = new Label(box, SWT.NONE);
			pidLabel.setText("Port:");
			pidLabel.setToolTipText("The process id of the JVM to connect to. If this is specified, it will be used. If it is not specified, the identify text or the auto connect feature will be used.");
			portText = new Text(box, SWT.NONE | SWT.BORDER);
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			portText.setLayoutData(gd);
			portText.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					setDirty();
				}
			});

			{
				Label agentInfoLabel = new Label(box, SWT.NONE);
				agentInfoLabel.setText("Copy java parameters:");

				copyJavaParameterButton = new Button(box, SWT.NONE);
				copyJavaParameterButton.setText("Copy Java Parameters");
				copyJavaParameterButton.setToolTipText(getAgentInfoLabel());
				GridData gd2 = new GridData();
				gd2.horizontalSpan = 2;
				copyJavaParameterButton.setLayoutData(gd2);
				copyJavaParameterButton
						.addSelectionListener(new SelectionAdapter() {
							@Override
							public void widgetSelected(SelectionEvent e) {
								Clipboard cp = new Clipboard(box.getDisplay());
								TextTransfer t = TextTransfer.getInstance();
								cp.setContents(
										new Object[] { getAgentInfoLabel() },
										new Transfer[] { t });
							}

						});
			}
		}
	}

	private void createPIDText(final Composite box) {
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

			pidSelectButton = new Button(box, SWT.NONE);
			pidSelectButton.setText("Select");
			pidSelectButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					PIDSelectionDialog pidDialog = new PIDSelectionDialog(box
							.getShell(), true);
					pidDialog.setVMs(JRTraceControllerService.getInstance()
							.getVMs());
					pidDialog.setFilterText(identifyText.getText());
					pidDialog.open();
					if (pidDialog.getReturnCode() == IDialogConstants.OK_ID) {
						if (pidDialog.useFilterText()) {
							identifyText.setText(pidDialog.getFilterText());
							pidText.setText("");
						} else

						{
							identifyText.setText("");
							pidText.setText(pidDialog.getPID());
						}
						setDirty();
					}
				}
			});
		}
	}

	private void createIdentifyByTextText(final Composite box) {
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
	}

	private void createServerText(final Composite box) {
		{
			Label description = new Label(box, SWT.NONE);
			description.setText("Target Machine:");
			description
					.setToolTipText("To connect to a target JVM on a different server enter the server address. Empty defaults to the local machine.");
			serverText = new Text(box, SWT.BORDER);
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			serverText.setLayoutData(gd);
			ModifyListener mod = new ModifyListener() {

				@Override
				public void modifyText(ModifyEvent e) {
					setDirty();

				}

			};
			serverText.addModifyListener(mod);

		}
	}

	private void createUploadAgentButton(final Composite box) {
		{

			uploadAgent = new Button(box, SWT.RADIO);
			uploadAgent.setText("Upload JRTrace Agent");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			uploadAgent.setLayoutData(gd);

			SelectionAdapter sel = new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {

					connectAgent.setSelection(!uploadAgent.getSelection());
					setDirty();

				}
			};

			uploadAgent.addSelectionListener(sel);

			Label description = new Label(box, SWT.NONE);
			description.setText("Requires JDK for Dev Env    ");
			description
					.setToolTipText("Select the target machine based on the process id. This option works without special startup parameters for the target process but requires the JDK for the development environment");

		}
	}

	private void createConnectAgentButton(final Composite box) {
		{

			connectAgent = new Button(box, SWT.RADIO);
			connectAgent.setText("Connect JRTrace Agent");
			GridData gd = new GridData();
			gd.horizontalSpan = 2;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			connectAgent.setLayoutData(gd);
			SelectionAdapter sel = new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					uploadAgent.setSelection(!connectAgent.getSelection());
					setDirty();
					updateUI();
				}
			};
			connectAgent.addSelectionListener(sel);

			Label description = new Label(box, SWT.NONE);
			description.setText("No JDK required");

		}
	}

	protected void updateUI() {
		boolean upload = uploadAgent.getSelection();

		identifyText.setEnabled(upload);
		pidText.setEnabled(upload);
		pidSelectButton.setEnabled(upload);
		autoconnectButton.setEnabled(upload);
		portText.setEnabled(!upload);
		serverText.setEnabled(!upload);
		copyJavaParameterButton.setEnabled(!upload);

		boolean noProjectSelected = rulesProjectName.getText().isEmpty();
		autouploadButton.setEnabled(!noProjectSelected);

	}

	private String getAgentInfoLabel() {

		String serverArgument = "";
		if (networkCombo != null) {
			String servername = networkCombo.getText();
			if (!servername.isEmpty()) {
				serverArgument = ",server=" + servername;
			}
		}

		return String.format("-javaagent:%s=port=%s,bootjar=%s%s", JarLocator
				.getJRTraceHelperAgent(),
				portText.getText().isEmpty() ? "<port>" : portText.getText(),
				JarLocator.getHelperLibJar(), serverArgument);
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
		configuration.setAttribute(BM_UPLOADAGENT, true);
		configuration.setAttribute(BM_UPLOADAGENT_PORT, 0);
		configuration.setAttribute(BM_SERVER_MACHINE, "");
		configuration.setAttribute(BM_MY_NETWORK_INTERFACE, "");

	}

	private void setDirty() {
		setDirty(true);
		updateUI();
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
			uploadAgent.setSelection(configuration.getAttribute(BM_UPLOADAGENT,
					true));
			connectAgent.setSelection(!configuration.getAttribute(
					BM_UPLOADAGENT, true));
			int port = configuration.getAttribute(BM_UPLOADAGENT_PORT, 0);

			if (port != 0)
				portText.setText(String.format("%d", port));
			String targetmachine = configuration.getAttribute(
					BM_SERVER_MACHINE, "");
			serverText.setText(targetmachine);

			String myinterface = configuration.getAttribute(
					BM_MY_NETWORK_INTERFACE, "");
			if (networkCombo != null)
				networkCombo.setText(myinterface);
		} catch (CoreException e) {
			throw new RuntimeException(
					"Problem in ConnectionTab.intializeFrom", e);
		}

		updateUI();
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(BM_TEXT_IDENT, identifyText.getText());
		configuration.setAttribute(BM_PID, pidText.getText());

		configuration
				.setAttribute(BM_PROJECT_IDENT, rulesProjectName.getText());
		configuration.setAttribute(BM_VERBOSE, verboseButton.getSelection());
		configuration.setAttribute(BM_AUTOUPLOAD,
				autouploadButton.isEnabled() ? autouploadButton.getSelection()
						: false);
		configuration.setAttribute(BM_AUTOCONNECT,
				autoconnectButton.getSelection());
		configuration.setAttribute(BM_DEBUG, debugButton.getSelection());
		configuration.setAttribute(BM_UPLOADAGENT, uploadAgent.getSelection());
		configuration.setAttribute(BM_SERVER_MACHINE, serverText.getText());
		int port = 0;
		try {
			port = Integer.parseInt(portText.getText());
		} catch (NumberFormatException e) {
			// do nothing
		}
		configuration.setAttribute(BM_UPLOADAGENT_PORT, port);
		if (networkCombo == null)
			configuration.setAttribute(BM_MY_NETWORK_INTERFACE, "");
		else {
			configuration.setAttribute(BM_MY_NETWORK_INTERFACE,
					networkCombo.getText());
		}

	}

	@Override
	public String getName() {
		return "JRTrace";
	}

}
