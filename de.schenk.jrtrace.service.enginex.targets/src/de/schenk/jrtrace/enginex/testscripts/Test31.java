/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

public class Test31 {

	public int test31() {
		return methodPrivate() + 10 * methodStatic();

	}

	private int methodPrivate() {
		return 0;
	}

	static int methodStatic() {
		return 0;
	}
}
