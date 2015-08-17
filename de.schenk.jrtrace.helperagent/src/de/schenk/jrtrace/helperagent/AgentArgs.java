/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

public class AgentArgs {

	private int port;
	private String bootjar;

	/**
	 * the ip address of the server on which the RMI Registry resides. Required
	 * if the computer has multiple network interfaces.
	 */
	private String server;
	/**
	 * Mainly for testing purposes: set the size of the buffer for outgoing
	 * notifications/messages to a predefined value. By default the buffer size
	 * is quite large (10000) but clients should always expect to loose
	 * notifications and implement handleError() on the
	 * NotificationAndErrorListener class.
	 */
	private int notificationBufferSize = 10000;

	public AgentArgs(String args) {
		String[] entries = args.split(",");
		for (String entry : entries) {
			String[] keyvalue = entry.split("=");
			if (keyvalue.length != 2)
				throw new RuntimeException("invalid commandline format " + args);
			switch (keyvalue[0]) {
			case "port":
				this.port = Integer.parseInt(keyvalue[1]);
				break;
			case "bootjar":
				this.bootjar = keyvalue[1];
				break;
			case "server":
				this.server = keyvalue[1];
				break;
			case "notificationBufferSize":
				this.notificationBufferSize = Integer.parseInt(keyvalue[1]);
				break;
			}

		}

	}

	public int getPort() {
		return port;
	}

	public String getBootJar() {
		return bootjar;
	}

	public String getServer() {
		return server;
	}

	public int getNotificationBufferSize() {
		return notificationBufferSize;
	}
}
