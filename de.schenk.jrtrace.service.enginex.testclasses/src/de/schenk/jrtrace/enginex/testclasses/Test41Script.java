/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.TestCounterClass;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test41", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test41Script {

	@XMethod(names = "test41", arguments = { "java.lang.String",
			"java.lang.Object" })
	public void testinstrumentation() {

		TestCounterClass.counter++;
	}
}
