/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XReturn;
import de.schenk.jrtrace.enginex.testscripts.Test17;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test17", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test17Script {

	@XMethod(names = { "test17" }, location = XLocation.EXIT)
	public void doit(@XReturn long para, int i) {

		if (para == 5) {
			Test17.success = true;
		}

	}

}