/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XInvokeReturn;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.helperlib.HelperLib;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test42", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET, methodinstance = true)
public class Test42Script extends HelperLib {

	@XMethod(names = "test42", location = XLocation.EXIT, invokedname = "currentTimeMillis")
	public void test42method(@XInvokeReturn String abc) {

	}
}