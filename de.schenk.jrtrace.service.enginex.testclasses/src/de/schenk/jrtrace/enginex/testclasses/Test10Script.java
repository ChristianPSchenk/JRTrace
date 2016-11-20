/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XReturn;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test10")
public class Test10Script {

	@XMethod(names = "test10", location = XLocation.EXIT)
	public void testinstrumentation(@XReturn int value) {
		if (value == 1234)
			throw new RuntimeException("good return value");

	}
}