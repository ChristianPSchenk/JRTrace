/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceController;
import de.schenk.jrtrace.service.VMInfo;

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
	 * @param serveraddress
	 *            the network address on which the RMI server expects requests.
	 *            Important if the computer has more than one network address
	 * @return the IVirtualMachine or null in case of any error.
	 */
	@Override
	public IJRTraceVM getMachine(String pid, String serveraddress) {

		IJRTraceVM theMachine = new JRTraceVMImpl(pid, serveraddress);

		return theMachine;

	}

	@Override
	public IJRTraceVM getMachine(int port, String targetmachine) {
		return new JRTraceConnectingImpl(port, targetmachine);
	}

	@Override
	public boolean hsperfdataAccessible() {
		String username;
		String tmpDir = System.getenv("TMP");
		String userName = System.getenv("USERNAME");
		File hsperfdataFolder = new File(tmpDir + "\\hsperfdata_" + userName);
		if (hsperfdataFolder.exists()) {
			File testFile = new File(hsperfdataFolder + "\\"
					+ String.format("%d", System.currentTimeMillis()));
			try {
				testFile.createNewFile();
				testFile.delete();
			} catch (IOException e) {
				return false;
			}
		} else {
			try {
				hsperfdataFolder.createNewFile();
				hsperfdataFolder.delete();
			} catch (IOException e) {
				return false;
			}

		}
		return true;
	}
}
