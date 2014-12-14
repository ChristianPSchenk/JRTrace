package de.schenk.jrtrace.service.internal;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.JMX;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import de.schenk.jrtrace.helperagent.AgentMain;
import de.schenk.jrtrace.helperagent.JRTraceMXBean;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;
import de.schenk.jrtrace.helperlib.TraceReceiver;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.service.ICancelable;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.SynchronousWaitListener;

abstract public class AbstractVM implements IJRTraceVM {

	protected TraceReceiver receiver;

	protected TraceSender helperAgentSender;

	/**
	 * will hold the last exception that occured during calls
	 */
	protected Exception lastException;

	@Override
	public boolean setSystemProperties(Properties p) {
		boolean result = true;
		Enumeration<Object> theKeys = p.keys();
		while (theKeys.hasMoreElements()) {
			String key = (String) theKeys.nextElement();
			String value = p.getProperty(key);
			if (!helperAgentSender.sendToServer(key + "=" + value,
					AgentMain.AGENT_COMMAND_SETENV))
				result = false;
		}

		return result;
	}

	private void setTraceSenderPort(int port) {

		helperAgentSender = new TraceSender(port);
	}

	/**
	 * 
	 * @param disconnect
	 *            true: only disconnect, let the client continue to listen for
	 *            new connections, false: shutdown the agent command listener
	 */
	private void stopSender(boolean disconnect) {
		if (helperAgentSender != null) {
			SynchronousWaitListener wait_for_stop = new SynchronousWaitListener(
					this, TraceSender.TRACECLIENT_AGENT_STOPPED_ID, "STOPPED");
			helperAgentSender.sendToServer(String.format("%b", disconnect),
					AgentMain.AGENT_COMMAND_STOPAGENT);
			wait_for_stop.waitForDone(10);
		}
	}

	@Override
	public void runGroovy(String groovyOSPath, String className) {

		helperAgentSender.sendToServer(className == null ? "" : className + ","
				+ groovyOSPath, AgentMain.AGENT_COMMAND_RUNGROOVY);
	}

	@Override
	public void runJava(File jarFile, String theClassLoader, String className,
			String methodName) {
		theClassLoader = theClassLoader == null ? "" : theClassLoader;
		if (className == null)
			throw new IllegalArgumentException(
					"className has to be provided for runJava");
		if (methodName == null)
			throw new IllegalArgumentException(
					"methodName has to be provided for runJava");

		helperAgentSender
				.sendToServer(jarFile + "," + theClassLoader + "," + className
						+ "," + methodName, AgentMain.AGENT_COMMAND_RUNJAVA);

	}

	@Override
	public void installEngineXClass(String fileForClass) {
		mbeanProxy.installEngineXClass(fileForClass);

	}

	@Override
	public void clearEngineX() {

		mbeanProxy.installEngineXClass("");

	}

	/**
	 * loads the jrtrace agent onto the vm. Checks whether the agent is loaded
	 * already and doesn't load it in this case.
	 * 
	 * @param the
	 *            port on which the server listens for commands.
	 * @param stopper
	 */
	protected boolean connectToAgent(int port, ICancelable stopper) {

		setTraceSenderPort(port);

		createMXBeanClientConnection();

		if (!registerTraceListener())
			return false;

		final boolean connected[] = new boolean[1];
		connected[0] = false;
		HelperAgentMessageReceiver helperAgentMessageReceiver = new HelperAgentMessageReceiver(
				this, Thread.currentThread());
		receiver.addListener(TraceSender.TRACECLIENT_AGENT_ID,
				helperAgentMessageReceiver);
		while (!helperAgentMessageReceiver.readyReceived()) {
			mbeanProxy.connect(receiver.getServerPort());

			try {
				System.out.println("Waiting for ready");
				Thread.sleep(500);

			} catch (InterruptedException e) {
				// next attempt
			}
			if (stopper != null && stopper.isCanceled()) {
				lastException = new Exception(
						"Connecting to target machine stopped.");
				return false;
			}

		}
		receiver.removeListener(TraceSender.TRACECLIENT_AGENT_ID,
				helperAgentMessageReceiver);

		return true;

	}

	private MBeanInfo mxbeanServer;
	JMXConnector jmxc;
	MBeanServerConnection mxbeanConnection;
	JRTraceMXBean mbeanProxy;

	private void createMXBeanClientConnection() {
		JMXServiceURL url;
		Exception e = null;
		for (int i = 0; i < 10; i++) {
			try {
				url = new JMXServiceURL(
						"service:jmx:rmi:///jndi/rmi://:9999/jmxrmi");
				jmxc = JMXConnectorFactory.connect(url, null);
				mxbeanConnection = jmxc.getMBeanServerConnection();

				ObjectName mbeanName = new ObjectName(AgentMain.MXBEAN_DOMAIN
						+ ":type=JRTRace");
				mxbeanServer = mxbeanConnection.getMBeanInfo(mbeanName);
				mbeanProxy = (JRTraceMXBean) JMX.newMBeanProxy(
						mxbeanConnection, mbeanName, JRTraceMXBean.class, true);
				return;
			} catch (IOException | InstanceNotFoundException
					| IntrospectionException | MalformedObjectNameException
					| ReflectionException e2) {
				e = e2;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// do nothing
			}
		}

		throw new RuntimeException("Connect failed after 10 tries", e);
	}

	private void stopMXBeanServerConnection() {

		try {

			jmxc.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Stop the communication with the client and disconnect
	 * 
	 * @param disconnectOnly
	 *            if false, the agent will shut down the listener
	 * @return true if successful
	 */
	protected boolean stopConnection(boolean disconnectOnly) {

		stopMXBeanServerConnection();
		stopSender(disconnectOnly);

		boolean result = true;
		if (receiver != null) {
			try {
				receiver.stop();

			} catch (Exception e) {

				lastException = e;
				result = false;

			}
		}
		return result;
	}

	@Override
	public boolean attach() {
		return attach(null);
	}

	protected boolean registerTraceListener() {
		receiver = new TraceReceiver();
		try {
			receiver.start();
		} catch (IOException e1) {
			lastException = e1;
			return false;
		}
		return true;
	}

	public boolean installJar(String jar) {
		return helperAgentSender.sendToServer(jar,
				AgentMain.AGENT_COMMAND_INSTALL_BOOT_JAR);

	}

	@Override
	public Exception getLastError() {
		return lastException;
	}

	@Override
	public void addClientListener(int id,
			IJRTraceClientListener iJRTraceClientListener) {
		receiver.addListener(id, iJRTraceClientListener);

	}

	@Override
	public void removeClientListener(int id, IJRTraceClientListener theListener) {
		receiver.removeListener(id, theListener);

	}
}
