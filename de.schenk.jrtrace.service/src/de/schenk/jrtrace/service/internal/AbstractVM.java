package de.schenk.jrtrace.service.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import de.schenk.enginex.helper.NotificationUtil;
import de.schenk.jrtrace.helperagent.JRTraceMXBean;
import de.schenk.jrtrace.service.ICancelable;
import de.schenk.jrtrace.service.IJRTraceVM;

abstract public class AbstractVM implements IJRTraceVM {

	/**
	 * will hold the last exception that occured during calls
	 */
	protected Exception lastException;

	@Override
	synchronized public void setLogLevel(int i) {

		mbeanProxy.setLogLevel(i);
	}

	@Override
	public void abort() {
		mbeanProxy.abort();

	}

	@Override
	synchronized public boolean setSystemProperties(Properties p) {
		boolean result = true;
		Enumeration<Object> theKeys = p.keys();
		while (theKeys.hasMoreElements()) {
			String key = (String) theKeys.nextElement();
			mbeanProxy.setEnvironmentVariable(key, p.getProperty(key));
		}

		return result;
	}

	/**
	 * 
	 * @param disconnect
	 *            true: only disconnect, let the client continue to listen for
	 *            new connections, false: shutdown the agent command listener
	 */
	private boolean stopSender(boolean disconnect) {
		if (mbeanProxy != null) {
			try {
				mbeanProxy.stop(disconnect);
			} catch (UndeclaredThrowableException e) {
				// when target has been shut down:
				return false;
			}
			mbeanProxy = null;
		}
		return true;
	}

	@Override
	synchronized public void runJava(File jarFile, String theClassLoader,
			String className, String methodName) {
		theClassLoader = theClassLoader == null ? "" : theClassLoader;
		if (className == null)
			throw new IllegalArgumentException(
					"className has to be provided for runJava");
		if (methodName == null)
			throw new IllegalArgumentException(
					"methodName has to be provided for runJava");

		mbeanProxy.runJava(jarFile.toString(), theClassLoader, className,
				methodName);
	}

	@Override
	synchronized public void installEngineXClass(byte[][] classBytes) {
		mbeanProxy.installEngineXClass(classBytes);

	}

	@Override
	synchronized public void clearEngineX() {

		mbeanProxy.clearEngineX();

	}

	/**
	 * loads the jrtrace agent onto the vm. Checks whether the agent is loaded
	 * already and doesn't load it in this case.
	 * 
	 * @param the
	 *            port on which the server listens for commands.
	 * @param targetmachine
	 *            the target machine to connect to. null for localhost.
	 * @param stopper
	 */
	protected boolean connectToAgent(int port, String targetmachine,
			ICancelable stopper) {

		boolean result = createMXBeanClientConnection(port, targetmachine,
				stopper);
		if (!result)
			return result;

		final boolean connected[] = new boolean[1];
		connected[0] = false;

		mbeanProxy.connect();

		return true;

	}

	JMXConnector jmxc;
	MBeanServerConnection mxbeanConnection;
	JRTraceMXBean mbeanProxy;
	private JRTraceBeanNotificationListener mxbeanListener;

	/**
	 * 
	 * @param port
	 *            the port on which the RMI registry has been started
	 * @param targetmachine
	 *            the machine on which the RMI registry has been started, use
	 *            null for localhost / 127.0.0.1
	 * @param stopper
	 * @return
	 */
	private boolean createMXBeanClientConnection(int port,
			String targetmachine, ICancelable stopper) {
		if (targetmachine == null) {
			targetmachine = "localhost";
		}
		final JMXServiceURL url;
		Exception e = null;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {

			url = new JMXServiceURL(String.format(
					"service:jmx:rmi:///jndi/rmi://" + targetmachine
							+ ":%d/jmxrmi", port));

			Future<JMXConnector> result = executor
					.submit(new Callable<JMXConnector>() {

						@Override
						public JMXConnector call() throws Exception {
							return JMXConnectorFactory.connect(url, null);
						}
					});

			while (!result.isDone()) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// do nothing
				}
				if (stopper.isCanceled()) {

					lastException = new RuntimeException("Canceled by User");
					return false;
				}

			}
			jmxc = result.get();

			mxbeanConnection = jmxc.getMBeanServerConnection();

			ObjectName mbeanName = NotificationUtil.getJRTraceObjectName();

			mbeanProxy = (JRTraceMXBean) JMX.newMBeanProxy(mxbeanConnection,
					mbeanName, JRTraceMXBean.class, true);

			mxbeanListener = new JRTraceBeanNotificationListener(this);
			mxbeanConnection.addNotificationListener(mbeanName, mxbeanListener,
					null, null);

			return true;
		} catch (IOException | InstanceNotFoundException | InterruptedException
				| ExecutionException e2) {
			lastException = new RuntimeException("Connect failed. ", e2);
			return false;
		} finally {
			executor.shutdownNow();
		}

	}

	private boolean stopMXBeanClientConnection(boolean disconnectOnly) {

		boolean result = true;
		try {

			try {
				mxbeanConnection.removeNotificationListener(
						NotificationUtil.getJRTraceObjectName(),
						mxbeanListener, null, null);
			} catch (InstanceNotFoundException e) {
				lastException = e;
				result = false;

			}

			catch (ListenerNotFoundException e) {
				throw new RuntimeException(e);
			} finally {
				result = (result & stopSender(disconnectOnly));
				jmxc.close();
			}
		} catch (IOException e) {
			lastException = e;
			result = false;
		}

		return true;
	}

	/**
	 * Stop the communication with the client and disconnect
	 * 
	 * @param disconnectOnly
	 *            if false, the agent will shut down the listener
	 * @return true if successful
	 */
	protected boolean stopConnection(boolean disconnectOnly) {

		boolean result2 = stopMXBeanClientConnection(disconnectOnly);

		return result2;
	}

	@Override
	synchronized public boolean attach() {
		return attach(new DummyCancelable());
	}

	synchronized public boolean installJar(byte[] jar) {
		mbeanProxy.addToBootClassPath(jar);
		return true;

	}

	@Override
	public Exception getLastError() {
		return lastException;
	}

	@Override
	public void addClientListener(String notifyId,
			NotificationListener streamReceiver) {

		mxbeanListener.addClientListener(notifyId, streamReceiver);

	}

	@Override
	public void removeClientListener(String notifyId,
			NotificationListener listener) {
		mxbeanListener.removeClientListener(notifyId, listener);

	}
}
