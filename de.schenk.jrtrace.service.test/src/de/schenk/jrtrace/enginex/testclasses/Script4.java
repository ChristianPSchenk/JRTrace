package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(exclude = ".*TestClass1.*", classes = "de.schenk.jrtrace.enginex.testclasses.TestClass1")
public class Script4 {

	@XMethod()
	public void method() {

	}

}
