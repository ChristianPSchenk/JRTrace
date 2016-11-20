/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XParam;
import de.schenk.jrtrace.enginex.testscripts.Test9;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test9", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test9Script {

	@XMethod(names = "test9")
	public void testinstrumentation(@XParam(n = 3) String c,
			@XParam(n = 2) double b, @XParam(n = 1) int a) {
		if (!("abc".equals(c)))
			throw new RuntimeException("bad string");
		if (!(a == 1))
			throw new RuntimeException("bad long");
		if (!(b == 2.0d))
			throw new RuntimeException("bad double");
		Test9.success = true;
	}
}