package de.schenk.jrtrace.enginex.reinstall;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.reinstall.SimpleReinstallClassUnderTest")
public class SimpleReinstallScript {

	@XMethod(names = "method", location = XLocation.EXIT)
	public int method() {
		return 2;
	}

}
