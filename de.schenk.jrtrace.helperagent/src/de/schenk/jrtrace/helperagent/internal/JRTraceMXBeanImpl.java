package de.schenk.jrtrace.helperagent.internal;

import de.schenk.jrtrace.helperagent.AgentMain;
import de.schenk.jrtrace.helperagent.JRTraceMXBean;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.helperlib.TraceService;

public class JRTraceMXBeanImpl implements JRTraceMXBean {

	private AgentMain agent;

	public JRTraceMXBeanImpl(AgentMain agent) {
		this.agent = agent;
	}

	InstallEngineXCommand installEngineXCommand = new InstallEngineXCommand();

	@Override
	public void connect(int senderPort) {

		TraceSender sender = new TraceSender(senderPort);

		TraceService.setSender(sender);

		agent.redirectStandardOut(true);

		System.out.println(String.format(
				" AgentMain connected and sending on (%d)", senderPort));

		TraceService.getInstance().failSafeSend(
				TraceSender.TRACECLIENT_AGENT_ID, AgentMain.AGENT_READY);
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	@Override
	public void installEngineXClass(String classOrJarLocation) {
		installEngineXCommand.installEngineX(classOrJarLocation);

	}

}
