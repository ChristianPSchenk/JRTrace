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

@XClass(classes = { "de.schenk.jrtrace.enginex.testclasses.TestClass1" })
public class Script3 {

	@XMethod(location=XLocation.BEFORE_INVOCATION,invokedname="invokedMethod")
	public void method(@XInvokeReturn Object x, @XInvokeParam(n=3) Object i,@XField(name="field") int field,@XInvokeThis Object o) {

	}

	
}
