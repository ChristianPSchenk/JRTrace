package de.schenk.jrtrace.helperagent;

import de.schenk.jrtrace.helperlib.IJRTraceClientListener;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.helperlib.TraceService;

public class RunConnectListener implements IJRTraceClientListener {

	private AgentMain agent;

	public RunConnectListener(AgentMain agentMain) {
		this.agent = agentMain;
	}

	@Override
	public void messageReceived(String clientSentence) {
		int senderPort = Integer.parseInt(clientSentence);
		TraceSender sender = new TraceSender(senderPort);

		TraceService.setSender(sender);

		agent.redirectStandardOut(true);

		System.out.println(String.format(
				" AgentMain connected and sending on (%d)", senderPort));

		TraceService.getInstance().failSafeSend(
				TraceSender.TRACECLIENT_AGENT_ID, AgentMain.AGENT_READY);
	}

}
