/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.java;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

import de.schenk.jrtrace.service.JarLocator;
import de.schenk.jrtrace.ui.JRTraceUIActivator;

// FIXME: after close and reload teh class path container is gone
public class JRTraceClassPathContainer implements IClasspathContainer {

	public static final String JRTRACE_CLASSPATH_CONTAINER_ID = "de.schenk.jrtrace.JRTRACE_CONTAINER";

	@Override
	public IClasspathEntry[] getClasspathEntries() {

		try {
			String jrtraceAgentDir = JarLocator.getJRTraceHelperAgent();
			String jrtraceAgentSource = JarLocator
					.getJRTraceHelperAgentSource();
			String helperlibDir = JarLocator.getHelperLibJar();
			String helperlibSource = JarLocator.getHelperLibSource();

			IClasspathEntry[] classPaths = new IClasspathEntry[2];
			classPaths[0] = JavaCore.newLibraryEntry(new Path(jrtraceAgentDir),
					new Path(jrtraceAgentSource), null);
			classPaths[1] = JavaCore.newLibraryEntry(new Path(helperlibDir),
					new Path(helperlibSource), null);

			return classPaths;
		} catch (RuntimeException e) {
			JRTraceUIActivator
					.getInstance()
					.getLog()
					.log(new Status(
							IStatus.ERROR,
							JRTraceUIActivator.BUNDLE_ID,
							"Error creating jrtrace classpath entries in JRTraceClassPathContainer",
							e));
			return new IClasspathEntry[] {};
		}

	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "JRTrace Libraries";
	}

	@Override
	public int getKind() {

		return K_APPLICATION;
	}

	@Override
	public IPath getPath() {

		return new Path(JRTRACE_CLASSPATH_CONTAINER_ID);
	}

}
