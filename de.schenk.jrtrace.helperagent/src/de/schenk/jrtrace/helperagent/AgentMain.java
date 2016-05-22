/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.jar.JarFile;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import de.schenk.jrtrace.helper.InstrumentationUtil;
import de.schenk.jrtrace.helper.NotificationUtil;
import de.schenk.jrtrace.helperagent.internal.JRTraceMXBeanImpl;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.NotificationConstants;

public class AgentMain {

	/*
	 * The message codes the Agent sends
	 */

	public static final String AGENT_READY = "READY";

	public static AgentMain theAgent = null;

	private JRTraceMXBeanImpl jrtraceBean;

	private ObjectName mxbeanName;

	public AgentMain() {

	}

	/**
	 * check if the agent is still active (not null), if yes: stop it. start the
	 * new agent.
	 * 
	 * @param args
	 * @param inst
	 */
	public static void launch(AgentArgs args, Instrumentation inst) {
		JRLog.debug(String.format(
				"JRTrace Agent launched port:%d on network interface: %s",
				args.getPort(),
				args.getServer() == null ? "<auto>" : args.getServer()));

		if (theAgent != null) {
			theAgent.stop(false);
			theAgent = null;
		}

		InstrumentationUtil.setInstrumentation(inst);

		theAgent = new AgentMain();
		theAgent.start(args);

	}

	private MBeanServer mbs = null;
	JMXConnectorServer cs = null;
	private Registry mxbeanRegistry = null;

	private void stopMXBeanServer() {
		synchronized (AgentMain.class) {

			mxbeanName = NotificationUtil.getJRTraceObjectName();

			if (mbs.isRegistered(mxbeanName)) {
				try {
					mbs.unregisterMBean(mxbeanName);

				} catch (MBeanRegistrationException | InstanceNotFoundException e) {
					throw new RuntimeException(e);
				}
			}

			if (cs != null) {
				try {
					cs.stop();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					cs = null;
				}
			}
			if (mxbeanRegistry != null) {

				try {
					UnicastRemoteObject.unexportObject(mxbeanRegistry, true);
				} catch (NoSuchObjectException e) {
					e.printStackTrace();
				} finally {
					mxbeanRegistry = null;
				}

			}

		}

	}

	private void startMXBeanServer(int port, String server, int buffersize) {

		synchronized (AgentMain.class) {

			HashMap<String, String> environment = new HashMap<String, String>();
			environment.put("jmx.remote.x.daemon", "true");
			environment.put("com.sun.management.jmxremote.port",
					String.format("%d", port));
			environment.put("com.sun.management.jmxremote.authenticate",
					"false");
			environment.put("com.sun.management.jmxremote.ssl", "false");

			environment.put("jmx.remote.x.notification.buffer.size",
					String.format("%s", buffersize));
			if (server != null) {
				System.setProperty("java.rmi.server.hostname", server);
			}

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

				mbs = ManagementFactory.getPlatformMBeanServer();

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

			} catch (InstanceAlreadyExistsException
					| MBeanRegistrationException | NotCompliantMBeanException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
	}

	private PrintStream stdout;
	private PrintStream stderr;

	private JRTraceClassFileTransformer enginextransformer;

	private boolean stdout_isredirected = false;

	private void start(AgentArgs args) {

		loadClass("sun.invoke.util.ValueConversions");
		loadClass("java.lang.invoke.LambdaForm");
		loadClass("java.lang.ref.ReferenceQueue");

		startMXBeanServer(args.getPort(), args.getServer(),
				args.getNotificationBufferSize());

	}

	/**
	 * Try to load the specified classes to force initialization
	 * 
	 * @param className
	 *            the fully qualified classname.
	 */
	private void loadClass(String className) {
		try {
			Class.forName(className);
		} catch (ClassNotFoundException e) {
			// do nothing
		}
	}

	public void redirectStandardOut(boolean enable) {
		synchronized (AgentMain.class) {

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
	}

	public void connect() {
		synchronized (AgentMain.class) {
			if (enginextransformer != null)
				return;
			enginextransformer = new JRTraceClassFileTransformer();
			InstrumentationUtil.getInstrumentation().addTransformer(
					enginextransformer, true);

			redirectStandardOut(true);

			JRLog.debug("AgentMain.connect() now connected");
		}
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
	public void stop(boolean disconnect) {
		synchronized (AgentMain.class) {
			JRLog.debug(String.format("AgentMain.stop(disconnect:%b)",
					disconnect));

			redirectStandardOut(false);

			if (enginextransformer != null)
				InstrumentationUtil.getInstrumentation().removeTransformer(
						enginextransformer);
			enginextransformer = null;

			theAgent = null;

			if (!disconnect) {
				// Thread stopServerThread = new Thread() {
				//
				// @Override
				// public void run() {
				// stopMXBeanServer();
				// };
				// };
				stopMXBeanServer();

				// stopServerThread.start();

			}
		}
	}

	public void appendToBootstrapClassLoaderSearch(JarFile jarFile) {
		InstrumentationUtil.getInstrumentation()
				.appendToBootstrapClassLoaderSearch(jarFile);

	}

	public static AgentMain getAgentInstance() {
		return theAgent;
	}
}
