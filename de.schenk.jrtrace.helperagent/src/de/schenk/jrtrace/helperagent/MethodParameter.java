/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helperagent;

import javax.management.openmbean.CompositeData;

public class MethodParameter {
	private String string;

	public MethodParameter(Object arg0) {
		string = (String) arg0;
	}

	public String getString() {
		return string;
	}

	public static MethodParameter from(CompositeData data) {
		return new MethodParameter((String) data.get("string"));

	}
}
