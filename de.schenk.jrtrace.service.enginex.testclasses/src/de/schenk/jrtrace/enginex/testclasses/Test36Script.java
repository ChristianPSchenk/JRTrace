/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XMethodName;
import de.schenk.jrtrace.annotations.XReturn;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test36", regex = true, classloaderpolicy = XClassLoaderPolicy.TARGET, methodinstance = true)
public class Test36Script {

	int i = 10;

	@XMethod(names = "test36a", location = XLocation.ENTRY)
	public void fkt36a_in(@XMethodName String s) {

		i = i + 1;

	}

	@XMethod(names = "test36a", location = XLocation.EXIT)
	public int fkt36a(long xyz) {

		i = i + 1;
		return i;

	}

	@XMethod(names = "test36b", location = XLocation.EXIT)
	public int fkt36b(@XReturn int p) {
		i = i + p;
		return i;
	}

}