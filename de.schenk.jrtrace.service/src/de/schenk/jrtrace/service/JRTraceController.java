/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

public interface JRTraceController {

	/**
	 * 
	 * @return false, if the tool wasn't started using a JDK or the proper
	 *         tools.jar/attach.dll. In this case getVMs() and
	 *         getMAchine(pid,...) will not work.
	 */
	public boolean supportsAttachToPID();

	/**
	 * 
	 * Checks accessibility of the folder: %tmp%/hsperfdata_%username%. If this
	 * folder is not accessible the VirtualMachine list and attach API will not
	 * work on windows.
	 * 
	 * @return true, if not Windows -OR- if the folder doesn't exist but can be
	 *         created -OR- if the folder exists and files can be created
	 *         inside.
	 */
	public boolean hsperfdataAccessible();

	/**
	 * 
	 * @return a list of all JVMs on this system active
	 */
	public VMInfo[] getVMs();

	/**
	 * 
	 * @param containing
	 *            a string that needs to be contained in the description
	 * @return a list of all JVMs running on this system that contain the
	 *         parameter in the description, empty list if no match.
	 */
	public VMInfo[] getVMs(String containing);

	/**
	 * returns a jrtrace virtual machine representation for the given PID. The
	 * JVM for this API doesn't need the Agent run on the command line. It will
	 * use the VirtualMachine API to try to upload the agent.
	 * 
	 * @param pid
	 *            the pid of the target process
	 * @param servernetworkaddress
	 *            if the computer has multiple network address, the address on
	 *            which the connection will be expected. null if not or for
	 *            local connections.
	 * @return the IJRTraceVM to connect
	 */
	public IJRTraceVM getMachine(String pid, String servernetworkaddress);

	/**
	 * returns a jrtrace machine representation that will try to connect to an
	 * agent already started with the specified port on the specified machine
	 * 
	 * @param port
	 *            the port on which the RMI registry is listening
	 * @param targetmachine
	 *            the machine on which the server is running or null for local
	 *            host
	 * @return the IJRTraceVM to connect
	 */
	public IJRTraceVM getMachine(int port, String targetmachine);

}
