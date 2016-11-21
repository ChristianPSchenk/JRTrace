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

	static BundleContext getContext() {
		return context;
	}

	private static File toolsjar;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		JDKInitActivator.context = bundleContext;

		File toolsjar = getJDKToolsJar();

		File attachdll = null;
		if (toolsjar == null) {
			toolsjar = findIncludedJavaSpecificFiles("tools.jar");
			attachdll = findIncludedJavaSpecificFiles("attach.dll");
			if (attachdll == null) {
				toolsjar = null;
			}
		}

		if (toolsjar == null) {
			
			hasJDK = false;
			
		} else {
		
			this.toolsjar=toolsjar;
			hasJDK = true;
		}

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
	private File getJDKToolsJar() {
		final String javahome = System.getProperty("java.home");
		File toolsjar = new File(javahome, "../lib/tools.jar");
		if (toolsjar.exists())
			return toolsjar;
		else
			return null;

	}

	private File getAttachDll(String alternateToolsJarSource) {
		final String javaversion = System.getProperty("java.version");
		File attachdll = new File(alternateToolsJarSource + "/" + javaversion
				+ "/attach.dll");
		if (!attachdll.exists()) {
			throw new RuntimeException("Expecting the attach.dll in "
					+ attachdll.toString()
					+ " when starting JRTrace on a non-JDK JRE");
		}
		return attachdll;

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
