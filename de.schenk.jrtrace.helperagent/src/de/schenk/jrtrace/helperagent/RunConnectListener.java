package de.schenk.jrtrace.helperagent;

import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class RunConnectListener implements IJRTraceClientListener {

	private AgentMain agent;

	public RunConnectListener(AgentMain agentMain) {
		this.agent = agentMain;
	}

	@Override
	public void messageReceived(String clientSentence) {
		int senderPort = Integer.parseInt(clientSentence);

		agent.connect(senderPort);
	}

}
