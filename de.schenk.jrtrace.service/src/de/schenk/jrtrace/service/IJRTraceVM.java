/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import java.util.Properties;

import javax.management.NotificationListener;

/**
 * 
 * Alles to connect to another JVM and uploads the JRTrace Agent.
 * 
 * Errorhandling: most methods return true/false for success and provide details
 * via getLastException
 *
 */
public interface IJRTraceVM {

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
	 * installs a new jar file into the target machine
	 * 
	 * @param bytes
	 *            the bytes of the jar file
	 * @return true on success
	 */
	public boolean installJar(byte[] bytes);

	/**
	 * The last exception that occured (e.g. after an unsuccessful, return code
	 * false call) The exception will not be cleared after a successful call to
	 * another function.
	 * 
	 * @return The last exception after an error.
	 */
	Exception getLastError();

	String getPID();

	/*
	 * set some system properties in the target
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
	boolean runJava(String theClassLoader, String className, String methodName,
			Object... parameters);

	/**
	 * Install the provide JRTrace classes. Uninstalls all previously installed
	 * classes.
	 * 
	 * @param classByteArray
	 *            an array with the array byte[], one for each JRTrace class to
	 *            be installed, e.g. classByteArray[0] -> the bytes of the first
	 *            class to install
	 */
	boolean installEngineXClass(byte[][] classByteArray);

	/**
	 * set the agents log level. Levels defined in JRLog constants.
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
	void addClientListener(String notifyId, NotificationListener streamReceiver);

	/**
	 * Add a listener that will be informed on connection loss.
	 * 
	 * @param r
	 */
	void addFailListener(Runnable r);

	void removeClientListener(String notifyId,
			NotificationListener errorstreamReceiver);

	/**
	 * sends an abort event to the machine. Any ongoning work will be
	 * interrupted. Might leave the machine in an inconsistent state .
	 */
	void abort();

	/**
	 * 
	 * @return a string array containing the fully qualified names of all
	 *         classes.
	 */
	String[] getLoadedClasses();

}
