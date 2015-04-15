/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import de.schenk.jrtrace.ui.wizard.RunJavaWizard;

public class RunJavaHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		RunJavaWizard wizard = new RunJavaWizard();

		IWorkbenchPart part = HandlerUtil.getActivePart(event);

		WizardDialog dialog = new WizardDialog(part.getSite().getShell(),
				wizard);
		dialog.create();
		IMember selectedFunction = getSelectedFunction(event);
		if (selectedFunction != null) {

			wizard.setSelection(selectedFunction.getDeclaringType()
					.getFullyQualifiedName(), selectedFunction.getElementName());
		}

		dialog.open();

		return null;
	}

	private IMember getSelectedFunction(ExecutionEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		if (currentSelection instanceof IStructuredSelection) {
			IStructuredSelection currentStructuredSelection = (IStructuredSelection) currentSelection;
			Object element = currentStructuredSelection.getFirstElement();
			if (element instanceof IMember) {

				return (IMember) element;
			}
			Object adapted = Platform.getAdapterManager().getAdapter(element,
					IMember.class);
			if (adapted != null) {

				return (IMember) element;
			}
		}

		ITextEditor editor = (ITextEditor) page.getActiveEditor();
		IJavaElement elem = JavaUI.getEditorInputJavaElement(editor
				.getEditorInput());
		if (elem instanceof ICompilationUnit) {
			ITextSelection sel = (ITextSelection) editor.getSelectionProvider()
					.getSelection();
			IJavaElement selected;
			try {
				selected = ((ICompilationUnit) elem).getElementAt(sel
						.getOffset());
				if (selected != null
						&& selected.getElementType() == IJavaElement.METHOD) {

					return (IMethod) selected;
				}
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}

		}

		return null;
	}

}
