package de.schenk.enginex.helper;

import javax.management.Notification;

public interface INotificationSender {

	void sendMessage(Notification notification);

}
