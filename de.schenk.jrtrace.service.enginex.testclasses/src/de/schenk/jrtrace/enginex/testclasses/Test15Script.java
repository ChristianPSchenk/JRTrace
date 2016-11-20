/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test15;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test15", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test15Script {

	@XMethod(names = { "test15" })
	public void doit() {
		Test15.success = false;
		Test15.success = Test15UnannotatedClass.doit();
	}

}