/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XReturn;
import de.schenk.jrtrace.enginex.testscripts.Test11;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test11", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test11Script {

	@XMethod(names = "test11", location = XLocation.EXIT)
	public int testinstrumentation(@XReturn int value) {
		if (value != 1234)
			throw new RuntimeException("bad return value");
		return 1235;

	}

	@XMethod(names = "test11b", location = XLocation.EXIT)
	public Test11 testinstrumentation2(@XReturn Object value) {
		if (!(value instanceof Test11))
			throw new RuntimeException("bad return value");
		return null;

	}
}