/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XReturn;

@XClass(classes = "some\\.thing", regex = true)
public class Script2 {

	@XMethod(names = "doit2", location = XLocation.EXIT)
	public void method(@XReturn Object x) {

	}

}
