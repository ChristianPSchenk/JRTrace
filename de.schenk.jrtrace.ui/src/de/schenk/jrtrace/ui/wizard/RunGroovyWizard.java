/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.wizard;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.wizard.Wizard;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.launch.JRTraceLaunchUtils;

public class RunGroovyWizard extends Wizard {

	private String theClassLoader = "";
	private IFile theFile;
	private GroovyScriptPage scriptPage;

	IFile getTheFile() {
		return theFile;
	}

	void setTheFile(IFile theFile) {
		this.theFile = theFile;
	}

	String getTheClassLoader() {
		return theClassLoader;
	}

	void setTheClassLoader(String theClassLoader) {
		this.theClassLoader = theClassLoader;
	}

	public RunGroovyWizard(IFile f) {
		theFile = f;
		this.setWindowTitle("Run Groovy Script on JRTrace Target");

	}

	@Override
	public void addPages() {
		scriptPage = new GroovyScriptPage();
		addPage(scriptPage);

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
			btarget.runGroovy(theFile, theClassLoader);
		}
		return true;
	}

	private List<JRTraceDebugTarget> getJRTraceTargets() {
		List<JRTraceDebugTarget> jrtraceTargets = JRTraceLaunchUtils
				.getJRTraceDebugTargets();
		return jrtraceTargets;
	}

}
