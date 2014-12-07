/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
 
public class JRTraceClasPathContainerInitializer extends
		ClasspathContainerInitializer {

	@Override
	public void initialize(IPath arg0, IJavaProject project) throws CoreException {
		   JRTraceClassPathContainer container = new JRTraceClassPathContainer();
           
        	   
           JavaCore.setClasspathContainer(arg0, new IJavaProject[] {project},
           new IClasspathContainer[] {container}, null);
           //} else {
        	//   Activator.getInstance().getLog().log(new Status(IStatus.ERROR,Activator.BUNDLE_ID,"Problem installing jrtrace classpath container"));
           
           //}
		
	}
	
	

}
