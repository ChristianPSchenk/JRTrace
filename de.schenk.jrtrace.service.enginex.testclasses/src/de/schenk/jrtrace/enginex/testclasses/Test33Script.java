/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XMethodName;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test33", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test33Script {

	@XMethod(names = "test33", location = XLocation.EXIT)
	public String instr(@XMethodName String obj) {
		return obj;

	}

}