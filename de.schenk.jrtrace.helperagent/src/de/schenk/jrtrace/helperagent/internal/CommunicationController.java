package de.schenk.jrtrace.helperagent.internal;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import de.schenk.jrtrace.helper.INotificationSender;
import de.schenk.jrtrace.helper.NotificationUtil;
import de.schenk.jrtrace.helperlib.NotificationConstants;

public class CommunicationController implements ICommunicationController {

	private INotificationSender emitter;

	/**
	 * The sequence numbers. The communication controller will number all
	 * outgoind notifications.
	 */
	private long sequenceNumber = 0;
	/**
	 * 0: no acknowledgement required n>0: acknowledgement expected after at
	 * least 5 messages.
	 */
	private int acknowledgement = 0;
	/**
	 * stores the last sequenceId that was acknowledged by the client
	 */
	private long lastAcknowledgeRequest = -1;

	public CommunicationController(INotificationSender dummySender) {
		emitter = dummySender;
	}

	@Override
	public void setAcknowledgementMode(int n) {
		if (n != 0 && n < 5)
			throw new IllegalArgumentException(
					"Acknowledgement can be off (n==0) or at max after every 5th message n>=5");
		lastAcknowledgeRequest = -1;
		this.acknowledgement = n;
		synchronized (latch) {
			latch.notifyAll();
		}

	}

	Object latch = new Object();

	@Override
	public void acknowledge(long id) {
		if (id != lastAcknowledgeRequest) {
			throw new RuntimeException(String.format(
					"invalid acknoweledgement:Expected %d, got %d",
					lastAcknowledgeRequest, id));
		}
		lastAcknowledgeRequest = -1;

		synchronized (latch) {
			latch.notifyAll();
		}

	}

	/**
	 * 
	 */
	@Override
	public void sendMessage(Notification notification) {
		if (notification.getSequenceNumber() != 0)
			throw new IllegalArgumentException(
					"The sequence number is managed by the CommunicationController and must therefore be set to 0.");
		synchronized (this) {
			while (acknowledgement != 0 && lastAcknowledgeRequest != -1
					&& ((sequenceNumber + 1) % acknowledgement == 0)) {
				try {

					synchronized (latch) {
						latch.wait(1000);
					}

				} catch (InterruptedException e) {
					// do nothing, continue to wait.
				}

			}

			sequenceNumber++;
			if (acknowledgement != 0 && (sequenceNumber % acknowledgement == 0)) {
				lastAcknowledgeRequest = sequenceNumber;

				AttributeChangeNotification acknowledgeRequestNotification = new AttributeChangeNotification(
						NotificationUtil.getJRTraceObjectName(),
						sequenceNumber, System.nanoTime(), null, null,
						NotificationConstants.NOTIFY_ACKNOWLEDGEREQUEST, null,
						null);
				emitter.sendMessage(acknowledgeRequestNotification);
				sequenceNumber++;
			}
			notification.setSequenceNumber(sequenceNumber);

			emitter.sendMessage(notification);
		}
	}
}
