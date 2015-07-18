/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.reinstall;

public class ReinstallBaseClass {

	public void method() {

	}

	public int call(int a) {
		int b = a;
		for (int i = 0; i < 100; i++)
			b += a;
		return b;
	}
}
