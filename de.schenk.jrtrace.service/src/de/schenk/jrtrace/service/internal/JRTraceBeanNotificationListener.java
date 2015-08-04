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

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import org.eclipse.core.runtime.Status;

import de.schenk.jrtrace.service.NotificationAndErrorListener;
import de.schenk.jrtrace.service.bundle.JRTraceServiceActivator;

public class JRTraceBeanNotificationListener implements NotificationListener {

	Map<String, Collection<NotificationAndErrorListener>> listenerMap = new HashMap<>();

	private AbstractVM machine;

	public JRTraceBeanNotificationListener(AbstractVM abstractVM) {
		this.machine = abstractVM;
	}

	long lastSequenceNumber = -1;

	@Override
	public void handleNotification(Notification notification, Object object) {

		String type = notification.getType();

		long newSequenceNumber = notification.getSequenceNumber();
		if (lastSequenceNumber != -1
				&& newSequenceNumber != lastSequenceNumber + 1) {
			lastSequenceNumber = newSequenceNumber;
			handleMessageLost();
		} else {
			lastSequenceNumber = newSequenceNumber;
		}

		if (notification instanceof AttributeChangeNotification) {
			AttributeChangeNotification a = (AttributeChangeNotification) notification;
			type = a.getAttributeType();
		}

		Collection<NotificationListener> listenerCopy = getListenerListCopy(type);
		for (NotificationListener listener : listenerCopy) {
			try {
				listener.handleNotification(notification, object);
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

	public Collection<NotificationListener> getListenerListCopy(String type) {
		Collection<NotificationListener> listenerCopy = new ArrayList<NotificationListener>();
		synchronized (listenerMap) {
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
		synchronized (listenerMap) {
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
			NotificationListener streamReceiver) {

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
