/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.handler;

import java.io.File;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.launch.JRTraceLaunchUtils;
import de.schenk.jrtrace.ui.util.JarUtil;

public class InstallHelperJarHandler extends AbstractHandler implements
		IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		Object f = (((IStructuredSelection) sel).getFirstElement());
		if (f == null)
			return null;
		IProject c = (IProject) Platform.getAdapterManager().getAdapter(f,
				IProject.class);

		if (c != null) {

			File jarFile = JarUtil.createJar(c,
					HandlerUtil.getActiveShell(event));

			List<JRTraceDebugTarget> jrtraceTargets = JRTraceLaunchUtils
					.getJRTraceDebugTargets();
			for (JRTraceDebugTarget btarget : jrtraceTargets) {
				btarget.installJar(jarFile);
			}
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
