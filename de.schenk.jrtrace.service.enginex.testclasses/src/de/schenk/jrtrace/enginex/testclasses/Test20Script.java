/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XInvokeReturn;
import de.schenk.jrtrace.annotations.XInvokeThis;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test20;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test20", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test20Script {

	@XMethod(location=XLocation.AFTER_INVOCATION,invokedname="test20sub")
	public Integer testinstrumentation(@XInvokeReturn Integer x,@XInvokeThis Test20 xinvokeThis) {	
	  if(!xinvokeThis.getId().equals("invokedObject"))
	    {
	    
	    throw new RuntimeException("wrong instance");
	    }
	  return x+5;
	}
}