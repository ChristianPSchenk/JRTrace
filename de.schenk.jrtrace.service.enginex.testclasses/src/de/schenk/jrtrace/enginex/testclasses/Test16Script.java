/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test16;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test16", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test16Script {

	@XMethod(names = { "subtest16" })
	public void doit() {
		Test16.success = true;

	}

}