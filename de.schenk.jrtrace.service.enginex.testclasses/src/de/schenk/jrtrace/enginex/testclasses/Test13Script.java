/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.jrtrace.enginex.testscripts.Test13;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test13", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test13Script {

	@XMethod(names = { "<init>" }, arguments = {})
	public void testinstrumentation(@XThis Object m) {
		if (m == null)
			throw new RuntimeException("this was null");

		Test13.a = 1;

	}

	@XMethod(names = { "<init>" }, arguments = { "int" })
	public void testinstrumentation2() {

		Test13.a = 2;

	}

}