package de.schenk.jrtrace.jdk.init.test;

import static org.junit.Assert.*;

import java.lang.management.ManagementFactory;
import java.util.List;

import org.junit.Test;

import de.schenk.jrtrace.jdk.init.machine.VMDescriptor;
import de.schenk.jrtrace.jdk.init.machine.VirtualMachineException;
import de.schenk.jrtrace.jdk.init.machine.VirtualMachineWrapper;
import de.schenk.jrtrace.jdk.init.machine.jvm.JDKVMController;
import de.schenk.jrtrace.jdk.init.machine.jvm.JavaCallResult;
import de.schenk.jrtrace.service.JarLocator;
import de.schenk.toolsjar.attach.app.JDKAttachApplication;

public class JVMInvokerTest {

	@Test
	public void testInvokeJDK() throws Exception
	{
		JavaCallResult result=JDKVMController.getInstance().run();
		System.out.println(result.getOutput());
		System.out.println(result.getError());
		assertTrue("Didn't get the help message when invoking the JDKVMController wihtout parameters.",result.getOutput().contains("Usage"));
	}
	
	@Test
	public void testListMachines()
	{
		List<VMDescriptor> list = VirtualMachineWrapper.list();
		boolean iAmInThere=false;
		boolean JDKApplicationIsFilteredOut=true;
		String mypid=ManagementFactory.getRuntimeMXBean().getName();
		for(VMDescriptor d: list)
		{
			System.out.println(d.getPID()+" "+d.getDisplayName());
			if(mypid.contains(d.getPID())) iAmInThere=true;
			if(d.getDisplayName().contains(JDKAttachApplication.class.getName()+" list")) JDKApplicationIsFilteredOut=false;
		}
		assertTrue("The list of VMs returned by testListMachines() doesn't contain the test jvm.",iAmInThere);
		assertTrue("VirtualMachineWrapper.list() shouldn't include the process that is used to create the list.",JDKApplicationIsFilteredOut);
	}

	@Test
	public void testAttachSuccess()
	{
		VMDescriptor myself = getMyself();
		
		try{
		
		VirtualMachineWrapper.loadAgent(myself.getPID(), JarLocator.getJRTraceHelperAgent(), "options");
		} catch(VirtualMachineException e)
		{
			// if there is an agentinitalization exception the agent was at least started. No need to test more.
			assertTrue(e.getMessage().contains("com.sun.tools.attach.AgentInitializationException"));
			return;
		}
		fail("no exception.");
		
		
	}
	@Test
	public void testAttachFail()
	{
		VMDescriptor myself = getMyself();
		
		try{
		VirtualMachineWrapper.loadAgent(myself.getPID(), "wrong", "option");
		}catch(VirtualMachineException e)
		{
			assertTrue(e.getMessage().contains("com.sun.tools.attach.AgentLoadException: Agent JAR not found"));
			return;
		}
		fail("No exception thrown...");
		
		
	}

	private VMDescriptor getMyself() {
		List<VMDescriptor> list = VirtualMachineWrapper.list();
		boolean iAmInThere=false;
		String mypid=ManagementFactory.getRuntimeMXBean().getName();
		VMDescriptor myself=null;
		for(VMDescriptor d: list)
		{
			System.out.println(d.getPID()+" "+d.getDisplayName());
			if(mypid.contains(d.getPID())) myself=d; 
		}
		return myself;
	}
}
