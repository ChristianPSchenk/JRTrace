/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.service;

/**
 * 
 * 
 * 
 * @author Christian Schenk
 *
 */
public interface JRTraceMessageListener {

	/**
	 * Method is invoked
	 * 
	 * @param message
	 *            the message object
	 */
	public void handleMessageReceived(Object message);
}
