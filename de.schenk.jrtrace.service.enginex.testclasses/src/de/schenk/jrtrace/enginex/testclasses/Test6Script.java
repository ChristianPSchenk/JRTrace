/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import org.osgi.framework.FrameworkEvent;

import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test6", classloaderpolicy = XClassLoaderPolicy.NAMED, classloadername = "org.osgi.framework.Bundle")
public class Test6Script {

	@XMethod(names = "test6")
	public void testinstrumentation() {

		int x = FrameworkEvent.INFO;

		throw new RuntimeException(Integer.toString(x));

	}
}