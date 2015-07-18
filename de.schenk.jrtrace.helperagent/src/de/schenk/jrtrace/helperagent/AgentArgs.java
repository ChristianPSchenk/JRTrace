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

}
