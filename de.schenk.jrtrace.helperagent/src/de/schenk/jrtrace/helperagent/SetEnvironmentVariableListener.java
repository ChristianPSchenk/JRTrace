package de.schenk.jrtrace.helperagent;

import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class SetEnvironmentVariableListener implements IJRTraceClientListener {

	@Override
	public void messageReceived(String clientSentence) {
		String[] splitted = clientSentence.split("=");
		System.setProperty(splitted[0], splitted[1]);

	}

}
