/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.util;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class JDTUtil {

	/**
	 * Try to identify a java method based on the event:</br>
	 * First check the current selection and see if it corresponds to a Java Method. Second check the selection in the currently
	 * active Editor.
	 * 
	 * @param event the event
	 * @return an IMember if any can be identified, null if not.
	 */
	public static IMember getSelectedFunction(ExecutionEvent event) {
		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		ISelection currentSelection = HandlerUtil.getCurrentSelection(event);
		IMember member = getSelectedFunction(currentSelection);
		if (member != null)
			return member;
		
		IEditorPart editor = page.getActiveEditor();
		if(editor instanceof ITextEditor)
			{
			member=getSelectedFunction((ITextEditor)editor);
			}
		return member;

	}

	public static IMember getSelectedFunction(ITextEditor editor) {
		IJavaElement elem = JavaUI.getEditorInputJavaElement(editor
				.getEditorInput());

		
		ITextSelection sel = (ITextSelection) editor.getSelectionProvider()
				.getSelection();
		if (elem instanceof ClassFile) {
			IJavaElement selected;
			try {
				selected = ((ClassFile) elem).getElementAt(sel.getOffset());
				if (selected != null
						&& selected.getElementType() == IJavaElement.METHOD) {

					return (IMethod) selected;
				}
			} catch (JavaModelException e) {
				throw new RuntimeException(e);
			}

		}

		if (elem instanceof ICompilationUnit) {

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

	public static IMember getSelectedFunction(ISelection currentSelection) {
		if (currentSelection instanceof IStructuredSelection) {
			IStructuredSelection currentStructuredSelection = (IStructuredSelection) currentSelection;
			Object element = currentStructuredSelection.getFirstElement();
			if (element == null)
				return null;
			if (element instanceof IMember) {

				return (IMember) element;
			}
			Object adapted = Platform.getAdapterManager().getAdapter(element,
					IMember.class);
			if (adapted != null) {

				return (IMember) element;
			}
		}
		return null;
	}

}
