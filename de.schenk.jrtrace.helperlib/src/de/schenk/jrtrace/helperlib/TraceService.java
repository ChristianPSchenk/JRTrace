/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

public class TraceService {
	private static TraceSender theInstance;

	static public void setSender(TraceSender sender) {
		theInstance = sender;
	}

	static public TraceSender getInstance() {

		return theInstance;
	}
}
