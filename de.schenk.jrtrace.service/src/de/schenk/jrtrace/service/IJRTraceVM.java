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
	 * 
	 * @param theClassLoader
	 *            if the jrtrace class has classloaderpolicy TARGET: the name of
	 *            the class to use to obtain the classloader
	 * @param className
	 *            the name of the jrtrace class to invoke a static method on
	 * @param methodName
	 *            the name of the static method to invoke
	 * @return true on success, use getLastException on false
	 */
	boolean runJava(String theClassLoader, String className, String methodName);

	/**
	 * Install the class or all classes from the jar file (depends on parameter)
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
	 */
	void setLogLevel(int i);

	boolean clearEngineX();

	boolean attach();

	void addClientListener(String notifyStdout,
			NotificationListener streamReceiver);

	void removeClientListener(String notifyStderr,
			NotificationListener errorstreamReceiver);

	/**
	 * sends an abort event to the machine. Any ongoning work will be
	 * interrupted. Might leave the machine in an inconsistent state .
	 */
	void abort();

}
