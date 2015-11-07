package de.schenk.jrtrace.enginex.testscripts;

import java.util.HashSet;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import de.schenk.jrtrace.service.NotificationAndErrorListener;

public class InstallErrorListener extends NotificationAndErrorListener {

	public HashSet<String> messages = new HashSet<String>();

	@Override
	public void sendMessage(Notification notification) {
		AttributeChangeNotification not = (AttributeChangeNotification) notification;
		String classname = (String) not.getOldValue();
		String method = (String) not.getNewValue();
		messages.add(classname + "|" + method + "|" + not.getMessage());

	}

	/**
	 * 
	 * @return true, if a message was received that contains the three text
	 *         elements
	 */
	boolean messageContains(String classname, String method, String msgpart) {
		for (String msg : messages) {
			if (msg.contains(classname) && msg.contains(method)
					&& msg.contains(msgpart))
				return true;
		}
		return false;
	}

}
