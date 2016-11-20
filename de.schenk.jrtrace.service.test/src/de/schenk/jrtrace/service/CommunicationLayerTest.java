package de.schenk.jrtrace.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.schenk.jrtrace.helperagent.internal.CommunicationController;
import de.schenk.jrtrace.helperagent.internal.ICommunicationController;
import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.internal.JRTraceBeanNotificationListener;

public class CommunicationLayerTest {

	public static final int LAST_MSG = -12345;
	private CommunicationController controller;
	private TestSender dummySender;
	private JRTraceBeanNotificationListener notificationListener;

	private boolean error = false;

	private NotificationAndErrorListener errorReceiver = new NotificationAndErrorListener() {

		public void handleError() {
			error = true;

		};

		@Override
		public void sendMessage(Notification notification) {
			// do nothing

		}
	};

	@Before
	public void setup() {
		error = false;
		notificationListener = new JRTraceBeanNotificationListener();
		notificationListener.addClientListener(
				NotificationConstants.NOTIFY_MESSAGE, errorReceiver);
		dummySender = new TestSender(notificationListener);
		controller = new CommunicationController(dummySender);
		notificationListener.setCommunicationControl(controller);
	}

	@After
	public void tearDown() {
		dummySender.stop();
	}

	@Test
	public void testUnlimitedCommunication() {

		controller.setAcknowledgementMode(0);

		long startTime = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			controller.sendMessage(getMessageWithTimeStamp(i));
		}
		controller.sendMessage(getMessageWithTimeStamp(LAST_MSG));

		assertTrue(dummySender.waitForEnd());

		assertTrue(dummySender.getLastMessageReceivedTime() < startTime + 2000 * 1000 * 1000);
	}

	@Test
	public void testAcknowledgeEvery5thMsg() {

		controller.setAcknowledgementMode(5);

		long startTime = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			controller.sendMessage(getMessageWithTimeStamp(i));
		}
		controller.sendMessage(getMessageWithTimeStamp(LAST_MSG));

		assertTrue(dummySender.waitForEnd());

		System.out
				.println(String.format(
						"Took: %d",
						(startTime - dummySender.getLastMessageReceivedTime()) / 1000 / 1000));
		assertTrue(dummySender.getLastMessageReceivedTime() < startTime + 2000 * 1000 * 1000);
		assertTrue(!error);
	}

	class NotificationSenderThread extends Thread {
		private ICommunicationController controller;
		private CountDownLatch latch;
		private CountDownLatch endlatch;

		public NotificationSenderThread(ICommunicationController controller,
				CountDownLatch startLatch, CountDownLatch endlatch) {
			this.controller = controller;
			this.latch = startLatch;
			this.endlatch = endlatch;
		}

		@Override
		public void run() {
			try {
				latch.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			for (int i = 0; i < 100; i++) {
				controller.sendMessage(getMessageWithTimeStamp(i));
			}
			endlatch.countDown();
		}
	}

	@Test
	public void testAcknowledgeEvery5thMsgFromMultipleThreads() {

		controller.setAcknowledgementMode(5);

		CountDownLatch latch = new CountDownLatch(1);
		CountDownLatch endlatch = new CountDownLatch(10);

		long startTime = System.nanoTime();
		for (int j = 0; j < 10; j++) {
			NotificationSenderThread t = new NotificationSenderThread(
					this.controller, latch, endlatch);
			t.start();
		}
		latch.countDown();
		try {
			endlatch.await();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		controller.sendMessage(getMessageWithTimeStamp(LAST_MSG));

		assertTrue(dummySender.waitForEnd());

		System.out
				.println(String.format(
						"Took: %d",
						(startTime - dummySender.getLastMessageReceivedTime()) / 1000 / 1000));
		assertTrue(dummySender.getLastMessageReceivedTime() < startTime + 2000 * 1000 * 1000);
		assertTrue(!error);
	}

	@Test
	public void testTransmissionError() {
		dummySender.setDropMessage(100);
		long startTime = System.nanoTime();
		for (int i = 0; i < 1000; i++) {
			controller.sendMessage(getMessageWithTimeStamp(i));
		}
		controller.sendMessage(getMessageWithTimeStamp(LAST_MSG));
		assertTrue(dummySender.waitForEnd());
		assertTrue(error);

	}

	/**
	 * ensure that it is not a problem is the listeners for messages block.
	 */
	@Test
	public void testBlockingListener() {

		final boolean[] endOfWait = new boolean[1];
		endOfWait[0] = false;
		long startTime = System.nanoTime();
		controller.setAcknowledgementMode(5);

		NotificationAndErrorListener blocker = new NotificationAndErrorListener() {

			@Override
			public void sendMessage(Notification notification) {
				while (!endOfWait[0])
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			}
		};
		notificationListener.addClientListener(
				NotificationConstants.NOTIFY_MESSAGE, blocker);
		for (int i = 0; i < 1000; i++) {
			controller.sendMessage(getMessageWithTimeStamp(i));
		}
		controller.sendMessage(getMessageWithTimeStamp(LAST_MSG));

		assertTrue(dummySender.waitForEnd());
		assertFalse(error);
		endOfWait[0] = true;

	}

	@Test
	public void testSequenceNumberIsNull() {

		AttributeChangeNotification msg = getMessageWithTimeStamp(1);
		msg.setSequenceNumber(345435);
		boolean exception = false;
		try {
			controller.sendMessage(msg);
		} catch (IllegalArgumentException e) {
			exception = true;
		}
		assertTrue(
				"No illegalargumentexception was thrown when trying to send a notification with a non-null sequencenumber",
				exception);
		assertTrue(error == false);

	}

	public AttributeChangeNotification getMessageWithTimeStamp(int i) {
		return new AttributeChangeNotification(new Long(i), 0,
				System.nanoTime(), null, null,
				NotificationConstants.NOTIFY_MESSAGE, null, null);
	}
}
