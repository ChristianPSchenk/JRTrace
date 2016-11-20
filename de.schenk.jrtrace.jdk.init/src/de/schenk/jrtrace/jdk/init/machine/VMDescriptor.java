package de.schenk.jrtrace.jdk.init.machine;

public class VMDescriptor {

	
	private String displayName;
	private String id;
	public VMDescriptor(String name,String id) {
		this.displayName=name;
	}
	public String displayName() {
		return displayName;
	}
	public String id() {
		return id;
	}

}
