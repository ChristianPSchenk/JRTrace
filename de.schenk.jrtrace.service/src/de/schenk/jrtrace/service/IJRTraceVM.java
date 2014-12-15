/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import java.io.File;
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
	 * detaches from the virtual machine. Reconnection is not supported
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
	 * @param jar
	 *            the path to the jar file
	 * @return true on success
	 */
	public boolean installJar(String jar);

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
	 * Run a groovy script on the target machine
	 * 
	 * @param osString
	 *            the absolute path of the file that holds the script
	 * @param string
	 *            the name of a class which will be used to get the classloader
	 *            for the script
	 */
	void runGroovy(String osString, String string);

	void runJava(File jarFile, String theClassLoader, String className,
			String methodName);

	/**
	 * Install the class or all classes from the jar file (depends on parameter)
	 * 
	 * @param fileForClass
	 *            a class file or a jar file
	 */
	void installEngineXClass(String fileForClass);

	void clearEngineX();

	boolean attach();

	void addClientListener(String notifyStdout,
			NotificationListener streamReceiver);

	void removeClientListener(String notifyStderr,
			NotificationListener errorstreamReceiver);

}
