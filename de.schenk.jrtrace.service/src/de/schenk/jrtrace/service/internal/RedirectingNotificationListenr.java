package de.schenk.jrtrace.service.internal;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;
import javax.management.NotificationListener;

import de.schenk.jrtrace.service.JRTraceMessageListener;

/**
 * 
 * A redirecting listener for NotificationConstants.MESSAGE messages.
 * 
 * @author Christian Schenk
 *
 */
public class RedirectingNotificationListenr implements NotificationListener {

	private JRTraceMessageListener redirect;

	public RedirectingNotificationListenr(
			JRTraceMessageListener jrTraceMessageListener) {
		this.redirect = jrTraceMessageListener;
	}

	@Override
	public void handleNotification(Notification notification, Object handback) {
		AttributeChangeNotification attr = (AttributeChangeNotification) notification;
		redirect.handleMessageReceived(attr.getOldValue());
	}

	@Override
	public int hashCode() {
		return redirect.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RedirectingNotificationListenr))
			return false;
		RedirectingNotificationListenr objred = (RedirectingNotificationListenr) obj;
		return this.redirect.equals(objred);
	}

}
