/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test1;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test1", derived = true, classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test2Script {

	@XMethod(names = "test1")
	public void test1() {

		Test1.counter++;
	}
}
