package de.schenk.toolsjar.attach.app;

import java.io.IOException;
import java.util.List;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * The main class of a simple java application that relies on the attach API
 * to allow to upload a java agent into a java process and to list the available JVMs.
 * 
 * @author Christian P. Schenk
 *
 */
public class JDKAttachApplication {

	public static final String DATASEPARATOR = "#!#!#!";

	public static void main(String[] args) {
		 if(args.length==0)
		 {
			 
			 showUsage();
			 return;
		 }
		 if(args.length==1&&"list".equals(args[0]))
		 {
			 list();
			 return;
		 }
		 
		 if(args.length==4&&"install".equals(args[0]))
		 {
			 install(args[1],args[2],args[3]);
			 return;
		 }
		 System.err.println("Wrong Parameters:");
		 for(int i=0;i<args.length;i++)
		 {
			 System.err.println(args[i]);
		 }
		 showUsage();
		 return;

	}

	private static void install(String pid, String agentJarPath, String agentOptions) {
		
		
		List<VirtualMachineDescriptor> descs = VirtualMachine.list();
		for(VirtualMachineDescriptor desc:descs)
		{
			if(desc.id().equals(pid))
			{
				install(desc,agentJarPath,agentOptions);
				
			}
		}
		
	}

	private static void install(VirtualMachineDescriptor descriptor ,String agentJarPath, String options) {
		try {
			VirtualMachine vm = VirtualMachine.attach(descriptor);
			vm.loadAgent(agentJarPath,options);
			vm.detach();
		} catch (AttachNotSupportedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AgentLoadException e) {
			e.printStackTrace();
		} catch (AgentInitializationException e) {
			e.printStackTrace();
		}
		
		
	}

	private static void list() {
		List<VirtualMachineDescriptor> machines = VirtualMachine.list();
		for(VirtualMachineDescriptor m: machines)
		{
			System.out.println(String.format("%s%s%s",m.id(),DATASEPARATOR,m.displayName()));
		}
	}

	private static void showUsage() {
			System.out.println("Usage:");
			System.out.println("No Parameters: show this info.");
			System.out.println("<tool> list   : lists pid and description of all running JVMs");
			System.out.println("<tool> install <pid> <agent.jar> <options>   : installs the specified agent into the JVM with the specified pid.");
		
	}

}
