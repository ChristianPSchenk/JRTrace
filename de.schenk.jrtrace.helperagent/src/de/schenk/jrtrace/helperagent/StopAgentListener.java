/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class StopAgentListener implements IJRTraceClientListener {

	private AgentMain theAgent;

	public StopAgentListener(AgentMain agentMain) {
		theAgent = agentMain;
	}

	@Override
	public void messageReceived(String clientSentence) {
		boolean disc = false;
		if ("true".equals(clientSentence))
			disc = true;

		theAgent.stop(disc);

	}

}
