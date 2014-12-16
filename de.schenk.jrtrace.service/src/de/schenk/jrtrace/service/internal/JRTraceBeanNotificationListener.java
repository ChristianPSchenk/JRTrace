package de.schenk.jrtrace.service.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

public class JRTraceBeanNotificationListener implements NotificationListener {

	Map<String, Collection<NotificationListener>> listenerMap = new HashMap<>();

	private AbstractVM machine;

	public JRTraceBeanNotificationListener(AbstractVM abstractVM) {
		this.machine = abstractVM;
	}

	@Override
	public void handleNotification(Notification notification, Object object) {

		String type = notification.getType();

		Collection<NotificationListener> listenerCopy = new ArrayList<NotificationListener>();
		synchronized (listenerMap) {
			Collection<NotificationListener> listener = listenerMap.get(type);
			if (listener != null) {
				listenerCopy.addAll(listener);
			}
		}
		for (NotificationListener listener : listenerCopy) {
			listener.handleNotification(notification, object);
		}

	}

	synchronized public void removeClientListener(String notifyId,
			NotificationListener streamReceiver) {

		Collection<NotificationListener> col = listenerMap.get(notifyId);
		if (col == null) {
			col = new ArrayList<NotificationListener>();
			listenerMap.put(notifyId, col);
		}
		col.remove(streamReceiver);

	}

	synchronized public void addClientListener(String notifyId,
			NotificationListener streamReceiver) {
		Collection<NotificationListener> col = listenerMap.get(notifyId);
		if (col == null) {
			col = new ArrayList<NotificationListener>();
			listenerMap.put(notifyId, col);
		}
		col.add(streamReceiver);

	}
}
