/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XField;
import de.schenk.jrtrace.annotations.XInvokeParam;
import de.schenk.jrtrace.annotations.XInvokeReturn;
import de.schenk.jrtrace.annotations.XInvokeThis;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(exclude={"abc.*","def.*"},classes = { "de.schenk.jrtrace.enginex.testclasses.TestClass1" })
public class Script3 {

	@XMethod(location=XLocation.BEFORE_INVOCATION,invokedname="invokedMethod",invokedclass="a.b.C")
	public void method(@XInvokeReturn Object x, @XInvokeParam(n=3) Object i,@XField(name="field") int field,@XInvokeThis Object o) {

	}
	
	@XMethod(location=XLocation.GETFIELD,fieldclass="a.b.C",fieldname="field")
	public void method2()
	{
	  
	}

	
}
