package de.schenk.jrtrace.service.internal;

import de.schenk.jrtrace.service.ICancelable;

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
	public boolean attach(ICancelable stopper) {

		setTraceSenderPort(port);
		return connectToAgent(stopper);

	}

	@Override
	public String getPID() {
		return String.format("Connected on port: %d", port);
	}

}
