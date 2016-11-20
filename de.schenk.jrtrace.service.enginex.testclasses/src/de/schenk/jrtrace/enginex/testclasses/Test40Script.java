/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.jrtrace.helperlib.HelperLib;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test40")
public class Test40Script extends HelperLib {

	@XMethod(names = "test40", location = XLocation.EXIT)
	public String onexit(@XThis Object object) {
		Object inner = getField(object, "inner");
		return (String) invokeMethod(inner, "privateMethod", 1, "String");

	}

}