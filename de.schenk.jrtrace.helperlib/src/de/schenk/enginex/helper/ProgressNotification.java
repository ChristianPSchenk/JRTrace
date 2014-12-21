package de.schenk.enginex.helper;

import javax.management.Notification;

import de.schenk.jrtrace.helperlib.NotificationConstants;

public class ProgressNotification extends Notification {

	private static int sequence = 0;

	private int total;
	private int done;

	public ProgressNotification(String txt, int done, int total) {
		super(NotificationConstants.NOTIFY_PROGRESS, NotificationUtil
				.getJRTraceObjectName(), sequence++, txt);

		this.total = total;
		this.done = done;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -6110523226111238155L;

}
