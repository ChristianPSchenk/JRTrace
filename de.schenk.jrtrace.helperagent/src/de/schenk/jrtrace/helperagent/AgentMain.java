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
import java.util.jar.JarFile;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.enginex.helper.NotificationUtil;
import de.schenk.jrtrace.helperagent.internal.JRTraceMXBeanImpl;
import de.schenk.jrtrace.helperlib.GroovyUtil;
import de.schenk.jrtrace.helperlib.HelperLib;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationConstants;

public class AgentMain {

	/*
	 * The message codes the Agent sends
	 */

	public static final String AGENT_READY = "READY";

	public static AgentMain theAgent = null;

	private static Instrumentation instrumentation;

	private JRTraceMXBeanImpl jrtraceBean;

	private ObjectName mxbeanName;

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
		JRLog.debug(String.format("JRTrace Agent launched port:%d", port));
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
			mxbeanRegistry = null;
			cs.stop();

		} catch (IOException e) {
			e.printStackTrace();
			// ignore exceptions on close
		}

	}

	synchronized private void startMXBeanServer(int port) {

		HashMap<String, String> environment = new HashMap<String, String>();
		environment.put("jmx.remote.x.daemon", "true");
		environment.put("com.sun.management.jmxremote.port",
				String.format("%d", port));
		environment.put("com.sun.management.jmxremote.authenticate", "false");
		environment.put("com.sun.management.jmxremote.ssl", "false");

		try {

			if (mxbeanRegistry == null) {

				try {
					mxbeanRegistry = LocateRegistry.getRegistry(port);

					// try to connect. In case of problem: createregistry.
					String[] list = mxbeanRegistry.list();
				} catch (RemoteException e) {

					mxbeanRegistry = LocateRegistry.createRegistry(port);
				}

			}

			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			JMXServiceURL url = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://:" + port + "/jmxrmi");
			cs = JMXConnectorServerFactory.newJMXConnectorServer(url,
					environment, mbs);

			cs.start();

			mxbeanName = NotificationUtil.getJRTraceObjectName();
			jrtraceBean = new JRTraceMXBeanImpl(this);
			if (!mbs.isRegistered(mxbeanName)) {
				mbs.registerMBean(jrtraceBean, mxbeanName);
			}
		} catch (InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private PrintStream stdout;
	private PrintStream stderr;

	private EngineXClassFileTransformer enginextransformer;

	private boolean stdout_isredirected = false;

	synchronized private void start(int port) {

		startMXBeanServer(port);

	}

	synchronized public void redirectStandardOut(boolean enable) {
		if (enable && !stdout_isredirected) {
			stdout_isredirected = true;
			stdout = System.out;
			stderr = System.err;
			System.setOut(new PrintStream(new RedirectingOutputStream(
					jrtraceBean, System.out)));
			System.setErr(new PrintStream(new RedirectingOutputStream(
					jrtraceBean, System.err,
					NotificationConstants.NOTIFY_STDERR)));
			NotificationUtil.setNotificationSender(jrtraceBean);
		} else {
			if (enable == false && stdout_isredirected) {
				stdout_isredirected = false;
				System.setOut(stdout);
				System.setErr(stderr);
				NotificationUtil.setNotificationSender(null);
			}
		}
	}

	synchronized public void connect() {

		if (enginextransformer != null)
			return;

		enginextransformer = new EngineXClassFileTransformer();
		instrumentation.addTransformer(enginextransformer, true);

		redirectStandardOut(true);

		JRLog.debug("AgentMain.connect() now connected");
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

		JRLog.debug(String.format("AgentMain.stop(disconnect:%b)", disconnect));
		EngineXHelper.clearEngineX();

		redirectStandardOut(false);

		if (enginextransformer != null)
			instrumentation.removeTransformer(enginextransformer);
		enginextransformer = null;

		GroovyUtil.clearScriptCache();

		theAgent = null;

		if (!disconnect) {
			stopMXBeanServer();

		}

	}

	public void appendToBootstrapClassLoaderSearch(JarFile jarFile) {
		instrumentation.appendToBootstrapClassLoaderSearch(jarFile);

	}

	public static AgentMain getAgentInstance() {
		return theAgent;
	}
}
