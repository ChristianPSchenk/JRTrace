/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.commonsuper.test;

public class CSCore extends CSCoreBase implements ICSCore {

	static public boolean success = false;
	public int x = 0;

	public void doit() {

		ICSCore c2 = new CSCore();
		if (x == 1)
			c2 = new CSCore2();
		else if (x == 3)
			c2 = new CSCore4();
		else if (x == 4)
			c2 = new CSCore3();

		CSCore3 c3 = new CSCore3();
		if (x == 0) {
			c3 = new CSCore4();
		}
		c3.method();

		System.out.println(c2.toString());
		success = true;
	}

}
