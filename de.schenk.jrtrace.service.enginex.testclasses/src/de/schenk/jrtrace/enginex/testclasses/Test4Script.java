/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.jrtrace.enginex.testscripts.Test4;
import de.schenk.jrtrace.enginex.testscripts.TestCounterClass;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test4", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test4Script {

	@XMethod(names = "test4")
	public void testinstrumentation(boolean p1, int p2, double p3, float p4,
			Object p5, long p6, byte p7, short p8, byte[] p9, @XThis Object var) {
		Test4 t4 = (Test4) var;

		TestCounterClass.counter += t4.i;
	}
}
