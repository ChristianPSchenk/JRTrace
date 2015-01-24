package de.schenk.jrtrace.service.internal;

import de.schenk.jrtrace.service.ICancelable;

public class JRTraceConnectingImpl extends AbstractVM {

	private int port;
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
	public String getPID() {
		return String.format("Connected on port: %d", port);
	}

}
