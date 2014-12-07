/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class TestListener implements IJRTraceClientListener {

	private Queue<String> q = new ConcurrentLinkedQueue<String>();

	@Override
	synchronized public void messageReceived(String clientSentence) {
		q.add(clientSentence);

	}

	public boolean isEmpty() {
		return q.isEmpty();
	}

	public String getMsg() {

		return q.poll();
	}

	public String getMsg(long timeout) {
		long timer = 0;
		while (q.isEmpty()) {
			timer++;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// don't do nothing
			}
			if (timer >= timeout)
				return null;
		}
		return q.poll();
	}

}
