package de.schenk.jrtrace.jdk.init.machine.jvm;

public class JavaCallResult {

	
	private String err;
	private String out;

	public JavaCallResult(String result, String result2) {
		this.out=result;
		this.err=result2;
	}

	public String getOutput() {
		return out;
	}
	public String getError() {
		return err;
	}
}
