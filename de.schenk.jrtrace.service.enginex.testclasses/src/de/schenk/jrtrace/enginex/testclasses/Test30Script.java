/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test30", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test30Script {

	public String haveit = "not";

	Test30Boot test30boot = new Test30Boot();

	@XMethod(names = "test30", location = XLocation.EXIT)
	public String instr() {
		if (test30boot.getClass().getClassLoader().getParent() != null)
			throw new RuntimeException(
					"test30boot didn't use the boot classpath");
		return test30boot.getInstr();
	}

}