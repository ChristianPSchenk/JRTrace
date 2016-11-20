/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.helperlib.HelperLib;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test38", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET, methodinstance = true)
public class Test38Script extends HelperLib {

	@XMethod(names = "test38", location = XLocation.AFTER_INVOCATION, invokedname = "currentTimeMillis")
	public long onStaticMethodInvoke() {
		return 6;

	}
}