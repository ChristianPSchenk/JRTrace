/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;

@XClass(classloaderpolicy = XClassLoaderPolicy.BOOT)
public class Test30Boot {

	public String getInstr() {
		return "instrumented";
	}

}
