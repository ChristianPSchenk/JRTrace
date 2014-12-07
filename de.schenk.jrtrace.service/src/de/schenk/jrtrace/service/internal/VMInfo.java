package de.schenk.jrtrace.service.internal;

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
