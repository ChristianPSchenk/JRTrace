/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;

public class TraceReceiver {

	public TraceReceiver(int port) {
		this.port = port;
	}

	public TraceReceiver() {
		this.port = 0;
	}

	public void stop() throws IOException {
		synchronized (listener) {
			listener.clear();
		}

		server.close();
	}

	int port = 0;

	public class TraceReceiverThread extends Thread {
		@Override
		public void run() {
			while (true) {
				try {

					Socket connectionSocket = server.accept();
					DataInputStream inFromClient = new DataInputStream(
							connectionSocket.getInputStream());

					int id = inFromClient.readInt();
					int size = inFromClient.readInt();

					byte[] buffer = new byte[size];

					int res = 0;
					while (res != size) {
						int erg = inFromClient.read(buffer, res, size - res);
						if (erg == -1)
							break;
						res += erg;
					}
					if (res != size) {

						System.err
								.println(String
										.format("Invalid message received from TraceClient, length wrong %d / %d",
												res, size));

					}
					HashSet<IJRTraceClientListener> listenerCopy = null;
					synchronized (listener) {
						listenerCopy = new HashSet<IJRTraceClientListener>();
						if (listener.get(id) != null)
							listenerCopy.addAll(listener.get(id));
					}

					for (IJRTraceClientListener l : listenerCopy) {
						try {
							l.messageReceived(new String(buffer, 0, res));
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}

					connectionSocket.close();
				} catch (SocketException e) {
					// socket closed, stop listening
					break;
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}
	}

	private ServerSocket server;
	private TraceReceiverThread receiverThread;

	/**
	 * starts a trace receiver which will listen on a free port for incoming
	 * traces from the monitored jrtrace application
	 * 
	 * @return the port of the receiver, -1 in case of an error
	 */
	public void start() throws IOException {
		server = new ServerSocket(port);
		receiverThread = new TraceReceiverThread();
		receiverThread.setDaemon(daemon);
		receiverThread.setName(String.format(
				"JRTrace Trace Receiver on port %d", server.getLocalPort()));
		receiverThread.start();

	}

	/**
	 * 
	 * @return the port on which the trace listener is listening
	 */
	public int getServerPort() {
		return server.getLocalPort();
	}

	HashMap<Integer, HashSet<IJRTraceClientListener>> listener = new HashMap<Integer, HashSet<IJRTraceClientListener>>();
	private boolean daemon;

	public void removeListener(IJRTraceClientListener iJRTraceClientListener) {
		removeListener(TraceSender.TRACECLIENT_STDOUT_ID,
				iJRTraceClientListener);
	}

	public void addListener(IJRTraceClientListener iJRTraceClientListener) {
		addListener(TraceSender.TRACECLIENT_STDOUT_ID, iJRTraceClientListener);
	}

	/**
	 * add a listener that will receive all the traces created by the jrtrace
	 * agent via the HelperLib
	 * 
	 * @param iJRTraceClientListener
	 */
	public void addListener(int id,
			IJRTraceClientListener iJRTraceClientListener) {
		synchronized (listener) {
			HashSet<IJRTraceClientListener> lst = listener.get(id);
			if (lst == null) {
				lst = new HashSet<IJRTraceClientListener>();
				listener.put(id, lst);
			}
			lst.add(iJRTraceClientListener);
		}

	}

	public void removeListener(int id, IJRTraceClientListener theListener) {
		synchronized (listener) {
			HashSet<IJRTraceClientListener> lsts = listener.get(id);
			if (lsts != null)
				lsts.remove(theListener);
		}

	}

	/**
	 * if called before starting the receiver the receiving thread will be a
	 * daemon thread
	 */
	public void setDaemon() {
		this.daemon = true;

	}
}
