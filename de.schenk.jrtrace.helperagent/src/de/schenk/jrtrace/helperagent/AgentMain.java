/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.jrtrace.helperlib.GroovyUtil;
import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.TraceReceiver;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.helperlib.TraceService;

public class AgentMain {

	/*
	 * The message codes the Agent sends
	 */

	public static final String AGENT_PORT = "PORT";

	/**
	 * Code to run a groovy script in the target machine
	 */
	public static final int AGENT_COMMAND_RUNGROOVY = 2;

	public static final int AGENT_COMMAND_RUNJAVA = 3;

	/** stops the listener for commands, resets standard out/err to normal */
	public static final int AGENT_COMMAND_STOPAGENT = 1;

	/**
	 * installs an XClass class
	 */
	public static final int AGENT_COMMAND_INSTALLENGINEXCLASS = 4;
	public static final int AGENT_COMMAND_INSTALL_BOOT_JAR = 5;
	public static final int AGENT_COMMAND_SETENV = 6;
	public static final int AGENT_CONNECT = 7;
	public static final String AGENT_READY = "READY";

	public static AgentMain theAgent = null;

	private static Instrumentation instrumentation;

	public AgentMain() {

	}

	/**
	 * check if the agent is still active (not null), if yes: stop it. start the
	 * new agent.
	 * 
	 * @param args
	 * 
	 * @param inst
	 */
	public static void launch(int port, Instrumentation inst) {

		HelperLib.setInstrumentation(inst);
		AgentMain.instrumentation = inst;
		if (theAgent != null) {
			theAgent.stop(false);
			theAgent = null;
		}
		if (theAgent == null) {
			theAgent = new AgentMain();
			theAgent.start(port);

		}
	}

	private PrintStream stdout;
	private PrintStream stderr;
	private TraceReceiver commandReceiver;

	private EngineXClassFileTransformer enginextransformer;

	private void start(int port) {

		commandReceiver = new TraceReceiver(port);
		commandReceiver.setDaemon();
		try {
			commandReceiver.start();
		} catch (IOException e) {

			throw new RuntimeException(
					"Error starting the command receiver of the Helper Agent. Agent install failed",
					e);

		}

		enginextransformer = new EngineXClassFileTransformer();
		instrumentation.addTransformer(enginextransformer, true);

		commandReceiver
				.addListener(AGENT_CONNECT, new RunConnectListener(this));
		commandReceiver.addListener(AGENT_COMMAND_RUNGROOVY,
				new RunGroovyListener());
		commandReceiver.addListener(AGENT_COMMAND_RUNJAVA,
				new RunJavaListener());
		commandReceiver.addListener(AGENT_COMMAND_INSTALLENGINEXCLASS,
				new InstallEngineXListener());
		commandReceiver.addListener(AGENT_COMMAND_SETENV,
				new SetEnvironmentVariableListener());
		commandReceiver.addListener(AGENT_COMMAND_INSTALL_BOOT_JAR,
				new AddToBootClassPathListener(instrumentation));
		commandReceiver.addListener(AGENT_COMMAND_STOPAGENT,
				new StopAgentListener(this));

	}

	public void redirectStandardOut(boolean enable) {
		if (enable) {
			stdout = System.out;
			stderr = System.err;
			System.setOut(new PrintStream(new RedirectingOutputStream(
					System.out)));
			System.setErr(new PrintStream(new RedirectingOutputStream(
					System.err, TraceSender.TRACECLIENT_STDERR_ID)));
		} else {
			System.setOut(stdout);
			System.setErr(stderr);
		}
	}

	/**
	 * stop all agent threads/activities/redirections
	 * 
	 * @param disconnect
	 *            : true: only disconnect, false: disable the command listener
	 *            as well / agent shutdown
	 */
	public void stop(boolean disconnect) {

		System.out.println(String.format("Agent.stop(%b)", disconnect));
		EngineXHelper.clearEngineX();

		try {

			redirectStandardOut(false);
			if (!disconnect) {
				commandReceiver.stop();
				instrumentation.removeTransformer(enginextransformer);
			}

			GroovyUtil.clearScriptCache();

			TraceService.getInstance().failSafeSend(
					TraceSender.TRACECLIENT_AGENT_STOPPED_ID, "STOPPED");
		} catch (IOException e) {
			throw new RuntimeException();
		} finally {

			theAgent = null;
		}
	}
}
