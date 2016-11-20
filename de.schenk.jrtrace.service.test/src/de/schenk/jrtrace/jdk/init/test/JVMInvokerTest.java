package de.schenk.jrtrace.jdk.init.test;

import org.junit.Test;

import de.schenk.jrtrace.jdk.init.machine.jvm.JDKVMController;

public class JVMInvokerTest {

	@Test
	public void testInvokeJDK() throws Exception
	{
		JDKVMController.getInstance().start();		
		JDKVMController.getInstance().isAlive();
	}
}
