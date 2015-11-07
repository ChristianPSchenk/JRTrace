/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XReturn;
import de.schenk.jrtrace.helperlib.HelperLib;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test43", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET, methodinstance = true)
public class Test43Script extends HelperLib {

	@XMethod(names = "test43", location = XLocation.AFTER_INVOCATION, invokedname = "currentTimeMillis")
	public void test43method(@XReturn String abc) {

	}
}