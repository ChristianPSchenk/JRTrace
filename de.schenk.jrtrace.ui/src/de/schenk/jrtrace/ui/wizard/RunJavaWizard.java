/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.wizard;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.launch.JRTraceLaunchUtils;

public class RunJavaWizard extends Wizard {

	private String theClassLoader = "";
	private RunJavaPage scriptPage;
	private String mainClass = "";
	private String runMethod = "";

	String getTheClassLoader() {
		return theClassLoader;
	}

	void setTheClassLoader(String theClassLoader) {
		this.theClassLoader = theClassLoader;
	}

	public RunJavaWizard() {

		this.setWindowTitle("Execute static void method on any JRTrace Class");

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

		List<JRTraceDebugTarget> jrtraceTargets = getJRTraceTargets();

		for (JRTraceDebugTarget btarget : jrtraceTargets) {
			btarget.runJava(theClassLoader, mainClass, runMethod);
		}
		return true;
	}

	public void setMainClass(String text) {
		if (text != null) {
			this.mainClass = text;
		}

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

	public void setSelection(String className, String functionName) {

		setMainClass(className);
		setRunMethod(functionName);
		scriptPage.updatePageControls();

	}
}
