/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XParam;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.jrtrace.enginex.testscripts.Test8;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test8", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test8Script {

	@XMethod(names = "test8")
	public void testinstrumentation(@XParam(n = 3) String c,
			@XParam(n = 2) double b, @XParam(n = 1) long a, @XThis Object o) {
		if (!(o instanceof Test8))
			throw new RuntimeException("bad");
		if (!(a == 1))
			throw new RuntimeException("bad long");
		if (!(b == 2.0d))
			throw new RuntimeException("bad double");
		if (!c.equals("abc"))
			throw new RuntimeException("bad string");
	}
}