/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testscripts;

public class InstrumentedClass2 {

	{
		LoadCheck.instrumentedClassLoader2 = true;
	}
	public static boolean x = false;

	public boolean getResult() {
		return x;
	}

	public void doit() {

		System.out.println("doit");
	}
}
