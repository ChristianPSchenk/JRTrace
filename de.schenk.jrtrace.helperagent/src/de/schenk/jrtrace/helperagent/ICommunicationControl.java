package de.schenk.jrtrace.helperagent;


public interface ICommunicationControl {
	/**
	 * 
	 * 
	 * @param n
	 *            =0: acknowledgement mode off. n>0: notification sending will
	 *            block if no acknowledgement is received from the client after
	 *            n notifications.
	 */
	public void setAcknowledgementMode(int n);

	/**
	 * If the client enables the acknowledgement mode with
	 * setAcknowledgementMode, the server expects a call to acknowledge after at
	 * least n messages. After sending n notifications the server will block
	 * further sending until the acknowledge is received.
	 * 
	 */
	public void acknowledge(long id);
}
