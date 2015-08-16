/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import java.util.Properties;

import de.schenk.jrtrace.helperlib.status.InjectStatus;

/**
 * 
 * Allows to connect to another JVM and uploads the JRTrace Agent.
 * 
 * Error handling: most methods return true/false for success and provide
 * details via getLastException() in case of a problem.
 *
 */
public interface IJRTraceVM {

	/**
	 * 
	 * @return the port on which the JMX Server in the target machine was
	 *         started and is listening.
	 */
	int getPort();

	/**
	 * detaches from the virtual machine.
	 * 
	 * @return true if detach was successful. False if error.
	 */
	boolean detach();

	/**
	 * attaches to the virtual machine and tries to upload the jrtrace agent
	 * Note: this call should be falled by a call to installJRTraceAgent, else
	 * several options might not work
	 * 
	 * @param stopper
	 *            if the user stops the attach process
	 * @return error or not
	 */
	boolean attach(ICancelable stopper);

	/**
	 * Makes a given jar file availble on the boot classpath of the target
	 * machine.
	 * 
	 * @param bytes
	 *            the bytes of the jar file
	 * @return true on success
	 */
	public boolean installJar(byte[] bytes);

	/**
	 * 
	 * @return the connection identifier of this connection. This may be a
	 *         process identifer for a local connection but may also be a
	 *         server:port description for remote connections. This value can be
	 *         used to identify the connection to the user.
	 */
	String getConnectionIdentifier();

	/**
	 * 
	 * @return true, if the connection with the target is disconnected.
	 */
	public boolean isDisconnected();

	/**
	 * The last exception that occurred (e.g. after an unsuccessful, return code
	 * false, call) The exception will not be cleared after a successful call to
	 * another function.
	 * 
	 * @return The last exception after an error.
	 */
	Throwable getLastError();

	/**
	 * 
	 * Sets java system properties in the target jvm.
	 * 
	 * @param props
	 *            the properties to set
	 * @return true on success.
	 */
	boolean setSystemProperties(Properties props);

	/**
	 * Invoke a method on one of the JRTrace classes. The method will be invoked
	 * asynchronously (a new thread will be started to execute the code) and the
	 * success only reports, whether it was possible to identify the method and
	 * start it.
	 * <p>
	 * Note: For non-static methods, the invocation occurs on the default object
	 * instance that was created by JRTrace for injection. However if this class
	 * isn't injected (a helper class without JRTrace annotations) there is no
	 * default object. In this case a new instance of the class is created for
	 * each invocation.
	 * </p>
	 * 
	 * @param theClassLoader
	 *            if the jrtrace class has classloaderpolicy TARGET: the name of
	 *            the class to use to obtain the classloader. Else use null.
	 * @param className
	 *            the name of the jrtrace class to invoke a method on
	 * @param methodName
	 *            the name of the method to invoke. This method may be static or
	 *            non-static. For non-static methods the standard JRTrace
	 *            mechanisms for obtaining a instance are used.
	 * @param parameters
	 *            the parameters will be used to invoke the method. Note: all
	 *            parameters must be serializable and deserializable on the
	 *            target machine. That basically restricts the allowed
	 *            parameters to standard java types.
	 * @return true on success, use getLastException() on false
	 */
	boolean invokeMethodAsync(String theClassLoader, String className,
			String methodName, Object... parameters);

	/**
	 * Install the provide JRTrace classes. Uninstalls all previously installed
	 * classes.
	 * 
	 * @param classByteArray
	 *            an array with the array byte[], one for each JRTrace class to
	 *            be installed, e.g. classByteArray[0] -> the bytes of the first
	 *            class to install
	 */
	boolean installJRTraceClasses(byte[][] classByteArray);

	/**
	 * Set the agents log level. Levels defined in JRLog constants.
	 * 
	 * @param level
	 *            see JRLog for levels.
	 */
	void setLogLevel(int i);

	/**
	 * Remove all installed JRTrace classes from the target.
	 * 
	 * @return true on success
	 */
	boolean clearEngineX();

	/**
	 * Initialize the connection.
	 * 
	 * @return true on success
	 */
	boolean attach();

	/**
	 * Add a listener to receive various types of messages from the machine.
	 * 
	 * @param notifyId
	 *            see {@link de.schenk.jrtrace.helperlib.NotificationConstants}
	 *            for supported message types. To receive messages sent via
	 *            HelperLib.sendMessage use
	 *            {@link de.schenk.jrtrace.helperlib.NotificationConstants.NOTIFY_MESSAGE}
	 * @param streamReceiver
	 */
	void addClientListener(String notifyId,
			NotificationAndErrorListener streamReceiver);

	/**
	 * Add a listener that will be informed on connection loss.
	 * 
	 * @param failRunnable
	 *            the runnable that will be called on connection loss.
	 */
	void addFailListener(Runnable failRunnable);

	/**
	 * 
	 * Remove an existing listener
	 * 
	 * @param notifyId
	 *            the ID on which the listener was listening
	 * @param listener
	 *            the listener to remove
	 */
	void removeClientListener(String notifyId,
			NotificationAndErrorListener listener);

	/**
	 * sends an abort event to the machine. Any ongoning work will be
	 * interrupted. Might leave the machine in an inconsistent state .
	 */
	void abort();

	/**
	 * 
	 * @return a string array containing the fully qualified names of all
	 *         classes that have been loaded in the target VM.
	 */
	String[] getLoadedClasses();

	/**
	 * 
	 * Installs a listener to obtain objects send with HelperLib.sendMessage.
	 * Actually just a convenience method for
	 * addClientListener(NotificationConstants.MESSAGE,...) that simplifies the
	 * installation of a listener for MESSAGE messages.
	 * 
	 * @param jrTraceMessageListener
	 *            the listener to install
	 */
	void addMessageListener(JRTraceMessageListener jrTraceMessageListener);

	/**
	 * Removes the given message listener
	 * 
	 * @param jrTraceMessageListener
	 *            the listenre to remove
	 */
	void removeMessageListener(JRTraceMessageListener jrTraceMessageListener);

	/**
	 * Performs a detailed analysis on the target machine to check if the
	 * currently installed JRTrace classes inject any code in the designated
	 * method.
	 * <p>
	 * 
	 * </p>
	 * 
	 * @param className
	 *            the fully qualified classname of the target class
	 * @return
	 */
	InjectStatus analyzeInjectionStatus(String className);

	/**
	 * By default, message are sent from the target to the development machine
	 * without any validation. Since messages are sent using JMX messages, some
	 * messages might be dropped under high load.
	 * 
	 * This can be avoided by requesting blocking communication: the target will
	 * request an acknowledgement from the development machine and further send
	 * requests will block the target machine until the acknowledgement arrives.
	 * 
	 * This is potentially dangerous for the target JVM, because the thread that
	 * sends the message will be blocked and not execute further.
	 * 
	 * @param n
	 *            0: non-blocking communication, n>5: blocking communication.
	 */
	void setAcknowledgementMode(int n);

}
