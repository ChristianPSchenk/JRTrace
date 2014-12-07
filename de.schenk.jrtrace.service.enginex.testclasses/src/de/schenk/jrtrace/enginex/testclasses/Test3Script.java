/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.TestCounterClass;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test3", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test3Script {

	@XMethod(names = "test3", arguments = "java.lang.String")
	public void testinstrumentation() {

		TestCounterClass.counter++;
	}
}
