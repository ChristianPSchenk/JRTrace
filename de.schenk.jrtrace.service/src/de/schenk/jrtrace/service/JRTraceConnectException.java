/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service;

/**
 * Runtime Exception that represents a failed connection to the target machine
 * 
 *
 */
public class JRTraceConnectException extends RuntimeException {

	public JRTraceConnectException(Exception e) {
		super(e);
	}
}
