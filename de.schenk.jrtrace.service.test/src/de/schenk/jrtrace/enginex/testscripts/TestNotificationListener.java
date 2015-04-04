package de.schenk.jrtrace.enginex.testscripts;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.management.Notification;
import javax.management.NotificationListener;

public class TestNotificationListener implements NotificationListener {

	@Override
	public void handleNotification(Notification notification, Object handback) {
		if (notificationBarrier == null)
			return;
		lastNotification = notification;

		try {
			notificationBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			throw new RuntimeException("test");
		}
		notificationBarrier = null;

	}

	CyclicBarrier notificationBarrier = new CyclicBarrier(2);
	Notification lastNotification = null;

	public Notification waitForNotification() throws InterruptedException,
			BrokenBarrierException, TimeoutException {
		notificationBarrier.await(10, TimeUnit.SECONDS);
		return lastNotification;
	}

	public void reset() {
		lastNotification = null;
		notificationBarrier = new CyclicBarrier(2);

	}
}
