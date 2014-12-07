/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import de.schenk.jrtrace.ui.wizard.RunJavaWizard;

public class RunJavaHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		Object f = (((IStructuredSelection) sel).getFirstElement());
		if (f == null)
			return null;
		IProject c = (IProject) Platform.getAdapterManager().getAdapter(f,
				IProject.class);

		if (c != null) {

			RunJavaWizard wizard = new RunJavaWizard(c);

			IWorkbenchPart part = HandlerUtil.getActivePart(event);

			WizardDialog dialog = new WizardDialog(part.getSite().getShell(),
					wizard);
			dialog.create();
			dialog.open();
		}
		return null;
	}

	/**
	 * works only for java projects.
	 */
	@Override
	public void setEnabled(Object evaluationContext) {
		boolean enabled = EvaluationContextUtil
				.isJavaProject(evaluationContext);

		setBaseEnabled(enabled);

	}

}
