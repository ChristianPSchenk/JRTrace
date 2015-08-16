package de.schenk.jrtrace.service;

import de.schenk.jrtrace.helper.INotificationSender;

public abstract class NotificationAndErrorListener implements
		INotificationSender {

	/**
	 * Invoked, when a message loss is detected. Clients that rely on all
	 * messages being present should handle this.
	 */
	public void handleError() {
		// do nothing by default.
	}

}
