package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class AgentStarter {

	/**
	 * If the agent is installed twice into the same machine, it is not
	 * reinstalled, but agentmain is called again. So no need to recreate the
	 * agent instance and to trigger logging.
	 * 
	 * @param agentArgs
	 * @param inst
	 */

	public static void agentmain(String agentArgs, Instrumentation inst) {
		launch(agentArgs, inst);
	}

	public static void premain(String args, Instrumentation inst) {
		launch(args, inst);

	}

	private static void launch(String args, Instrumentation inst) {
		AgentArgs agentArgs = new AgentArgs(args);

		try {
			inst.appendToBootstrapClassLoaderSearch(new JarFile(agentArgs
					.getBootJar()));
		} catch (IOException e) {
			throw new RuntimeException("didn't find helperlib jar "
					+ agentArgs.getBootJar());
		}
		AgentMain.launch(agentArgs.getPort(), inst);

	}

}
