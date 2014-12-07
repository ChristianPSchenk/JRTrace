/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class TraceSender {

	private static final int USE_SYSTEMENV_TRACEPORT = -2;

	private int sendPort = USE_SYSTEMENV_TRACEPORT;

	/**
	 * Creates a tracesender that sends to the specified port
	 * 
	 * @param port
	 *            target port
	 */
	public TraceSender(int port) {
		sendPort = port;
	}

	public final static int TRACECLIENT_STDOUT_ID = 0;
	public final static int TRACECLIENT_STDERR_ID = 2;
	static public final int TRACECLIENT_JRTRACEERROR_ID = 1;
	static public final int TRACECLIENT_AGENT_ID = 4;
	static public final int TRACECLIENT_ENGINEX_STATUS = 8;
	static public final int TRACECLIENT_AGENT_STOPPED_ID = 9;

	public boolean connectionLost() {
		return failedBefore;
	}

	private boolean failedBefore;

	public synchronized void failSafeSend(String msg) {
		failSafeSend(TRACECLIENT_STDOUT_ID, msg);
	}

	public synchronized void failSafeSend(int id, String msg) {
		if (!sendToServer(msg, id)) {
			if (!failedBefore) {
				System.out
						.print("Connection to log client broken. Stopping sending std.out..");
				failedBefore = true;
			}

		} else {
			if (failedBefore)
				System.out.print("Resuming sending std.out to local port");
			failedBefore = false;

		}
	}

	synchronized public boolean sendToServer(String message) {
		return sendToServer(message, TRACECLIENT_STDOUT_ID);
	}

	/**
	 * write the message to the trace listener port
	 * 
	 * @param message
	 * @return true if success, false if fail.
	 */
	synchronized public boolean sendToServer(String message, int id) {
		byte[] bytes = (message).getBytes();
		Socket clientSocket;
		try {
			int port = getPort();
			if (port == USE_SYSTEMENV_TRACEPORT)
				return false;
			clientSocket = new Socket("localhost", port);

			DataOutputStream outToServer = new DataOutputStream(
					clientSocket.getOutputStream());

			outToServer.writeInt(id);
			outToServer.writeInt(bytes.length);
			outToServer.write(bytes);
			outToServer.close();

			clientSocket.close();

		} catch (IOException e) {

			return false;
		}

		return true;

	}

	public int getPort() {

		return sendPort;
	}

}
