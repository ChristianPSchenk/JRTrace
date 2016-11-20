/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.service;

public class VMInfo {

	private String name;
	private String pid;

	public VMInfo(String pid, String name) {
		this.pid = pid;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getId() {
		return pid;

	}

}
