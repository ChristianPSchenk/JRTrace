package de.schenk.jrtrace.service.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Enumeration;
import java.util.Properties;

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
	synchronized public void runGroovy(String groovyOSPath, String className) {
		mbeanProxy.runGroovy(className, groovyOSPath);
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
	synchronized public void installEngineXClass(String fileForClass) {
		mbeanProxy.installEngineXClass(fileForClass);

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
	 * @param stopper
	 */
	protected boolean connectToAgent(int port, ICancelable stopper) {

		createMXBeanClientConnection(port);

		final boolean connected[] = new boolean[1];
		connected[0] = false;

		mbeanProxy.connect();

		return true;

	}

	JMXConnector jmxc;
	MBeanServerConnection mxbeanConnection;
	JRTraceMXBean mbeanProxy;
	private JRTraceBeanNotificationListener mxbeanListener;

	private void createMXBeanClientConnection(int port) {
		JMXServiceURL url;
		Exception e = null;
		for (int i = 0; i < 10; i++) {
			try {
				url = new JMXServiceURL(String.format(
						"service:jmx:rmi:///jndi/rmi://:%d/jmxrmi", port));
				jmxc = JMXConnectorFactory.connect(url, null);
				mxbeanConnection = jmxc.getMBeanServerConnection();

				ObjectName mbeanName = NotificationUtil.getJRTraceObjectName();

				mbeanProxy = (JRTraceMXBean) JMX.newMBeanProxy(
						mxbeanConnection, mbeanName, JRTraceMXBean.class, true);

				mxbeanListener = new JRTraceBeanNotificationListener(this);
				mxbeanConnection.addNotificationListener(mbeanName,
						mxbeanListener, null, null);

				return;
			} catch (IOException | InstanceNotFoundException e2) {
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

	private boolean stopMXBeanClientConnection(boolean disconnectOnly) {

	  boolean result=true;
		try {

	        try {
            mxbeanConnection.removeNotificationListener(NotificationUtil.getJRTraceObjectName(),
                  mxbeanListener, null, null);
          }
          catch (InstanceNotFoundException e) {
           lastException=e;
           result=false;
           
          }
	        
	        
          catch (ListenerNotFoundException e) {
            throw new RuntimeException(e);
          } finally
          {
            result=(result & stopSender(disconnectOnly));
			jmxc.close();
          }
		} catch (IOException e) {
			lastException=e;
			result=false;
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
		return attach(null);
	}

	synchronized public boolean installJar(String jar) {
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
