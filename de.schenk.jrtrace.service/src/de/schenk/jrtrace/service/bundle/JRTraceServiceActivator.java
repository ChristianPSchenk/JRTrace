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
		Activator x=new Activator();
		x.test();

	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}

}
