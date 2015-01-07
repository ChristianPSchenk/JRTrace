/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(exclude="de\\.schenk.*", classes = "de.schenk.jrtrace.enginex.testscripts.Test22", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test22Script {

	@XMethod(location=XLocation.EXIT,names="xyz")
	public int testinstrumentation() {	
	  return 0;
	}
}