/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.jrtrace.enginex.testscripts.Test37;
import de.schenk.jrtrace.helperlib.HelperLib;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test37", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET, methodinstance = true)
public class Test37Script extends HelperLib {

	int i = 10;

	@XMethod(names = "test37", location = XLocation.PUTFIELD, fieldname = "theField")
	public void putfield(@XThis Test37 s) {

		setField(s, "theField", 10);

	}
}