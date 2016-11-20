/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.reinstall;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;

@XClass(classes = "de.schenk.jrtrace.enginex.reinstall.ReinstallBaseClass", derived = true, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class ReinstallScript {

	@XMethod()
	public void instrummentation(@XThis Object o) {
		ReinstallScriptHelper.count();
	}

}
