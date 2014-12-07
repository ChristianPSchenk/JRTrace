/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import java.util.HashSet;
import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.IJRTraceVM;

public class JRTraceControllerImpl implements JRTraceController {
	@Override
	public VMInfo[] getVMs() {
		List<VirtualMachineDescriptor> vmds = VirtualMachine.list();
		VMInfo[] vmInfo = new VMInfo[vmds.size()];
		int i = 0;
		for (VirtualMachineDescriptor vmd : vmds) {
			vmInfo[i++] = new VMInfo(vmd.id(), vmd.displayName());
		}

		return vmInfo;
	}

	@Override
	public VMInfo[] getVMs(String containing) {
		VMInfo[] vms = getVMs();
		HashSet<VMInfo> list = new HashSet<VMInfo>();
		for (int i = 0; i < vms.length; i++) {
			if (vms[i].getName().contains(containing)) {
				list.add(vms[i]);
			}
		}
		// after matching identify text, no process remain:
		if (list.size() == 0)
			return vms;
		return list.toArray(new VMInfo[0]);
	}

	/**
	 * tries to connect to the process with the given identifier.
	 * 
	 * @param pid
	 *            the process identifier
	 * @return the IVirtualMachine or null in case of any error.
	 */
	@Override
	public IJRTraceVM getMachine(String pid) {

		JRTraceVMImpl theMachine = new JRTraceVMImpl(pid);

		return theMachine;

	}
}
