package de.schenk.jrtrace.enginex.testclasses;

public class Test26ScriptRunnable implements Runnable {
	 private Test26Script s;
	public Test26ScriptRunnable(Test26Script s) {
		this.s=s;
	}
	@Override
	public void run() {
		this.s.haveit=1;

	}

}
