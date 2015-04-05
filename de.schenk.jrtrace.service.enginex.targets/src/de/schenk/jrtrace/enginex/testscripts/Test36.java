/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

public class Test36 {

	public int test36() {
		long v = 5;
		int k = 3;
		return test36a() + test36b() + (int) v + k;

	}

	private int test36b() {

		return 10;
	}

	private int test36a() {

		return 0;
	}

}
