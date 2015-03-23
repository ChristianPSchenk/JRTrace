/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.wizard;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.schenk.jrtrace.ui.Activator;
import de.schenk.jrtrace.ui.java.JRTraceClassPathContainer;

public class JRTraceProjectCreationJob extends Job {

	private String projectName;
	private String packageName;
	private String className;

	/**
	 * 
	 * @param theProjectName
	 *            mandatory: name of the project
	 * @param thePackage
	 *            package name, can be null
	 * @param theClass
	 *            if not null: create a helper class.
	 */
	public JRTraceProjectCreationJob(String theProjectName, String thePackage,
			String theClass) {
		super("Creating JRTrace Project " + theProjectName);
		projectName = theProjectName;
		this.packageName = thePackage;
		if (packageName == null || packageName.isEmpty())
			packageName = "de.schenk.jrtrace.libs";
		this.className = theClass;

	}

	@Override
	protected IStatus run(IProgressMonitor progressMonitor) {
		try {

			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			IProject project = root.getProject(projectName);
			if (project.exists()) {
				showError("Project exists already.");
				return Status.OK_STATUS;
			}
			createJavaProjectAndLibraries(progressMonitor, project);

		} catch (JavaModelException e) {
			showError("Exception while creating the project: " + e.getMessage());

		} catch (CoreException e) {
			showError("Exception while creating the project: " + e.getMessage());

		}

		return Status.OK_STATUS;
	}

	private void createJavaProjectAndLibraries(
			IProgressMonitor progressMonitor, IProject project)
			throws CoreException, JavaModelException {
		project.create(progressMonitor);
		project.open(progressMonitor);

		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = JavaCore.NATURE_ID;
		description.setNatureIds(newNatures);
		project.setDescription(description, progressMonitor);

		IJavaProject javaProject = JavaCore.create(project);

		IFolder binFolder = project.getFolder("bin");
		binFolder.create(false, true, null);
		javaProject.setOutputLocation(binFolder.getFullPath(), progressMonitor);

		IFolder sourceFolder = project.getFolder("src");
		sourceFolder.create(false, true, progressMonitor);

		IPackageFragmentRoot root = javaProject
				.getPackageFragmentRoot(sourceFolder);

		IClasspathEntry srcClassPathEntry = JavaCore.newSourceEntry(root
				.getPath());

		IClasspathEntry entry = JavaCore.newContainerEntry(new Path(
				JRTraceClassPathContainer.JRTRACE_CLASSPATH_CONTAINER_ID));
		Set<IClasspathEntry> entries = new HashSet<IClasspathEntry>();
		entries.add(entry);

		entries.add(srcClassPathEntry);

		IClasspathEntry jvmEntry = JavaCore.newContainerEntry(JavaRuntime
				.newDefaultJREContainerPath());
		entries.add(jvmEntry);

		if (className != null && !className.isEmpty()) {
			createSampleHelperLib(javaProject, sourceFolder);
		}

		// LibraryLocation[] locations=
		// JavaRuntime.getLibraryLocations(vmInstall);
		// for (LibraryLocation element : locations) {
		// entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(),
		// null, null));
		// }
		//
		javaProject.setRawClasspath(
				entries.toArray(new IClasspathEntry[entries.size()]),
				progressMonitor);
	}

	private void createSampleHelperLib(IJavaProject javaProject,
			IFolder sourceFolder) throws JavaModelException {
		IPackageFragment pack = javaProject
				.getPackageFragmentRoot(sourceFolder).createPackageFragment(
						packageName, false, null);

		String data = getResourceAsString("SampleHelperLib.resource");

		data = data.replaceAll("###package###", packageName);
		data = data.replaceAll("###classname###", className);

		pack.createCompilationUnit(className + ".java", data, false, null);
	}

	private String getResourceAsString(String string) {
		String data = "";

		URL codeStream = this.getClass().getResource(string);

		try {
			URI codeURI = FileLocator.toFileURL(codeStream).toURI();
			byte[] bytes = Files.readAllBytes(Paths.get(codeURI));
			data = new String(bytes);
		} catch (IOException e) {
			throw new RuntimeException("SampleHelperLib missing", e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("SampleHelperLib missing", e);
		}
		return data;
	}

	private void showError(final String message) {
		showError(message, null);
	}

	private void showError(final String message, final Exception e) {
		Activator
				.getInstance()
				.getLog()
				.log(new Status(IStatus.ERROR, Activator.BUNDLE_ID, message, e));
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {

				MessageDialog.openError(Display.getDefault().getActiveShell(),
						"Error creating the JRTrace Project", message + "\n"
								+ (e != null ? e.toString() : ""));

			}

		});

	}

}
