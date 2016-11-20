/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.schenk.jrtrace.ui.util.JDTUtil;
import de.schenk.jrtrace.ui.wizard.RunJavaWizard;

public class RunJavaHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		RunJavaWizard wizard = new RunJavaWizard();

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		WizardDialog dialog = new WizardDialog(part.getSite().getShell(),
				wizard);
		dialog.create();
		IMember selectedFunction = JDTUtil.getSelectedFunction(event);
		if (selectedFunction != null) {

			wizard.setSelection(selectedFunction.getDeclaringType()
					.getFullyQualifiedName(), selectedFunction.getElementName());
		}

		dialog.open();

		return null;
	}

}
