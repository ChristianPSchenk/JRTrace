package de.schenk.jrtrace.example;

import java.io.IOException;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.schenk.jrtrace.service.ClassUtil;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.JRTraceMessageListener;
import de.schenk.jrtrace.service.ui.dialogs.PIDSelectionDialog;

public class ExampleApp implements IApplication {

	private Text stackDisplay;

	@Override
	public Object start(IApplicationContext context) throws Exception {

		Shell topLevelShell = new Shell(Display.getDefault());
		topLevelShell.setLayout(new GridLayout(1, false));
		topLevelShell.setText("JRTrace Stacktrace Example");
		Button button = new Button(topLevelShell, SWT.PUSH);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getStacktrace();
			}
		});
		button.setText("Get a Stacktrace...");
		stackDisplay = new Text(topLevelShell, SWT.BORDER | SWT.V_SCROLL);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.verticalAlignment = SWT.FILL;
		stackDisplay.setLayoutData(gridData);
		// getAStackTrace();

		topLevelShell.open();
		while (!topLevelShell.isDisposed()) {
			topLevelShell.getDisplay().readAndDispatch();
		}
		return 0;
	}

	private boolean getStacktrace() {
		String pid = null;
		PIDSelectionDialog dialog = new PIDSelectionDialog(null, false);
		int result = dialog.open();
		if (result == IDialogConstants.OK_ID) {
			pid = dialog.getPID();

		} else {
			return false;
		}

		final IJRTraceVM jvm = JRTraceControllerService.getInstance()
				.getMachine(pid, null);
		jvm.attach();

		byte[][] allClasses = new byte[1][];

		try {
			allClasses[0] = ClassUtil.getClassBytes(StackTraceUtility.class);
		} catch (IOException e) {
			throw new RuntimeException("Internal problem...");
		}
		jvm.installJRTraceClasses(allClasses);

		jvm.addMessageListener(new JRTraceMessageListener() {

			@Override
			public void handleMessageReceived(Object message) {
				final String stack = (String) message;
				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						stackDisplay.setText(stack);
						jvm.detach();
					}
				});
			}
		});

		jvm.invokeMethodAsync(null, StackTraceUtility.class.getName(),
				"getStackTrace");

		return true;
	}

	@Override
	public void stop() {
		// do nothing

	}

}
