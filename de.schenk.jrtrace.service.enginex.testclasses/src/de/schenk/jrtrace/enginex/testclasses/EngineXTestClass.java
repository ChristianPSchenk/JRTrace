/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.InstrumentedClass;

@XClass(classes = { "de.schenk.jrtrace.enginex.testscripts.InstrumentedClass" }, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class EngineXTestClass {

	@XMethod(names = "doit")
	public void dosomething() {
		InstrumentedClass.x = true;

	}

	static public void astaticmethod() {
		InstrumentedClass.x = true;
	}
}
