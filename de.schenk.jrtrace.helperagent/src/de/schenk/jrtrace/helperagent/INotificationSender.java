package de.schenk.jrtrace.helperagent;

import javax.management.Notification;

public interface INotificationSender {

	void sendMessage(Notification notification);

}
