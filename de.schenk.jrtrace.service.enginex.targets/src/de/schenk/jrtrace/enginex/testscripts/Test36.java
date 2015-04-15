/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

public class Test36 {

	public int test36() {

		return test36a() + test36b();

	}

	private int test36b() {
		long v = 5;
		int k = 3;
		return 10 + (int) v + k;
	}

	private int test36a() {

		return 0;
	}

}
