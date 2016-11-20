package de.schenk.jrtrace.jdk.init.machine.jvm;

public class JDKVMController {

	private static JDKVMController invoker=new JDKVMController();
	
	public static JDKVMController getInstance() {
		return invoker;
	}

}
