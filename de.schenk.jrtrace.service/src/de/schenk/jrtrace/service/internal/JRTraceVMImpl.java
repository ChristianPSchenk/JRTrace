/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import de.schenk.jrtrace.helperagent.AgentMain;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;
import de.schenk.jrtrace.helperlib.TraceReceiver;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JarLocator;
import de.schenk.jrtrace.service.SynchronousWaitListener;

public class JRTraceVMImpl implements IJRTraceVM {

	VirtualMachine vm;
	private String thePID;

	/**
	 * will hold the last exception that occured during calls
	 */
	private Exception lastException;

	//

	private TraceReceiver receiver;

	private TraceSender helperAgentSender;

	public JRTraceVMImpl(String pid) {
		thePID = pid;
	}

	/**
	 * 
	 * @return true if successful, excception can be retrieved via
	 *         getException() if not successful
	 */
	public boolean attach() {
		VirtualMachine theVM = null;
		try {
			theVM = VirtualMachine.attach(thePID);
		} catch (AttachNotSupportedException e) {
			lastException = e;
			return false;

		} catch (IOException e) {
			lastException = e;
			return false;
		}

		vm = theVM;

		return true;
	}

	/**
	 * loads the jrtrace agent onto the vm. Checks whether the agent is loaded
	 * already and doesn't load it in this case.
	 */
	public boolean loadJRTraceAgent(boolean verbose, boolean debug) {

		try {

		} catch (Exception e) {
			lastException = e;
			return false;
		}

		if (!registerTraceListener())
			return false;

		CyclicBarrier agentReadyBarrier = new CyclicBarrier(2);
		try {
			receiver.addListener(TraceSender.TRACECLIENT_AGENT_ID,
					new HelperAgentMessageReceiver(this, agentReadyBarrier));
			String agent = JarLocator.getJRTraceHelperAgent();
			String helperPath = JarLocator.getHelperLibJar();
			vm.loadAgent(
					agent,
					String.format("port=%d,bootjar=%s",
							receiver.getServerPort(), helperPath));
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

		} catch (AgentLoadException e) {
			lastException = e;
			return false;
		} catch (AgentInitializationException e) {
			lastException = e;
			return false;
		} catch (IOException e) {
			lastException = e;
			return false;
		} catch (URISyntaxException e) {
			lastException = e;
			return false;
		}

		return true;

	}

	private boolean registerTraceListener() {
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

	public boolean detach() {
		boolean result = true;
		try {

		} catch (Exception e1) {
			lastException = e1;
			result = false;
		}

		if (helperAgentSender != null) {
			SynchronousWaitListener wait_for_stop = new SynchronousWaitListener(
					this, TraceSender.TRACECLIENT_AGENT_STOPPED_ID, "STOPPED");
			helperAgentSender.sendToServer("stopping",
					AgentMain.AGENT_COMMAND_STOPAGENT);
			wait_for_stop.waitForDone(10);
		}

		if (receiver != null) {
			try {
				receiver.stop();

			} catch (Exception e) {

				lastException = e;
				result = false;

			}
		}
		if (!detachVM()) {
			result = false;
		}
		return result;

	}

	private boolean detachVM() {
		try {
			if (vm != null) {
				vm.detach();
			}
		} catch (IOException e) {
			lastException = e;
			return false;
		}
		return true;
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

	@Override
	public String getPID() {
		return thePID;
	}

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

	public void setTraceSenderPort(int port) {

		helperAgentSender = new TraceSender(port);
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
}
