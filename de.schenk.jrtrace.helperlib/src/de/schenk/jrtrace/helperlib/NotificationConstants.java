/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

public interface NotificationConstants {

	/**
	 * System.err is sent here.
	 */
	public static final String NOTIFY_STDERR = "stderr";
	/**
	 * System.out is sent here
	 */
	public static final String NOTIFY_STDOUT = "stdout";
	/**
	 * progress status is sent with this constant
	 */
	public static final String NOTIFY_PROGRESS = "progress";
	/**
	 * a problem in the jrtrace scripts
	 */
	public static final String NOTIFY_PROBLEM = "problem";
	/**
	 * jrtrace error messages
	 */
	public static final String NOTIFY_ERROR = "error";
	/**
	 * custom messages from the HelperLib.sendMessage(...) are sent with this
	 * constant
	 */
	public static final String NOTIFY_MESSAGE = "message";
}
