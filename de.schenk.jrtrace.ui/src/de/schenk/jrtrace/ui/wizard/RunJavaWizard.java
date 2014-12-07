/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.wizard;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.PlatformUI;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.launch.JRTraceLaunchUtils;
import de.schenk.jrtrace.ui.util.JarUtil;

public class RunJavaWizard extends Wizard {

	private String theClassLoader = "";
	private IProject theProject;
	private RunJavaPage scriptPage;
	private String mainClass;
	private String runMethod;

	public IProject getProject() {
		return theProject;
	}

	public void setProject(IProject theProject) {
		this.theProject = theProject;
	}

	String getTheClassLoader() {
		return theClassLoader;
	}

	void setTheClassLoader(String theClassLoader) {
		this.theClassLoader = theClassLoader;
	}

	public RunJavaWizard(IProject project) {
		setProject(project);
		this.setWindowTitle("Run Java on Target");

	}

	public void addPages() {
		scriptPage = new RunJavaPage();
		addPage(scriptPage);

	}

	private List<JRTraceDebugTarget> getJRTraceTargets() {
		List<JRTraceDebugTarget> jrtraceTargets = JRTraceLaunchUtils
				.getJRTraceDebugTargets();
		return jrtraceTargets;
	}

	public boolean canFinish() {
		boolean allPagesComplete = super.canFinish();
		if (allPagesComplete && !getJRTraceTargets().isEmpty())
			return true;

		return false;

	};

	@Override
	public boolean performFinish() {

		scriptPage.storeSettings();

		File jarFile = JarUtil.createJar(this.getProject(), PlatformUI
				.getWorkbench().getActiveWorkbenchWindow().getShell());

		List<JRTraceDebugTarget> jrtraceTargets = getJRTraceTargets();

		for (JRTraceDebugTarget btarget : jrtraceTargets) {
			btarget.runJava(jarFile, theClassLoader, mainClass, runMethod);
		}
		return true;
	}

	public void setMainClass(String text) {
		this.mainClass = text;

	}

	public String getMainClass() {
		return this.mainClass;
	}

	public String getRunMethod() {
		return runMethod;
	}

	public void setRunMethod(String text) {
		this.runMethod = text;

	}
}
