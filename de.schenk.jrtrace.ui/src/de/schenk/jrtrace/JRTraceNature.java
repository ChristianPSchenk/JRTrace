/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

import de.schenk.jrtrace.ui.JRTraceUIActivator;

public class JRTraceNature implements IProjectNature {

	IProject project;

	@Override
	public void configure() throws CoreException {
		addJRTraceNature(project);
	}

	static public void addJRTraceNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		Set<String> newNatures = new HashSet<String>();
		newNatures.addAll(Arrays.asList(natures));

		newNatures.add(JRTraceUIActivator.NATURE_ID);

		description.setNatureIds(newNatures.toArray(new String[0]));
		project.setDescription(description, new NullProgressMonitor());
	}

	static public void removeJRTraceNature(IProject project)
			throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		Set<String> newNatures = new HashSet<String>();
		newNatures.addAll(Arrays.asList(natures));

		newNatures.remove(JRTraceUIActivator.NATURE_ID);

		description.setNatureIds(newNatures.toArray(new String[0]));
		project.setDescription(description, new NullProgressMonitor());
	}

	@Override
	public void deconfigure() throws CoreException {
		removeJRTraceNature(project);

	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;

	}

}
