/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.TestCounterClass;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test2", derived = true, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test1Script {

	@XMethod(names = "doit")
	public void test1() {

		TestCounterClass.counter++;
	}
}
