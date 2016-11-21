/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import java.io.IOException;



import de.schenk.jrtrace.jdk.init.machine.VirtualMachineException;
import de.schenk.jrtrace.jdk.init.machine.VirtualMachineWrapper;
import de.schenk.jrtrace.service.ICancelable;
import de.schenk.jrtrace.service.JarLocator;

public class JRTraceVMImpl extends AbstractVM {


	private String thePID;
	private String servernetworkaddress;

	/**
	 * 
	 * @param pid
	 *            the pid to upload the agent to.
	 * @param servernetwork
	 *            the ip address name on which the server will expect a
	 *            connection or null for local connections
	 */
	public JRTraceVMImpl(String pid, String servernetwork) {
		thePID = pid;
		this.servernetworkaddress = servernetwork;
	}

	/**
	 * 
	 * @return true if successful, excception can be retrieved via
	 *         getException() if not successful
	 */
	public boolean attach(ICancelable stopper) {

		int port = installAgent(thePID);
		if (port == -1)
			return false;

		


		return connectToAgent(port, null, stopper);
	}

	

	private int installAgent(String thePID) {

		port = PortUtil.getFreePort();
		try {
			String agent = JarLocator.getJRTraceHelperAgent();
			String helperPath = JarLocator.getHelperLibJar();
			String mynetwork = servernetworkaddress == null ? ""
					: (",server=" + servernetworkaddress);
			
			VirtualMachineWrapper.loadAgent(thePID,agent, String.format("port=%d,bootjar=%s%s", port,
					helperPath, mynetwork));

		} catch (VirtualMachineException e) {
			lastException = e;
			return -1;
		}
		return port;
	}

	public boolean detach() {
	

		return  stopConnection(false);
		

	}



	@Override
	public String getConnectionIdentifier() {
		return thePID;
	}

	@Override
	public String toString() {
		return String
				.format("Agent installed into process: %s  (JRTrace Server on port: %d)",
						thePID, port);
	}

}
