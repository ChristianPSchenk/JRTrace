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

/**
 * A bad hack to avoid to include the tools.jar into de.schenk.toolsjar Every
 * plugin that needs com.sun.tools should include a dependency to this bundle
 * and access the Activator from its start() method in its Activator. This will
 * cause the tools.jar to be located and copied in the de.jrtrace.toolsjar
 * before the bundle is actually started.
 */

public class Activator extends Plugin {

	private static BundleContext context;
	private static boolean hasJDK = false;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		String target = getFolderOfToolsjarBundle("lib");
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

			java.nio.file.Path source = java.nio.file.Paths.get(toolsjar
					.getAbsolutePath());
			java.nio.file.Path targetPath = java.nio.file.Paths.get(target
					+ "/tools.jar");
			if ((!Files.exists(targetPath))
					|| Files.size(source) != Files.size(targetPath))
				Files.copy(source, targetPath,
						StandardCopyOption.REPLACE_EXISTING);

			if (attachdll != null) {
				java.nio.file.Path dllsource = java.nio.file.Paths
						.get(attachdll.getAbsolutePath());
				java.nio.file.Path dlltarget = java.nio.file.Paths
						.get(getFolderOfToolsjarBundle("dll") + "/attach.dll");
				if ((!Files.exists(dlltarget))
						|| Files.size(dllsource) != Files.size(dlltarget))
					Files.copy(dllsource, dlltarget,
							StandardCopyOption.REPLACE_EXISTING);

			}

			hasJDK = true;
		}

	}

	public String getFolderOfToolsjarBundle(String libfolder) {
		try {
			String target = getJar("de.schenk.toolsjar", libfolder);
			return target;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Try to locate a file in the version specific folder of the toolsjar
	 * bundle and return it if successful
	 * 
	 * @param alternateToolsJarSources
	 *            a directory that may contains subdirectories for the various
	 *            java versions
	 * @param filename
	 *            the name of the file to retrieve.
	 * @return null, if file couldn't be found, the File is it exists
	 */
	public File findIncludedJavaSpecificFiles(String filename) {
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
	public File getJDKToolsJar() {
		final String javahome = System.getProperty("java.home");
		File toolsjar = new File(javahome, "../lib/tools.jar");
		if (toolsjar.exists())
			return toolsjar;
		else
			return null;

	}

	public File getAttachDll(String alternateToolsJarSource) {
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
		Activator.context = null;
	}

	static public String getJar(String bundleId, String jarpath)
			throws URISyntaxException, IOException {

		Bundle bundle = Platform.getBundle(bundleId);

		Path path = new Path(jarpath);

		URL fileURL = FileLocator.find(bundle, path, null);

		return new File(FileLocator.toFileURL(fileURL).toURI())
				.getAbsolutePath();
	}

}
