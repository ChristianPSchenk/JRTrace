package de.schenk.jrtrace.helperagent;

public interface JRTraceMXBean {

	public boolean connect();

	public boolean disconnect();

	public void installEngineXClass(byte[] classOrJarLocation);

	public boolean stop(boolean disconnectOnly);

	public void setEnvironmentVariable(String key, String value);

	public void addToBootClassPath(String jarFile);

	/**
	 * Execute a static method of a specified class from a jarfile in the
	 * context of an arbitrary classloader
	 * 
	 * @param pathToJar
	 *            the jar of the file that contains the class
	 * 
	 * @param referenceClassName
	 *            for classloading: the fully qualified name of the class that
	 *            will be used to obtain the classloader
	 * @param mainClass
	 *            the fully qualified name of the class
	 * @param mainMethod
	 *            the method name to execute from mainClass
	 */
	public void runJava(String pathToJar, String referenceClassName,
			String mainClass, String mainMethod);

	

	/**
	 * set the agents log level. Levels defined in JRLog constants.
	 * 
	 * @param level
	 */
	public void setLogLevel(int level);

	public void clearEngineX();

	public void abort();

}
