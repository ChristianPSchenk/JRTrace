/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.InstrumentedClass2;

@XClass(classes = { "de.schenk.jrtrace.enginex.testscripts.InstrumentedClass2" }, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class EngineXTestClass2 {

	@XMethod(names = "doit")
	public void dosomething() {
		InstrumentedClass2.x = true;

	}

	static public void astaticmethod() {
		InstrumentedClass2.x = true;
	}
}
