package de.schenk.jrtrace.service.internal;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import de.schenk.jrtrace.helperagent.AgentMain;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;
import de.schenk.jrtrace.helperlib.TraceReceiver;
import de.schenk.jrtrace.helperlib.TraceSender;
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

	protected void setTraceSenderPort(int port) {

		helperAgentSender = new TraceSender(port);
	}

	/**
	 * 
	 * @param disconnect
	 *            true: only disconnect, let the client continue to listen for
	 *            new connections, false: shutdown the agent command listener
	 */
	protected void stopSender(boolean disconnect) {
		if (helperAgentSender != null) {
			SynchronousWaitListener wait_for_stop = new SynchronousWaitListener(
					this, TraceSender.TRACECLIENT_AGENT_STOPPED_ID, "STOPPED");
			helperAgentSender.sendToServer(String.format("%b", disconnect),
					AgentMain.AGENT_COMMAND_STOPAGENT);
			wait_for_stop.waitForDone(10);
		}
	}

	protected boolean stopReceiver() {
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
		helperAgentSender.sendToServer(fileForClass,
				AgentMain.AGENT_COMMAND_INSTALLENGINEXCLASS);

	}

	@Override
	public void clearEngineX() {
		helperAgentSender.sendToServer("",
				AgentMain.AGENT_COMMAND_INSTALLENGINEXCLASS);

	}

	/**
	 * loads the jrtrace agent onto the vm. Checks whether the agent is loaded
	 * already and doesn't load it in this case.
	 */
	protected boolean connectToAgent() {

		if (!registerTraceListener())
			return false;

		CyclicBarrier agentReadyBarrier = new CyclicBarrier(2);

		receiver.addListener(TraceSender.TRACECLIENT_AGENT_ID,
				new HelperAgentMessageReceiver(this, agentReadyBarrier));

		helperAgentSender.sendToServer(
				String.format("%d", receiver.getServerPort()),
				AgentMain.AGENT_CONNECT);

		try {
			agentReadyBarrier.await(10000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			lastException = e;
			return false;
		} catch (BrokenBarrierException e) {
			lastException = e;
			return false;
		} catch (TimeoutException e) {
			lastException = e;
			return false;
		}

		return true;

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
