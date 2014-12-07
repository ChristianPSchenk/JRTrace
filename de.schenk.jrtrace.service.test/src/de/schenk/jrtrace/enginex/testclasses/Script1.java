/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XField;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XParam;
import de.schenk.jrtrace.annotations.XReturn;
import de.schenk.jrtrace.annotations.XThis;

@XClass(classes = { "de.schenk.jrtrace.enginex.testclasses.TestClass1",
		"de.schenk.jrtrace.enginex.testclasses.TestClass3" }, derived = true, classloaderpolicy = XClassLoaderPolicy.NAMED, classloadername = "a.b.c")
public class Script1 {

	@XMethod(names = "doit", arguments = "java.lang.String")
	public void method(@XThis Object x, @XParam(n = 1) int y) {

	}

	@XMethod(names = "doit2", location = XLocation.EXIT)
	public void method(@XReturn Object x) {

	}

	@XMethod(names = "doit3", arguments = {})
	public int method3(@XField(name = "aField") Object o) {
		return 0;
	}

	static public void method2(Object x) {

	}
}
