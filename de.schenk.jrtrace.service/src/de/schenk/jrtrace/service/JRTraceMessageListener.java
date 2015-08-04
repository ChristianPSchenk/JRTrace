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

	/**
	 * invoked, if a transmission error (typically message loss) occurs.
	 */
	public void handleError();
}
