/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import de.schenk.jrtrace.helperagent.AgentMain;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class HelperAgentMessageReceiver implements IJRTraceClientListener {

	private AbstractVM vm;
	private boolean ready = false;
	private Thread notifyThread;

	/**
	 * 
	 * @param JRTraceVMImpl
	 *            the machine for which the trace port needs to be set.
	 * @param notify
	 *            the thread that needs to be interrupted() if the READY message
	 *            comes in.
	 */
	public HelperAgentMessageReceiver(AbstractVM JRTraceVMImpl, Thread notify) {
		vm = JRTraceVMImpl;
		notifyThread = notify;
		ready = false;
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
			System.out.println("Ready received");
			ready = true;
			notifyThread.interrupt();
		}
	}

	public boolean readyReceived() {
		return ready;
	}
}
