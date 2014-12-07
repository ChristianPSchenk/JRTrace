/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test7", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test7Script {

	@XMethod(names = "test7")
	public void testinstrumentation() {

		Test7ScriptBoot.callIt();
	}
}