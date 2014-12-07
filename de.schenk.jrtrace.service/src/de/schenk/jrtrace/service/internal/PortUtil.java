/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service.internal;

import java.io.IOException;
import java.net.ServerSocket;

public class PortUtil {

	/**
	 * tries to get a free port and returns it
	 * @return the free port , -1 if no port
	 */
	public static int getFreePort() {
		ServerSocket serverSocket=null;
		try {
			 serverSocket = new ServerSocket(0);
			int freePort = serverSocket.getLocalPort();
			
			return freePort;
		} catch (IOException e) {
			return -1;
		} finally
		{
			try {
				serverSocket.close();
			} catch (IOException e) {
				//do nothing
			}
		}
		
	}
}
