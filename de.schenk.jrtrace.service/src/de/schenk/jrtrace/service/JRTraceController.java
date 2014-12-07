/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import de.schenk.jrtrace.service.internal.VMInfo;

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
	 * returns a jrtrace virtual machine representation for the given PID
	 * 
	 * @param pid
	 *            the pid of the target process
	 * @return the IJRTraceVM to connect
	 */
	public IJRTraceVM getMachine(String pid);

}
