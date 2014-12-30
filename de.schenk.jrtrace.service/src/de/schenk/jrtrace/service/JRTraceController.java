/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;


public interface JRTraceController {

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
	 *         parameter in the description
	 */
	public VMInfo[] getVMs(String containing);

	/**
	 * returns a jrtrace virtual machine representation for the given PID. The
	 * JVM for this API doesn't need the Agent run on the command line. It will
	 * use the VirtualMachine API to try to upload the agent.
	 * 
	 * @param pid
	 *            the pid of the target process
	 * @return the IJRTraceVM to connect
	 */
	public IJRTraceVM getMachine(String pid);

	/**
	 * returns a jrtrace machine representation that will try to connect to an
	 * agent already started with the specified port.
	 * 
	 * @param port
	 * @return the IJRTraceVM to connect
	 */
	public IJRTraceVM getMachine(int port);

}
