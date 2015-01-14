/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.jarpackager.IJarBuilder;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;

import de.schenk.jrtrace.ui.Activator;
import de.schenk.jrtrace.ui.handler.FileCollector;

public class JarUtil {

	/**
	 * Create a jar from the prjoect and store it in a temp folder
	 * 
	 * @param prj
	 *            contents to put in the jar (all files in this dir)
	 * @param parentShell
	 * @return the location of the jar (java.io.File)
	 * @throws RuntimeException
	 *             if anything fails here
	 */
	public static File createJar(IProject prj, Shell parentShell) {

		ArrayList<IFile> files = new ArrayList<IFile>();

		try {
			prj.accept(new FileCollector(files));
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		IJavaElement element = JavaCore.create(prj);
		JarPackageData description = new JarPackageData();
		if (element instanceof IJavaProject) {
			IJavaProject cf = (IJavaProject) element;
			Object[] pj = new Object[1];
			pj[0] = cf;
			description.setElements(pj);

		}

		else
			throw new RuntimeException("No JavaProject...");

		IJarBuilder jarBuilder = description.createPlainJarBuilder();
		description.setJarBuilder(jarBuilder);
		try {
			java.nio.file.Path temp = Files.createTempDirectory("jrtracejars");
			temp.toFile().deleteOnExit();

			IPath location = new Path(temp.toAbsolutePath() + "/"
					+ prj.getName() + ".jar");
			description.setJarLocation(location);
		
		} catch (IOException e1) {
			throw new RuntimeException("Not possible to create temp dir", e1);
		}
		description.setIncludeDirectoryEntries(false);
		// description.setManifestMainClass(t);
		description.setSaveManifest(true);

		description.setManifestLocation(new Path("bin/META-INF/mf.mf"));
		description.setManifestVersion("1.0");

		IJarExportRunnable runnable = description
				.createJarExportRunnable(parentShell);
		try {
			new ProgressMonitorDialog(parentShell).run(true, true, runnable);
		} catch (InvocationTargetException e) {
			IStatus status = runnable.getStatus();
			Activator.getInstance().getLog().log(status);
			throw new RuntimeException("Error during jar creation.", e);
		} catch (InterruptedException e) {
			IStatus status = runnable.getStatus();
			Activator.getInstance().getLog().log(status);
			throw new RuntimeException("Error during jar creation.", e);
		}
		return description.getAbsoluteJarLocation().toFile();
	}

}
