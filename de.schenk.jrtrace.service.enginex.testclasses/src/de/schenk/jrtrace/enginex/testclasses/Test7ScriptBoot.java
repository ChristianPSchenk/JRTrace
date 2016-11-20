/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XClass;

@XClass(classloaderpolicy = XClassLoaderPolicy.BOOT)
public class Test7ScriptBoot {

	public static void callIt() {
		throw new RuntimeException("didit");

	}

}
