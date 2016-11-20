/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

public class Test18 {

	public static int stage = 0;

	public int test18() {
		long localvar = 5;
		stage = 1;
		if (stage != 1)
			return -1;
		long newlocalvar = test18sub(localvar);

		int b = 0;
		return stage + (int) (newlocalvar * b);
	}

	/**
   * 
   */
	private long test18sub(long localvar) {
		if (stage == 2)
			stage = 3;
		return localvar + 1;
	}

}
