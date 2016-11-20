/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.reinstall;

public class ReinstallClass4 extends ReinstallBaseClass {

	@Override
	public int call(int a) {
		for (int i = 0; i < 1000; i++) {
			a = (int) ((double) a + 5.0 * (double) a);
		}
		return a + 2;
	}
}
