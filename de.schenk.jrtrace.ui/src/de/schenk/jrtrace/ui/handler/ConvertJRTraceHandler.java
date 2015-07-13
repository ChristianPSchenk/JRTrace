/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.handler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.schenk.jrtrace.JRTraceNature;
import de.schenk.jrtrace.ui.java.JRTraceClassPathContainer;

public class ConvertJRTraceHandler extends AbstractHandler implements IHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		Object f = (((IStructuredSelection) sel).getFirstElement());
		if (f == null)
			return null;
		IProject project = (IProject) Platform.getAdapterManager().getAdapter(
				f, IProject.class);

		if (project != null) {
			try {
				JRTraceNature.addJRTraceNature(project);

				Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();

				IJavaProject javaProject = JavaCore.create(project);
				IClasspathEntry[] rawclasspath = javaProject.getRawClasspath();
				entries.addAll(Arrays.asList(rawclasspath));

				IClasspathEntry entry = JavaCore
						.newContainerEntry(new Path(
								JRTraceClassPathContainer.JRTRACE_CLASSPATH_CONTAINER_ID));
				entries.add(entry);
				javaProject.setRawClasspath(
						entries.toArray(new IClasspathEntry[entries.size()]),
						null);

			} catch (CoreException e) {
				throw new RuntimeException(e);
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
