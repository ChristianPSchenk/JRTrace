/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test21", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test21Script {

	@XMethod(location=XLocation.REPLACE_INVOCATION,invokedname="clear",invokedclass="java.util.HashSet")
	public void testinstrumentation() {	
	  
	}
}