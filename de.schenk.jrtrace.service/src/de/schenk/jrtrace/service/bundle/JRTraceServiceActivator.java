/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.bundle;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.schenk.jrtrace.jdk.init.Activator;

public class JRTraceServiceActivator extends Plugin implements BundleActivator {

	public static final String ID = "de.schenk.jrtrace.service";
	static JRTraceServiceActivator thePlugin;

	@Override
	public void start(BundleContext context) throws Exception {
		// important to do the proper startup. Force activation of jdk.init
		// first so
		// the tools.jar is copied to the toolsjar bundle, which is activated
		// later.
		Activator.hasJDK();

		thePlugin = this;

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		thePlugin = null;
	}

	public static JRTraceServiceActivator getActivator() {
		return thePlugin;
	}
}
