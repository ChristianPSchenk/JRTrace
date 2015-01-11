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

		final String javahome = System.getProperty("java.home");
		File toolsjar = new File(javahome, "../lib/tools.jar");
		if (!toolsjar.exists()) {

			hasJDK = false;
		} else {
			String target = getJar("de.schenk.toolsjar", "lib");

			java.nio.file.Path source = java.nio.file.Paths.get(toolsjar
					.getAbsolutePath());
			java.nio.file.Path targetPath = java.nio.file.Paths.get(target
					+ "/tools.jar");
			if ((!Files.exists(targetPath))
					|| Files.size(source) != Files.size(targetPath))
				Files.copy(source, targetPath,
						StandardCopyOption.REPLACE_EXISTING);
			hasJDK = true;
		}

	}

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

		
		return new File(FileLocator.toFileURL(fileURL).toURI()).getAbsolutePath();
	}

}
