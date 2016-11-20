/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

public class Test37 {

	int theField = 5;
	int theOtherField = 3;

	public int test37() {
		long k = 3;

		theField = 6;

		return theField + theOtherField + (int) k;
	}

}
