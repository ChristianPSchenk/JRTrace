/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de\\.schenk.*Test12", regex = true)
public class Test12Script {

	@XMethod(names = { "t[est][est][est]12" }, location = XLocation.EXIT)
	public boolean testinstrumentation() {

		return true;

	}

}