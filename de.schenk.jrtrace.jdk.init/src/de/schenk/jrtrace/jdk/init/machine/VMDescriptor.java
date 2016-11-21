package de.schenk.jrtrace.jdk.init.machine;

public class VMDescriptor {

	
	private String displayName;
	private String id;
	public VMDescriptor(String name,String id) {
		this.displayName=name;
		this.id=id;
	}
	public String getDisplayName() {
		return displayName;
	}
	public String getPID() {
		return id;
	}

}
