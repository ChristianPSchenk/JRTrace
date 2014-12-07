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
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
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

			getLog().log(
					new Status(Status.ERROR, "de.schenk.toolsjar", Status.OK,
							"This bundle requires a JDK defined. Didn't find tools.jar at "
									+ toolsjar.toString(), null));
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog
							.openError(
									null,
									"No JDK!",
									"The jrtrace plugins require that the product is started using a JDK. It wasn't possible to locate tools.jar in the installation "
											+ javahome
											+ ". Ensure that this tool starts with a JDK!");

				}
			});

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
		}

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

		String pathString = FileLocator.resolve(fileURL).toURI()
				.toASCIIString();
		return pathString.replace("file:/", "");
	}

	public void test() {
		System.out.println("init");

	}
}
