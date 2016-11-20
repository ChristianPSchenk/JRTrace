/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XException;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.jrtrace.enginex.testscripts.Test34;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test34", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test34Script {

	@XMethod(names = "test34", location = XLocation.EXCEPTION)
	public Throwable instr(@XException Throwable e, @XThis Test34 obj) {
		obj.exceptionNoted = true;
		return new RuntimeException("test2");

	}

}