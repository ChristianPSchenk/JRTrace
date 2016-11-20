/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

public class Test40 {

	private Test40Inner inner = new Test40Inner();

	public String test40() {

		return "nix";

	}

	private class Test40Inner {
		private String privateMethod(int i, String x) {
			return String.format("%d%s", i, x);
		}
	};
}
