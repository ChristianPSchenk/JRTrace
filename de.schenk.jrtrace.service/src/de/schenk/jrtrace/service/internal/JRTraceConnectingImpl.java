package de.schenk.jrtrace.service.internal;

import de.schenk.jrtrace.service.ICancelable;

public class JRTraceConnectingImpl extends AbstractVM {

	private String targetmachine;

	public JRTraceConnectingImpl(int port, String targetmachine) {
		this.port = port;
		this.targetmachine = targetmachine;
	}

	@Override
	public boolean detach() {

		return stopConnection(true);

	}

	@Override
	public boolean attach(ICancelable stopper) {

		return connectToAgent(port, targetmachine, stopper);

	}

	@Override
	public String getConnectionIdentifier() {
		return String.format("%s:%d", targetmachine == null ? "localhost"
				: targetmachine, port);
	}

	public String toString() {
		return "Connection to " + getConnectionIdentifier();
	}

}
