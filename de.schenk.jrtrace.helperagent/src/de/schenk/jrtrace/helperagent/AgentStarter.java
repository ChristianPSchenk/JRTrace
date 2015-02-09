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

		primePattern();

		AgentMain.launch(agentArgs.getPort(), agentArgs.getServer(), inst);

	}

	/**
	 * For small applications the Pattern classes might not be loaded when the
	 * first jrtrace transformation is triggered. That will cause a
	 * ClassCircularityException.
	 * 
	 * This method just makes sure that the Pattern and related classes are
	 * loaded.
	 */
	private static void primePattern() {

		String string = "ateststring";
		if (string.matches("\\.a.*b\n")) {

		}
	}

}
