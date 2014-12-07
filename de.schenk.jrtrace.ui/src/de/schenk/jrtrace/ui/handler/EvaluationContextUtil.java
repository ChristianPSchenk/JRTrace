/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.handler;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISources;

public class EvaluationContextUtil {
	/**
	 * 
	 * @param evaluationContext
	 * @return true, if the selection of this evaluation context is a java
	 *         project
	 */
	static public boolean isJavaProject(Object evaluationContext) {

		IEvaluationContext e = (IEvaluationContext) evaluationContext;
		Object selVar = e.getVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME);
		Object obj = null;
		if (selVar instanceof IStructuredSelection) {
			obj = ((IStructuredSelection) selVar).getFirstElement();
		}

		if (obj == null) {
			return false;
		}

		IJavaProject project = (IJavaProject) Platform.getAdapterManager()
				.getAdapter(obj, IJavaProject.class);
		if (project == null) {
			return false;
		}
		return true;
	}
}
