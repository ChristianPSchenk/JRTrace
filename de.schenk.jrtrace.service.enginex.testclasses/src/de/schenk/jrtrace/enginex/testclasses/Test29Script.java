/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test29", derived = true, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test29Script {

	public String haveit = "not";
	Runnable runnable =

	new Runnable() {

		@Override
		public void run() {
			haveit = "done";

		}
	};

	@XMethod(names = "test29", location = XLocation.EXIT)
	public String instr() {
		runnable.run();
		return "instrumented";
	}

}