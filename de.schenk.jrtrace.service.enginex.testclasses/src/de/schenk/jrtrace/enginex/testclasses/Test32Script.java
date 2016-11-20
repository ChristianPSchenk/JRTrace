/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XInvokeThis;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test32;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test32", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test32Script {

	@XMethod(names = "test32", location = XLocation.BEFORE_INVOCATION, invokedname = "test32inner")
	public void beforeInstr(@XInvokeThis Test32 obj) {
		obj.x++;

	}

	@XMethod(names = "test32", location = XLocation.AFTER_INVOCATION, invokedname = "test32inner")
	public void afterInstr(@XInvokeThis Test32 obj) {
		obj.x++;

	}

}