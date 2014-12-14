/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.jrtrace.helperagent.internal.JRTraceMXBeanImpl;
import de.schenk.jrtrace.helperlib.GroovyUtil;
import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.TraceReceiver;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.helperlib.TraceService;

public class AgentMain {

	public static final String MXBEAN_DOMAIN = "de.schenk.jrtrace";

	/*
	 * The message codes the Agent sends
	 */

	/**
	 * Code to run a groovy script in the target machine
	 */
	public static final int AGENT_COMMAND_RUNGROOVY = 2;

	public static final int AGENT_COMMAND_RUNJAVA = 3;

	/** stops the listener for commands, resets standard out/err to normal */
	public static final int AGENT_COMMAND_STOPAGENT = 1;

	public static final int AGENT_COMMAND_INSTALL_BOOT_JAR = 5;
	public static final int AGENT_COMMAND_SETENV = 6;

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

	static JMXConnectorServer cs = null;
	private static Registry mxbeanRegistry = null;

	synchronized private void stopMXBeanServer() {
		try {
			cs.stop();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	synchronized private void startMXBeanServer() {

		HashMap<String, String> environment = new HashMap<String, String>();
		environment.put("jmx.remote.x.daemon", "true");
		environment.put("com.sun.management.jmxremote.port",
				String.format("%d", 9999));
		environment.put("com.sun.management.jmxremote.authenticate", "false");
		environment.put("com.sun.management.jmxremote.ssl", "false");

		try {

			if (mxbeanRegistry == null) {

				try {
					mxbeanRegistry = LocateRegistry.getRegistry(9999);

					// try to connect. In case of problem: createregistry.
					String[] list = mxbeanRegistry.list();
				} catch (RemoteException e) {

					mxbeanRegistry = LocateRegistry.createRegistry(9999);
				}

			}

			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			JMXServiceURL url = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://:" + 9999 + "/jmxrmi");
			cs = JMXConnectorServerFactory.newJMXConnectorServer(url,
					environment, mbs);

			cs.start();

			ObjectName mxbeanName = new ObjectName(MXBEAN_DOMAIN
					+ ":type=JRTRace");
			JRTraceMXBean jrtraceBean = new JRTraceMXBeanImpl(this);
			if (!mbs.isRegistered(mxbeanName)) {
				mbs.registerMBean(jrtraceBean, mxbeanName);
			}
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException
				| MBeanRegistrationException | NotCompliantMBeanException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private PrintStream stdout;
	private PrintStream stderr;
	private TraceReceiver commandReceiver;

	private EngineXClassFileTransformer enginextransformer;

	synchronized private void start(int port) {

		startMXBeanServer();

		commandReceiver = new TraceReceiver(port);
		commandReceiver.setDaemon();
		try {
			commandReceiver.start();
		} catch (IOException e) {

			throw new RuntimeException(
					"Error starting the command receiver of the Helper Agent. Agent install failed",
					e);

		}

		commandReceiver.addListener(AGENT_COMMAND_RUNGROOVY,
				new RunGroovyListener());
		commandReceiver.addListener(AGENT_COMMAND_RUNJAVA,
				new RunJavaListener());

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

	synchronized public void connect(int senderPort) {
		System.out.println("connect");
		if (enginextransformer != null && TraceService.getInstance() != null
				&& TraceService.getInstance().getPort() == senderPort)
			return;
		System.out.println("connecting");
		enginextransformer = new EngineXClassFileTransformer();
		instrumentation.addTransformer(enginextransformer, true);

		TraceSender sender = new TraceSender(senderPort);
		TraceService.setSender(sender);
		redirectStandardOut(true);
		System.out.println(String.format(
				" AgentMain is connected and sending on (%d)", senderPort));
		TraceService.getInstance().failSafeSend(
				TraceSender.TRACECLIENT_AGENT_ID, AgentMain.AGENT_READY);
	}

	/**
	 * disconnect the agent from the current connection
	 */
	public void disconnect() {
		stop(true);
	}

	/**
	 * shut down the agent
	 */
	public void stop() {
		stop(false);
	}

	/**
	 * stop all agent threads/activities/redirections
	 * 
	 * @param disconnect
	 *            true: only disconnect, false: disable the command listener as
	 *            well , the agent in this case effectively shut down
	 */
	synchronized public void stop(boolean disconnect) {

		System.out.println(String.format("Agent.stop(%b)", disconnect));
		EngineXHelper.clearEngineX();

		try {

			redirectStandardOut(false);

			if (!disconnect) {
				stopMXBeanServer();
				commandReceiver.stop();
				instrumentation.removeTransformer(enginextransformer);
				enginextransformer = null;
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
