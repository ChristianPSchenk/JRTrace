/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import javax.management.Notification;

public interface INotificationSender {

	void sendMessage(Notification notification);

}
