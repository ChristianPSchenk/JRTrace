package de.schenk.jrtrace.service;

import javax.management.NotificationListener;

public abstract class NotificationAndErrorListener implements
		NotificationListener {

	/**
	 * Invoked, when a message loss is detected. Clients that rely on all
	 * messages being present should handle this.
	 */
	public void handleError() {
		// do nothing by default.
	}

}
