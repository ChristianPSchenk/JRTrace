/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.wizard;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewProjectWizard extends Wizard implements INewWizard {

	private NewMainPage mainPage;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("Create JRTrace Project");

	}

	@Override
	public void addPages() {
		mainPage = new NewMainPage("JRTrace");
		addPage(mainPage);
	}

	@Override
	public boolean canFinish() {

		return mainPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {

		Job creator = new JRTraceProjectCreationJob(mainPage.getProjectName(),
				mainPage.getPackageName(), mainPage.getClassName());
		creator.schedule();
		return true;
	}

}
