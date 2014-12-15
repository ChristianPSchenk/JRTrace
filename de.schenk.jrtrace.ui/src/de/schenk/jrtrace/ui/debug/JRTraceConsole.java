/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.debug;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.IJRTraceVM;

public class JRTraceConsole {
	String title = "";
	private MessageConsoleStream stream;
	private IConsole[] theConsole;
	private IJRTraceVM machine;
	private MessageConsoleStream errorstream;
	private StreamReceiver errorstreamReceiver;
	private StreamReceiver streamReceiver;

	public JRTraceConsole(IJRTraceVM machine) {
		title = machine.getPID();
		this.machine = machine;
	}

	public void start() {

		MessageConsole console;
		try {
			console = new MessageConsole(
					"JRTrace",
					ImageDescriptor
							.createFromURL(new URL(
									"platform:/plugin/de.schenk.jrtrace.ui/icons/jrtrace_icon_16px.gif")));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		console.activate();
		theConsole = new IConsole[] { console };
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(theConsole);
		stream = console.newMessageStream();
		streamReceiver = new StreamReceiver(stream);
		errorstream = console.newMessageStream();
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				errorstream.setColor(Display.getDefault().getSystemColor(
						SWT.COLOR_RED));
			}
		});

		errorstreamReceiver = new StreamReceiver(errorstream);
		stream.println("=== JRTrace Console on " + title + " ===");

		machine.addClientListener(NotificationConstants.NOTIFY_STDOUT,
				streamReceiver);
		machine.addClientListener(NotificationConstants.NOTIFY_STDERR,
				errorstreamReceiver);

	}

	class StreamReceiver implements NotificationListener {
		private MessageConsoleStream stream;

		public StreamReceiver(MessageConsoleStream theStream) {
			this.stream = theStream;
		}

		@Override
		public void handleNotification(Notification notification,
				Object handback) {
			String clientSentence = notification.getMessage();
			if (clientSentence != null) {
				if (stream != null && !stream.isClosed()) {
					try {
						stream.write(clientSentence.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

	}

	public boolean stop() {
		machine.removeClientListener(NotificationConstants.NOTIFY_STDOUT,
				streamReceiver);
		machine.removeClientListener(NotificationConstants.NOTIFY_STDERR,
				errorstreamReceiver);
		if (stream != null) {

			try {
				stream.close();
				stream = null;
			} catch (IOException e) {
				return false;

			}
		}
		return true;
	}

	public void close() {
		stop();
		ConsolePlugin.getDefault().getConsoleManager()
				.removeConsoles(theConsole);
	}

}
