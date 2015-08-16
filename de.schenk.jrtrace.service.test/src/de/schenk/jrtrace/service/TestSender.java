package de.schenk.jrtrace.service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import de.schenk.jrtrace.helper.INotificationSender;
import de.schenk.jrtrace.service.internal.JRTraceBeanNotificationListener;

public class TestSender implements INotificationSender {

	LinkedBlockingQueue<Notification> queue = new LinkedBlockingQueue<>();

	private CountDownLatch latch = new CountDownLatch(1);
	private Throwable error;
	private volatile boolean stop = false;

	private Thread receiver;
	private long lastMsgReceived;

	private JRTraceBeanNotificationListener listener;

	private int dropMessage = -1;

	public TestSender(final JRTraceBeanNotificationListener listener) {
		this.listener = listener;
		receiver = new Thread() {

			@Override
			public void run() {
				try {
					while (!stop) {
						Notification msg = queue.take();

						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						long received = System.nanoTime();
						AttributeChangeNotification ch = (AttributeChangeNotification) msg;

						if (ch.getSource() instanceof Long) {
							long content = (Long) ch.getSource();

							System.out.println(ch.getSequenceNumber());
							if (content == CommunicationLayerTest.LAST_MSG) {
								lastMsgReceived = received;
								stop = true;
							}
							if (dropMessage == content)
								continue;
						}
						TestSender.this.listener.handleNotification(msg,
								new Object());

					}
				} catch (Throwable e) {
					e.printStackTrace();
					error = e;
				} finally {
					latch.countDown();
				}
			}
		};
		receiver.start();
	}

	@Override
	public void sendMessage(Notification notification) {

		queue.add(notification);
	}

	public void stop() {
		stop = true;
	}

	public long getLastMessageReceivedTime() {
		return lastMsgReceived;
	}

	/**
	 * 
	 * @return true for successful end, false for end with some internal error.
	 */
	public boolean waitForEnd() {
		while (true) {
			try {
				latch.await();
				return error == null;
			} catch (InterruptedException e) {
				// continue wait.
			}
		}
	}

	/**
	 * If used, the sender will drop the message with Object Long = i (for error
	 * simulation)
	 * 
	 * @param i
	 *            the message to drop
	 */
	public void setDropMessage(int i) {
		dropMessage = i;

	}
}
