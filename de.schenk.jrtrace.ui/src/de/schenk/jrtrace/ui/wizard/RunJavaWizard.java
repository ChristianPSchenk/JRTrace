/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.wizard;

import org.eclipse.jface.wizard.Wizard;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;

public class RunJavaWizard extends Wizard {

	private String theClassLoader = "";
	private RunJavaPage scriptPage;
	private String mainClass = "";
	private String runMethod = "";
	private JRTraceDebugTarget debugTarget = null;
	static private boolean didAlreadyRunInThisSession=false;

	String getTheClassLoader() {
		return theClassLoader;
	}

	void setTheClassLoader(String theClassLoader) {
		this.theClassLoader = theClassLoader;
	}

	public RunJavaWizard() {

		this.setWindowTitle("Execute Method on JRTrace Class");

	}

	public void addPages() {
		scriptPage = new RunJavaPage();
		addPage(scriptPage);

	}

	public boolean canFinish() {
		boolean allPagesComplete = super.canFinish();
		if (allPagesComplete && debugTarget != null)
			return true;

		return false;

	};
	
	/**
	 *  Runs the last java command again. Will only run again if the RunJavaWizard
	 *  was finished at least once in the same eclipse session already.
	 *  
	 *   
	 * @return true, if the last java command selected from the wizard was run again. false, if no command was selected before and rerun fails.
	 */
	public boolean reRunJava()
	{
		if(didAlreadyRunInThisSession&&theClassLoader!=null&& mainClass!=null&& runMethod!=null)
		{
			debugTarget.runJava(theClassLoader, mainClass, runMethod);
			return true;
		}
		return false;
	}

	@Override
	public boolean performFinish() {

		scriptPage.storeSettings();

		debugTarget.runJava(theClassLoader, mainClass, runMethod);
		didAlreadyRunInThisSession=true;
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

	public JRTraceDebugTarget getDebugTarget() {
		return debugTarget;
	}

	public void setDebugTarget(JRTraceDebugTarget firstElement) {

		debugTarget = firstElement;
	}
}
