/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import de.schenk.jrtrace.helperagent.AgentMain;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class HelperAgentMessageReceiver implements IJRTraceClientListener {

	private AbstractVM vm;
	private CyclicBarrier readySignal;

	public HelperAgentMessageReceiver(AbstractVM JRTraceVMImpl,
			CyclicBarrier agentReadyBarrier) {
		vm = JRTraceVMImpl;
		readySignal = agentReadyBarrier;
	}

	@Override
	public void messageReceived(String clientSentence) {
		if (clientSentence.startsWith(AgentMain.AGENT_PORT)) {
			String portString = clientSentence.substring(AgentMain.AGENT_PORT
					.length() + 1);
			int port = Integer.parseInt(portString);
			vm.setTraceSenderPort(port);
		}
		if (clientSentence.startsWith(AgentMain.AGENT_READY)) {
			try {
				readySignal.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				throw new RuntimeException(
						"Barrierbroken while waiting for agent to respond.", e);
			}
		}
	}
}
