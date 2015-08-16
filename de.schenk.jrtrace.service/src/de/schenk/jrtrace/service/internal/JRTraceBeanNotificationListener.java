/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.eclipse.core.runtime.Status;

import de.schenk.jrtrace.helper.INotificationSender;
import de.schenk.jrtrace.helperagent.ICommunicationControl;
import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.NotificationAndErrorListener;
import de.schenk.jrtrace.service.bundle.JRTraceServiceActivator;

/**
 * The central listener which will process and reroute all incoming messages
 * from the server.
 * 
 * In combination with the CommunicationController this receiver implements a
 * simple reliability protocol: The listener will respond to ack_req signals
 * with a call on the acknoweledge. The server (the CommunicationController)
 * will block if the acknowledge is not received.
 * 
 * This helps to ensure that all messages are transmitted since JMX will drop
 * messages if more than a predefined number of messages are queued (currently
 * 1000 by default). However this is not specified and therefore in addition the
 * NotificationListener also implements a possibility to get a error
 * information.
 * 
 * @author Christian Schenk
 *
 */
public class JRTraceBeanNotificationListener implements NotificationListener {

	Map<String, Collection<NotificationAndErrorListener>> listenerMap = new HashMap<>();

	BlockingQueue<Notification> queue = new LinkedBlockingQueue<Notification>();

	class NotificationThread extends Thread {
		public NotificationThread() {
			super("NotificationThread");
		}

		@Override
		public void run() {

			while (true) {
				Notification n;
				try {
					n = queue.poll(100, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					continue;
				}
				if (n == null) {
					synchronized (JRTraceBeanNotificationListener.this) {
						n = queue.peek();
						if (n == null) {
							notificationThread = null;
							break;
						}
					}
				}

				informListeners(n);

			}

		}

		public void addNotification(Notification notification) {
			while (true) {
				try {
					queue.put(notification);
					return;
				} catch (InterruptedException e) {
					// try forever to put that element in.
				}
			}

		}
	}

	NotificationThread notificationThread = null;

	private ICommunicationControl control;

	public JRTraceBeanNotificationListener() {

	}

	public JRTraceBeanNotificationListener(ICommunicationControl control) {
		this();
		setCommunicationControl(control);
	}

	public void setCommunicationControl(ICommunicationControl control) {

		this.control = control;

	}

	long lastSequenceNumber = -1;

	@Override
	public void handleNotification(Notification notification, Object object) {

		long newSequenceNumber = notification.getSequenceNumber();
		// System.out.println("JRtraceBeanlistener received " + type + " "
		// + String.format("%d", newSequenceNumber));
		if (lastSequenceNumber != -1
				&& newSequenceNumber != lastSequenceNumber + 1) {
			lastSequenceNumber = newSequenceNumber;
			handleMessageLost();
		} else {
			lastSequenceNumber = newSequenceNumber;
		}
		String type = getNotificationType(notification);

		if (type.equals(NotificationConstants.NOTIFY_ACKNOWLEDGEREQUEST)) {
			if (control != null) {

				control.acknowledge(notification.getSequenceNumber());
			}
			return;
		}
		synchronized (this) {
			if (notificationThread == null) {
				notificationThread = new NotificationThread();
				notificationThread.start();

			}
			notificationThread.addNotification(notification);
		}

	}

	public void informListeners(Notification notification) {
		Collection<INotificationSender> listenerCopy = getListenerListCopy(getNotificationType(notification));
		for (INotificationSender listener : listenerCopy) {
			try {
				listener.sendMessage(notification);
			} catch (Throwable e) {
				JRTraceServiceActivator
						.getActivator()
						.getLog()
						.log(new Status(
								Status.ERROR,
								JRTraceServiceActivator.ID,
								"Exception while invoking handleNotification on listener.",
								e));
			}
		}
	}

	public String getNotificationType(Notification notification) {
		String type = notification.getType();
		if (notification instanceof AttributeChangeNotification) {
			AttributeChangeNotification a = (AttributeChangeNotification) notification;
			type = a.getAttributeType();
		}
		return type;
	}

	public Collection<INotificationSender> getListenerListCopy(String type) {
		Collection<INotificationSender> listenerCopy = new ArrayList<INotificationSender>();
		synchronized (this) {
			Collection<NotificationAndErrorListener> listener = listenerMap
					.get(type);
			if (listener != null) {
				listenerCopy.addAll(listener);
			}
		}
		return listenerCopy;
	}

	private void handleMessageLost() {

		Set<NotificationAndErrorListener> allListeners = new HashSet<>();
		synchronized (this) {
			Collection<Collection<NotificationAndErrorListener>> listener = listenerMap
					.values();
			for (Collection<NotificationAndErrorListener> l : listener) {
				allListeners.addAll(l);
			}
		}
		for (NotificationAndErrorListener l2 : allListeners) {
			try {
				l2.handleError();
			} catch (Throwable e) {
				JRTraceServiceActivator
						.getActivator()
						.getLog()
						.log(new Status(
								Status.ERROR,
								JRTraceServiceActivator.ID,
								"Exception while invoking handleError on listener.",
								e));
			}
		}

	}

	synchronized public void removeClientListener(String notifyId,
			NotificationAndErrorListener streamReceiver) {

		Collection<NotificationAndErrorListener> col = listenerMap
				.get(notifyId);
		if (col == null) {
			col = new ArrayList<NotificationAndErrorListener>();
			listenerMap.put(notifyId, col);
		}
		col.remove(streamReceiver);

	}

	synchronized public void addClientListener(String notifyId,
			NotificationAndErrorListener streamReceiver) {
		Collection<NotificationAndErrorListener> col = listenerMap
				.get(notifyId);
		if (col == null) {
			col = new ArrayList<NotificationAndErrorListener>();
			listenerMap.put(notifyId, col);
		}
		col.add(streamReceiver);

	}
}
