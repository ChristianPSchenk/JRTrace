/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.enginex.testscripts.TestCounterClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test5", classloaderpolicy = XClassLoaderPolicy.BOOT)
public class Test5Script {

	@XMethod(names = "test5")
	public void testinstrumentation() {

		// access will fail in test5 because the BOOT classloader has no access
		// to TestCounterClass
		TestCounterClass.counter += 100;

	}
}