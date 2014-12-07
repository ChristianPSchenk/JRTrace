/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schenk.jrtrace.helperlib.TraceReceiver;
import de.schenk.jrtrace.helperlib.TraceSender;

public class TraceReceiverTest {

	private TraceReceiver traceReceiver;
	private int port;
	private TraceSender traceClient;

	@Before
	public void setup() throws Exception {
		traceReceiver = new TraceReceiver();
		traceReceiver.start();
		port = traceReceiver.getServerPort();
		traceClient = new TraceSender(port);

	}

	@After
	public void after() throws Exception {
		traceReceiver.stop();

	}

	@Test
	public void testSimpleSendReceive() throws Exception {
		TestListener listenerOn5 = new TestListener();
		traceReceiver.addListener(5, listenerOn5);
		traceClient.sendToServer("abc", 5);

		assertEquals(listenerOn5.getMsg(50), "abc");
	}

	class TestSenderThread extends Thread {
		private String text;
		private CyclicBarrier latch;

		public TestSenderThread(String text, CyclicBarrier latch) {
			this.text = text;
			this.latch = latch;
		}

		@Override
		public void run() {
			try {
				latch.await();
				for (int i = 0; i < 100; i++) {
					traceClient.sendToServer(text, 5);
					Thread.yield();
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} catch (BrokenBarrierException e) {
				throw new RuntimeException(e);
			}

		}
	}

	/**
	 * 5 sender threads concurrently sending long messages
	 * 
	 * @throws Exception
	 */
	@Test
	public void testConcurrentSendReceive() throws Exception {

		TestListener listenerOn5 = new TestListener();
		traceReceiver.addListener(5, listenerOn5);
		int threads = 10;
		CyclicBarrier starter = new CyclicBarrier(threads + 1);
		HashSet<String> validMessages = new HashSet<String>();
		for (int i = 0; i < threads; i++) {
			StringBuffer msg = new StringBuffer();
			for (int j = 0; j < 100; j++) {
				msg.append(String
						.format("message%dajsfsajkfhskfhkjfhkjsdfs", i));
				if (j % 10 == 0)
					msg.append("\n");

			}
			validMessages.add(msg.toString());
			Thread sender = new TestSenderThread(msg.toString(), starter);
			sender.start();

		}

		starter.await();

		for (int i = 0; i < threads * 100; i++) {
			String msg = listenerOn5.getMsg(500);
			if (!validMessages.contains(msg)) {

				fail(String.format("Received invalid msg %d:" + msg
						+ " valid: " + validMessages.toString(), i));
			}
		}
		assertTrue(listenerOn5.isEmpty());

	}

}
