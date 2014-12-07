package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

import de.schenk.jrtrace.helperlib.IJRTraceClientListener;

public class AddToBootClassPathListener implements IJRTraceClientListener {

	private Instrumentation inst;

	public AddToBootClassPathListener(Instrumentation instrumentation) {
		this.inst = instrumentation;
	}

	@Override
	public void messageReceived(String clientSentence) {
		try {
			inst.appendToBootstrapClassLoaderSearch(new JarFile(clientSentence));
		} catch (IOException e) {
			throw new RuntimeException(
					"Didn't find jar to append to bootclasspath:"
							+ clientSentence);
		}

	}

}
