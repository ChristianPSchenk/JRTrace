/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test18;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test18", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test18Script {

	@XMethod(location=XLocation.BEFORE_INVOCATION,invokedname="test18sub")
	public void testinstrumentation() {
	  if(Test18.stage==1) Test18.stage=2;
	}
}