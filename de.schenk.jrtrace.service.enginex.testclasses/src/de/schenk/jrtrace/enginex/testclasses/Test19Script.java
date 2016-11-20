/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XInvokeParam;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test19;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test19", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test19Script {

	@XMethod(location=XLocation.REPLACE_INVOCATION,invokedname="test19sub")
	public int testinstrumentation(@XInvokeParam(n=1) String x) {
	  if(!"hallo".equals(x)) throw new RuntimeException("wrong string");
	  if(Test19.stage==1) Test19.stage=2;
	  return 1000;
	}
}