/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import javax.management.AttributeChangeNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.ObjectName;

import de.schenk.jrtrace.helperlib.NotificationConstants;

public class NotificationUtil {

	public static final String MXBEAN_DOMAIN = "de.schenk.jrtrace";

	private static INotificationSender notificationSender;

	public static void setNotificationSender(INotificationSender jrtraceBean) {
		notificationSender = jrtraceBean;

	}

	static public void sendNotification(Notification notification) {
		if (notificationSender != null) {

			notificationSender.sendMessage(notification);
		}
	}

	static public void sendProgressNotification(String txt, int done, int total) {
		Notification not = new AttributeChangeNotification(
				NotificationUtil.getJRTraceObjectName(), 0, System.nanoTime(),
				txt, "Progress", NotificationConstants.NOTIFY_PROGRESS, done,
				total);
		sendNotification(not);
	}

	public static ObjectName getJRTraceObjectName() {
		try {
			return new ObjectName(MXBEAN_DOMAIN + ":type=JRTRace");
		} catch (MalformedObjectNameException e) {
			throw new RuntimeException("invalid name");

		}
	}

	public static void sendProblemNotification(String msg, String className,
			String methodName, String methodSignature) {
		Notification not = new AttributeChangeNotification(
				NotificationUtil.getJRTraceObjectName(), 0, System.nanoTime(),
				msg, methodSignature, NotificationConstants.NOTIFY_PROBLEM,
				className, methodName);
		sendNotification(not);
	}

	/**
	 * Sends tool error messages to the receivers.
	 * 
	 * @param errormessage
	 */
	public static void sendErrorNotification(String errormessage) {
		Notification not = new AttributeChangeNotification(
				NotificationUtil.getJRTraceObjectName(), 0, System.nanoTime(),
				errormessage, "Error", NotificationConstants.NOTIFY_ERROR, 0, 0);
		sendNotification(not);
	}

	public static void sendMessageNotification(Object msg) {
		Notification not = new AttributeChangeNotification(
				NotificationUtil.getJRTraceObjectName(), 0, System.nanoTime(),
				"msg", "Message", NotificationConstants.NOTIFY_MESSAGE, msg,
				null);
		sendNotification(not);

	}
}
