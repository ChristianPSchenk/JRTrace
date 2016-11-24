/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.jdk.init;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import de.schenk.jrtrace.jdk.init.utils.BundleFilesUtil;

/**
 * 
 */

public class JDKInitActivator extends Plugin {

	private static BundleContext context;
	private static boolean hasJDK = false;
	private static File toolsjar=null;
	private static boolean isJava9=false;
	static BundleContext getContext() {
		return context;
	}

	private static File java9homedir;

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		JDKInitActivator.context = bundleContext;

	
		
		File toolsjar = findToolsJar();
		File java9bin = findJDK9HomeDir();

		if (toolsjar != null)  {
		
			this.toolsjar=toolsjar;
			hasJDK = true;
			isJava9=false;
		}
		if(java9bin!=null)
		{
			this.java9homedir=java9bin;
			isJava9=true;
			hasJDK=true;
		}
		

	}

	/**
	 * Returns the home directory of a JDK that can be used to use the jdk.attach package.
	 * 1) First checks the running JVM: if it is a JDK, returns the java.home
	 * 2) Second checks if the de.schenk.toolsjar\bin\java9 folder contains a valid JDK (java.exe in bin\java.exe) and returns if available.
	 * 
	 * @return
	 */
	private File findJDK9HomeDir() {
		final String javahome = System.getProperty("java.home");
		File jdkbin=new File(javahome+File.separator+"bin");
		if(new File(jdkbin,"attach.dll").exists() && new File(jdkbin,"java.exe").exists()) return new File(javahome);
		
		 File java9dir=new File(BundleFilesUtil.getFile("de.schenk.toolsjar", "java9"));
		 if(!java9dir.exists()) return null;
		 File javaexe=new File(java9dir,"bin"+File.separator+"java.exe");
		 if(!javaexe.exists()) return null;
		return java9dir;
				
	}

	private File findToolsJar() {
		File toolsjar = getJDKToolsJarFromRunningJVM();

		File attachdll = null;
		if (toolsjar == null) {
			toolsjar = findIncludedJavaSpecificFiles("tools.jar");
			attachdll = findIncludedJavaSpecificFiles("attach.dll");
			if (attachdll == null) {
				toolsjar = null;
			}
		}
		return toolsjar;
	}
	
	static public File getToolsJar()
	{
		return toolsjar;
	}

	private String getFolderOfToolsjarBundle(String libfolder) {
	
			String target = BundleFilesUtil.getFile("de.schenk.toolsjar", libfolder);
			return target;
	
	}

	/**
	 * Try to locate a file in the version specific folder of the toolsjar
	 * bundle and return it if successful
	 * 
	 * 
	 * @param filename
	 *            the name of the file to retrieve.
	 * @return null, if file couldn't be found, the File is it exists
	 */
	private File findIncludedJavaSpecificFiles(String filename) {
		String alternateToolsJarSources = getFolderOfToolsjarBundle("lib");
		final String javaversion = System.getProperty("java.version");

		File toolsjar = new File(alternateToolsJarSources + "/" + javaversion
				+ "/" + filename);

		if (toolsjar.exists())
			return toolsjar;
		return null;

	}


	/**
	 * 
	 * @return a file representing the tools.jar jar from the currently executed
	 *         JDK, null if not run on a JDK.
	 */
	private File getJDKToolsJarFromRunningJVM() {
		final String javahome = System.getProperty("java.home");
		File toolsjar = new File(javahome, "../lib/tools.jar");
		if (toolsjar.exists())
			return toolsjar;
		else
			return null;

	}

	
	static public File getJDK9HomeDir()
	{
		return java9homedir;
	}

	/**
	 * Only valid if hasJDK returns true.
	 * @return true if a JDK9 is available, false if a JDK8 or below is available.
	 *         
	 */
	public static boolean isJava9JDK() {
		return isJava9;
	}
	/**
	 * 
	 * @return true, if connecting via the attach API (using the PID) is
	 *         possible.
	 */
	public static boolean hasJDK() {
		return hasJDK;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		JDKInitActivator.context = null;
	}

	



}
