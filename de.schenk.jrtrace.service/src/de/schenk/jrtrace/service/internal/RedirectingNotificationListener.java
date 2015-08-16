/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import de.schenk.jrtrace.service.JRTraceMessageListener;
import de.schenk.jrtrace.service.NotificationAndErrorListener;

/**
 * 
 * A redirecting listener for NotificationConstants.MESSAGE messages.
 * 
 * @author Christian Schenk
 *
 */
public class RedirectingNotificationListener extends
		NotificationAndErrorListener {

	private JRTraceMessageListener redirect;

	public RedirectingNotificationListener(
			JRTraceMessageListener jrTraceMessageListener) {
		this.redirect = jrTraceMessageListener;
	}

	@Override
	public void sendMessage(Notification notification) {
		AttributeChangeNotification attr = (AttributeChangeNotification) notification;
		redirect.handleMessageReceived(attr.getOldValue());
	}

	@Override
	public int hashCode() {
		return redirect.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof RedirectingNotificationListener))
			return false;
		RedirectingNotificationListener objred = (RedirectingNotificationListener) obj;
		return this.redirect.equals(objred);
	}

}
