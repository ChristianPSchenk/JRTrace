/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.schenk.jrtrace.ui.wizard.RunGroovyWizard;

public class RunGroovyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		ISelection sel = HandlerUtil.getCurrentSelection(event);
		IFile f = (IFile) (((IStructuredSelection) sel).getFirstElement());
		RunGroovyWizard wizard = new RunGroovyWizard(f);

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		// wizard.init(part.getSite().getWorkbenchWindow().getWorkbench(),
		// (IStructuredSelection)selection);
		// Instantiates the wizard container with the wizard and opens it
		WizardDialog dialog = new WizardDialog(part.getSite().getShell(),
				wizard);
		dialog.create();
		dialog.open();

		return null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		IEvaluationContext e = (IEvaluationContext) evaluationContext;
		Object selObj = e.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		ISelection sel = null;
		if (selObj instanceof ISelection) {
			sel = (ISelection) selObj;
		} else {
			return;
		}
		Object obj = null;
		if (sel instanceof IStructuredSelection) {
			obj = ((IStructuredSelection) sel).getFirstElement();
		}
		if (!(obj instanceof IFile)) {
			setBaseEnabled(false);
			return;
		}

		IFile file = (IFile) obj;
		if (file.getFileExtension().equalsIgnoreCase("groovy")) {
			setBaseEnabled(true);
			return;
		}
		setBaseEnabled(false);
	}

}
