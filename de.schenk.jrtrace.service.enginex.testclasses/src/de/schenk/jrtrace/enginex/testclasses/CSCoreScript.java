/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.commonsuper.test.CSCore;

@XClass(classes = "de.schenk.jrtrace.commonsuper.test.CSCore", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class CSCoreScript {

	@XMethod(names = "doit")
	public void testinstrumentation() {
		CSCore.success = true;
	}
}