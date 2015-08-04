/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.service.internal;

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
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import de.schenk.jrtrace.helper.NotificationUtil;
import de.schenk.jrtrace.helperagent.JRTraceMXBean;
import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.helperlib.SerializationUtil;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
import de.schenk.jrtrace.service.ICancelable;
import de.schenk.jrtrace.service.IJRTraceVM;
import de.schenk.jrtrace.service.JRTraceMessageListener;
import de.schenk.jrtrace.service.NotificationAndErrorListener;

abstract public class AbstractVM implements IJRTraceVM {

	@Override
	public void addMessageListener(JRTraceMessageListener jrTraceMessageListener) {
		addClientListener(NotificationConstants.NOTIFY_MESSAGE,
				new RedirectingNotificationListenr(jrTraceMessageListener));

	}

	@Override
	public void removeMessageListener(
			JRTraceMessageListener jrTraceMessageListener) {
		removeClientListener(NotificationConstants.NOTIFY_MESSAGE,
				new RedirectingNotificationListenr(jrTraceMessageListener));

	}

	/**
	 * Helper Type: implement the runnable and call safeRun to properly set the
	 * error state.
	 * 
	 * @author Christiann Schenk
	 *
	 */
	abstract class SafeVMRunnable implements Runnable {

		final public boolean safeRun() {
			try {
				run();
			} catch (Throwable e) {

				lastException = e;
				return false;
			}
			return true;

		}
	}

	@Override
	public void addFailListener(final Runnable r) {

		jmxc.addConnectionNotificationListener(new NotificationListener() {

			@Override
			public void handleNotification(Notification notification,
					Object handback) {

				JMXConnectionNotification not = (JMXConnectionNotification) notification;
				if (not.getType().equals(JMXConnectionNotification.FAILED)) {
					r.run();
				}

			}
		}, null, null);

	}

	/* the MX Bean registry port on which the agent is listening */
	protected int port;

	//

	public int getPort() {
		return port;
	}

	/**
	 * will hold the last exception that occured during calls
	 */
	protected Throwable lastException;

	@Override
	synchronized public void setLogLevel(int i) {

		mbeanProxy.setLogLevel(i);
	}

	@Override
	public String[] getLoadedClasses() {
		return mbeanProxy.getLoadedClasses();
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
	synchronized public boolean invokeMethodAsync(final String theClassLoader,
			final String className, final String methodName,
			final Object... parameters) {

		final Object[] objArray;
		if (parameters != null) {
			objArray = parameters;
		} else {
			objArray = new Object[] { null };
		}
		SafeVMRunnable runnable = new SafeVMRunnable() {

			@Override
			public void run() {
				String useClassloader = theClassLoader == null ? ""
						: theClassLoader;
				if (className == null)
					throw new IllegalArgumentException(
							"className has to be provided for runJava");
				if (methodName == null)
					throw new IllegalArgumentException(
							"methodName has to be provided for runJava");
				byte[] bytes = SerializationUtil.serialize(objArray);
				mbeanProxy.invokeMethodAsync(useClassloader, className,
						methodName, bytes);

			}
		};
		return runnable.safeRun();
	}

	@Override
	synchronized public boolean installJRTraceClasses(final byte[][] classBytes) {

		return new SafeVMRunnable() {

			@Override
			public void run() {
				mbeanProxy.installEngineXClass(classBytes);

			}
		}.safeRun();

	}

	@Override
	synchronized public boolean clearEngineX() {
		return new SafeVMRunnable() {

			@Override
			public void run() {
				mbeanProxy.clearEngineX();

			}
		}.safeRun();
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

	/**
	 * Stop the communication with the client and disconnect
	 * 
	 * @param disconnectOnly
	 *            if false, the agent will shut down the listener
	 * @return true if successful
	 */
	protected boolean stopConnection(boolean disconnectOnly) {

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

	@Override
	synchronized public boolean attach() {
		return attach(new DummyCancelable());
	}

	synchronized public boolean installJar(byte[] jar) {
		mbeanProxy.addToBootClassPath(jar);
		return true;

	}

	@Override
	public Throwable getLastError() {
		return lastException;
	}

	@Override
	public void addClientListener(String notifyId,
			NotificationAndErrorListener streamReceiver) {

		mxbeanListener.addClientListener(notifyId, streamReceiver);

	}

	@Override
	public InjectStatus analyzeInjectionStatus(String className) {
		InjectStatus status = null;
		if (isDisconnected()) {
			status = new InjectStatus(StatusEntityType.JRTRACE_SESSION);

			status.setMessage(InjectStatus.MSG_NOT_CONNECTED);
			status.setInjected(StatusState.DOESNT_INJECT);
			return status;
		} else {
			try {
				byte[] statusBytes = mbeanProxy
						.analyzeInjectionStatus(className);
				status = (InjectStatus) SerializationUtil.deserialize(
						statusBytes, null);
				status.updateStatusFromChildren();
			} catch (RuntimeException e) {
				status = new InjectStatus(StatusEntityType.JRTRACE_SESSION);
				status.setMessage(InjectStatus.MSG_NOT_CONNECTED);
				status.setInjected(StatusState.DOESNT_INJECT);
			}

		}
		status.setEntityName(getConnectionIdentifier());
		return status;

	}

	/**
	 * 
	 * @return true, if the connection with the target is disconnected.
	 */
	@Override
	public boolean isDisconnected() {
		return mbeanProxy == null;
	}

	@Override
	public void removeClientListener(String notifyId,
			NotificationAndErrorListener listener) {
		mxbeanListener.removeClientListener(notifyId, listener);

	}

}
