/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XField;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test23;

@XClass( classes = "de.schenk.jrtrace.enginex.testscripts.Test23", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test23Script {

	@XMethod(names="will_not_apply",location=XLocation.GETFIELD,fieldclass="de.schenk.jrtrace.enginex.testscripts.Test23",fieldname="field")
	public void testinstrumentation() {	
	  // same name of jrtrace method used twice with different parameters.
	}
	
	@XMethod(names="test23",location=XLocation.GETFIELD,fieldclass="de.schenk.jrtrace.enginex.testscripts.Test23",fieldname="field")
	public void testinstrumentation(@XField(name="field") int fieldValue) {	
	  Test23.hitpoint+=1;
	  if(fieldValue!=5) throw new RuntimeException("wrong fieldvalue");
	}
	
	@XMethod(names="test23",location=XLocation.GETFIELD,fieldclass="nomatch",fieldname="field")
	public void testinstrumentation2(@XField(name="field") int fieldValue) {	
	  throw new RuntimeException("this should never be injected");
	}
	
	@XMethod(names="test23",location=XLocation.PUTFIELD,fieldclass="de.schenk.jrtrace.enginex.testscripts.Test23",fieldname="field")
	public void testinstrumentation3(@XField(name="field") int fieldValue) {	
	  if(fieldValue!=25) throw new RuntimeException("wrong fieldvalue after put");
	  Test23.hitpoint+=10;
	}
	
}