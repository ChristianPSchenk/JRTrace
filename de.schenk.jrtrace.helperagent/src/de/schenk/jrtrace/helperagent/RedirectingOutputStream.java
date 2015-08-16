/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import javax.management.Notification;

import de.schenk.jrtrace.helper.INotificationSender;
import de.schenk.jrtrace.helper.NotificationUtil;
import de.schenk.jrtrace.helperlib.NotificationConstants;

public class RedirectingOutputStream extends OutputStream {

	private PrintStream outPrinter;
	private StringWriter internalWriter;

	private static long sequence = 0;
	private INotificationSender bean;
	private String comId;

	public RedirectingOutputStream(INotificationSender jrtraceBean,
			PrintStream out) {

		this(jrtraceBean, out, NotificationConstants.NOTIFY_STDOUT);

	}

	public RedirectingOutputStream(INotificationSender jrtraceBean,
			PrintStream out, String id) {
		this.bean = jrtraceBean;
		comId = id;
		outPrinter = out;
		internalWriter = new StringWriter();
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		outPrinter.write(arg0, arg1, arg2);
		internalWriter.write(new String(arg0), arg1, arg2);
		writeMessage();
	}

	@Override
	public void write(int b) throws IOException {
		outPrinter.write(b);
		internalWriter.write(b);
		if (b == 13) {
			writeMessage();

		}

	}

	boolean inSend = false;

	synchronized private void writeMessage() {
		String msg = internalWriter.toString();
		if (!inSend) {
			try {
				inSend = true;

				NotificationUtil.sendNotification(new Notification(comId,
						NotificationUtil.getJRTraceObjectName(), 0, msg));

			} finally {
				inSend = false;
			}
		} else {
			outPrinter.print(msg);
		}
		internalWriter.getBuffer().setLength(0);
	}
}
