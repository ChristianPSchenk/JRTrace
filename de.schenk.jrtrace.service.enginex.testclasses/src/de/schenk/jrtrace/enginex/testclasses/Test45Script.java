/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import java.io.File;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XParam;
import de.schenk.jrtrace.enginex.testscripts.TestCounterClass;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test45", derived = true, classloaderpolicy = XClassLoaderPolicy.BOOT)
public class Test45Script {

	@XMethod(names = "test45")
	public void test1(@XParam(n=1) StringBuffer x) {

		File m=new File("can be used without exception!");
		x.append("sehr");
	}
}
