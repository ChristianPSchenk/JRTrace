/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.bundle;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import de.schenk.jrtrace.jdk.init.Activator;

public class JRTraceServiceActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		// important to do the proper startup. Force activation of jdk.init
		// first so
		// the tools.jar is copied to the toolsjar bundle, which is activated
		// later.
		Activator.hasJDK();

	}

	@Override
	public void stop(BundleContext context) throws Exception {

	}

}
