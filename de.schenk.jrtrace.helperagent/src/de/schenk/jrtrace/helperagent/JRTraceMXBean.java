package de.schenk.jrtrace.helperagent;

public interface JRTraceMXBean {

	/**
	 * Establish the connection
	 * 
	 * @return true on success
	 */
	public boolean connect();

	/**
	 * Disconnect. Convenience method equivalent to stop(true);
	 * 
	 * @return true on success
	 */
	public boolean disconnect();

	/**
	 * Install a set of JRTrace classes. This operation will always uninstall
	 * all older JRTrace classes and will reinstall the provided new set of
	 * classes.
	 * 
	 * @param classBytes
	 *            classBytes[0] are the classbytes of the first JRTrace class to
	 *            be installed, etc...
	 */
	public void installEngineXClass(byte[][] classBytes);

	/**
	 * Disconnect and optionally shut down the agent. The main difference to
	 * disconnect is, that no new connection is possible, since the agent will
	 * be shut down
	 * 
	 * @param disconnectOnly
	 *            if true, will only disconnect. If false, will disconnect and
	 *            stop the agent completely.
	 * @return true on success.
	 */
	public boolean stop(boolean disconnectOnly);

	/**
	 * Add a new java systemm variable to the target machine
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 */
	public void setEnvironmentVariable(String key, String value);

	/**
	 * Add a jar to the boot classpath of the target machine.
	 * 
	 * @param jar
	 *            the byte's corresponding to the jar file. The jar file will be
	 *            stored in a temporary folder on the target machine and added
	 *            to the boot classpath.
	 */
	public void addToBootClassPath(byte[] jar);

	/**
	 * Execute a method of a specified JRTrace class .
	 * 
	 * 
	 * @param referenceClassName
	 *            for classloading: the fully qualified name of the class that
	 *            will be used to obtain the classloader. Only relevant if the
	 *            JRTrace class has classloadingpolicy TARGET.
	 * @param theClassName
	 *            the fully qualified name of the JRTrace class that contains
	 *            the method to execute.
	 * @param theMethodName
	 *            the name of the method name to execute
	 * @param serializedParameters
	 *            a byte array that will be deserialized into an Object[] using
	 *            standard java serialization. The parameters will be used for
	 *            invocation. This implies that only parameters types can be
	 *            used that are available on both the target and developer
	 *            machine.
	 * @throws RuntimeException
	 *             on error. Note: error behaviour today is not very
	 *             well-defined
	 */
	public void invokeMethodAsync(String referenceClassName,
			String theClassName, String theMethodName,
			byte[] serializedParameters);

	/**
	 * set the agents log level. Levels defined in JRLog constants.
	 * 
	 * @param level
	 *            see the possible levels in JRLog.
	 */
	public void setLogLevel(int level);

	/**
	 * Uninstall all JRTrace classes.
	 */
	public void clearEngineX();

	/**
	 * long running operations (clear, install) can be aborted using this api.
	 */
	public void abort();

	/**
	 * 
	 * @return an array with the fully qualified names of all classes currently
	 *         loaded.
	 */
	public String[] getLoadedClasses();

	/**
	 * Analyzes whether the specified method in the specified class is
	 * instrumented via JRTrace in the current session.
	 * 
	 * <p>
	 * The returned InjectStatus contains the details about why JRTrace classes
	 * and methods do not match.
	 * </p>
	 * 
	 * @param className
	 * @param methodDescriptor
	 * @return a byte array that is the serialization of an InjectStatus.
	 */
	public byte[] analyzeInjectionStatus(String className,
			String methodDescriptor);

}
