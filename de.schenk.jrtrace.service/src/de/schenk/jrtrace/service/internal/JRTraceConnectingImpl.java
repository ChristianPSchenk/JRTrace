package de.schenk.jrtrace.service.internal;

public class JRTraceConnectingImpl extends AbstractVM {

	private int port;

	public JRTraceConnectingImpl(int port) {
		this.port = port;
	}

	@Override
	public boolean detach() {

		stopSender(true);

		return stopReceiver();

	}

	@Override
	public boolean attach() {

		setTraceSenderPort(port);
		return connectToAgent();

	}

	@Override
	public String getPID() {
		return String.format("Connected on port: %d", port);
	}

}
