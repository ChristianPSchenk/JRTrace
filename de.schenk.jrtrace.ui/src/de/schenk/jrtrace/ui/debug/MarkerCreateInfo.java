/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.debug;

public class MarkerCreateInfo {
	private String classname;
	private String method;
	private String message;
	private String descriptor;

	public MarkerCreateInfo(String classname, String method, String message,
			String descritpor) {
		this.classname = classname;
		this.method = method;
		this.descriptor = descritpor;
		this.message = message;

	}

	public String getClassName() {
		return classname;
	}

	public String getMethod() {
		return method;
	}

	public String getMessage() {
		return message;
	}

	public String getMethodDescriptor() {
		return descriptor;
	}
}
