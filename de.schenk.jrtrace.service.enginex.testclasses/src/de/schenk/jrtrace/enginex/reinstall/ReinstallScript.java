package de.schenk.jrtrace.enginex.reinstall;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.reinstall.ReinstallBaseClass", derived = true)
public class ReinstallScript {

	public long counter = 0;

	@XMethod()
	public void instrummentation() {
		counter++;
	}

}
