package de.schenk.jrtrace.jdk.init.machine;

import java.util.ArrayList;
import java.util.List;

import de.schenk.jrtrace.jdk.init.machine.jvm.JDKVMController;
import de.schenk.jrtrace.jdk.init.machine.jvm.JavaCallResult;
import de.schenk.toolsjar.attach.app.JDKAttachApplication;



public class VirtualMachineWrapper {

	

	public static void loadAgent(String pid, String agent, String options) throws VirtualMachineException {
		ArrayList<VMDescriptor> result = new ArrayList<VMDescriptor>();
		JavaCallResult processResult = JDKVMController.getInstance().run("install",pid,agent,options);
	
		String error = processResult.getError();
		if(!error.isEmpty())
			
			throw new VirtualMachineException(error);
		
	}

	

	public static List<VMDescriptor> list() {
		
		ArrayList<VMDescriptor> result = new ArrayList<VMDescriptor>();
		JavaCallResult processResult = JDKVMController.getInstance().run("list");
		String[] lines = processResult.getOutput().split("\n");
		for(String line:lines)
		{
			String[] elements=line.split(JDKAttachApplication.DATASEPARATOR);
			result.add(new VMDescriptor(elements[1],elements[0]));
		}
		return result;
	}

}
