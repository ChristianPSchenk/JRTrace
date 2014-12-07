/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test.utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class KillThread extends Thread {

	private Thread threadToKill;
	private boolean isKilled = false;
	private String message;
	private int killPort;

	public KillThread(Thread currentThread, int killPort) {
		this.threadToKill = currentThread;
		this.killPort = killPort;
	}

	public String getKillThreadMessage() {
		return message;
	}

	public boolean isKilled() {
		return isKilled;
	}

	@Override
	public void run() {
		try {
			message = "Started listening";
			byte[] receiveData = new byte[1];

			DatagramSocket clientSocket = new DatagramSocket(killPort);

			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			clientSocket.receive(receivePacket);
			isKilled = true;
			threadToKill.interrupt();
			clientSocket.close();
		} catch (UnknownHostException e) {
			message = e.getMessage();
			e.printStackTrace();
			return;
		} catch (SocketException e) {
			message = e.getMessage();
			e.printStackTrace();
			return;
		} catch (IOException e) {
			message = e.getMessage();
			e.printStackTrace();
			return;
		}
		message = "Terminated properly";
	}

}
