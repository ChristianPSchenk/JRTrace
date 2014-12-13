/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import de.schenk.enginex.helper.EngineXNameUtil;
import de.schenk.jrtrace.service.JarLocator;
import de.schenk.jrtrace.service.internal.PortUtil;

public class JavaUtil {

	private static Process javaProcess = null;
	private int killPort;

	public Thread startTestProcessInSameVM() {
		Thread t = new Thread() {
			@Override
			public void run() {
				TestProcess
						.main(new String[] { String.format("%d", killPort) });
			}
		};
		t.start();
		return t;
	}

	/**
	 * launches the java process without any specific agent.
	 * 
	 */
	public void launchJavaProcess() throws IOException, URISyntaxException,
			InterruptedException {

		launchJavaProcess("");
	}

	/**
	 * launches the java test process with an agent listening on a free port
	 * 
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @returns the port with which the agent was started.
	 */
	public int launchJavaProcessWithAgent() throws IOException,
			URISyntaxException, InterruptedException {
		int freePort = PortUtil.getFreePort();
		String agentPath = JarLocator.getJRTraceHelperAgent();
		String bootjarPath = JarLocator.getHelperLibJar();
		launchJavaProcess(String.format("-javaagent:%s=port=%d,bootjar=%s",
				agentPath, freePort, bootjarPath));
		return freePort;
	}

	/**
	 * 
	 * @param runAgent
	 *            if true, passes the agent on the command line already
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public void launchJavaProcess(String parameters) throws IOException,
			URISyntaxException, InterruptedException {
		String javaHome = System.getProperty("java.home");

		String fullPath = getClassPathForClass(TestProcess.class);
		ArrayList<String> commandParameters = new ArrayList<String>();
		commandParameters.add(javaHome + "\\bin\\java.exe");
		commandParameters.add(parameters);
		commandParameters.add("-cp");
		commandParameters.add(fullPath);
		commandParameters.add(TestProcess.class.getName());
		obtainKillPort();
		commandParameters.add(String.format("%d", killPort));

		ProcessBuilder processBuilder = new ProcessBuilder(commandParameters);
		processBuilder.redirectErrorStream(true);
		processBuilder.inheritIO();
		javaProcess = processBuilder.start();

		InputStream inputStream = javaProcess.getInputStream();
		OutputStreamPrinter outputStreamPrinter = new OutputStreamPrinter(
				inputStream);
		outputStreamPrinter.start();

	}

	/**
	 * example: for <anyPackage>.class1 in plug.in will return:
	 * g:\workspace\plug.in\bin works only for non-jar plugins.
	 * 
	 * @param class1
	 *            the class
	 * @return the valid classpath entry to launch java exe for this class
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private String getClassPathForClass(Class<TestProcess> class1)
			throws URISyntaxException, IOException {
		String file = getFileForClass(class1);
		String internalclassname = EngineXNameUtil.getInternalName(class1
				.getName());
		int index = file.indexOf(internalclassname);

		return file.substring(0, index - 1);
	}

	/**
	 * Makes the classfile of the specified class available as file in the
	 * filesystem.
	 * 
	 * @param classname
	 *            the class
	 * @return a valid file system path to the file containing the bytecode
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public String getPathForClass(Class<?> theclass) throws URISyntaxException,
			IOException {
		String fullPath = null;

		fullPath = getFileForClass(theclass);

		int index = fullPath.lastIndexOf("/");
		fullPath = fullPath.substring(0, index);

		return fullPath;
	}

	/**
	 * return a file for the given class in this bundle
	 * 
	 * @param theclass
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public String getFileForClass(Class<?> theclass) throws URISyntaxException,
			IOException {
		return getFileForClass(theclass, "de.schenk.jrtrace.service.test");
	}

	/**
	 * return a file for the given class from the specified bundle. (assuming
	 * standard file layout: class is in bin folder...)
	 * 
	 * @param theclass
	 * @param bundleid
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public String getFileForClass(Class<?> theclass, String bundleid)
			throws URISyntaxException, IOException {
		String fullPath;
		String classname = theclass.getName();
		String qualifiedTestProcessClassFile = classname.replace(".", "/")
				+ ".class";
		Path path = new Path("/bin/" + qualifiedTestProcessClassFile);

		// TODO: not nice to have the hard coded bundle name
		Bundle bundle = Platform.getBundle(bundleid);
		URL fileURL = FileLocator.find(bundle, path, null);

		fullPath = FileLocator.resolve(fileURL).toURI().toASCIIString()
				.replace("file:/", "");
		return fullPath;
	}

	public void sendKillAndWaitForEnd() throws InterruptedException {

		if (javaProcess != null) {
			while (true) {
				sendKillUDPPacket();

				try {
					javaProcess.exitValue();

					break;
				} catch (IllegalThreadStateException e) {
					// do nothing, try again
				}
				Thread.sleep(10);

			}

		}

	}

	public void sendKillUDPPacket() {

		try {

			DatagramSocket clientSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[1];
			DatagramPacket sendPacket = new DatagramPacket(sendData,
					sendData.length, IPAddress, killPort);
			clientSocket.send(sendPacket);
			clientSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public String getQualifiedTestClassName() {
		return TestProcess.class.getName();
	}

	public void obtainKillPort() {
		killPort = PortUtil.getFreePort();

	}

	public byte[] getClassBytes(Class<?> c) throws IOException {

		String className = c.getName();
		String classAsPath = className.replace('.', '/') + ".class";
		InputStream stream = c.getClassLoader()
				.getResourceAsStream(classAsPath);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];

		while ((nRead = stream.read(data, 0, data.length)) != -1) {
			bos.write(data, 0, nRead);
		}

		bos.flush();

		return bos.toByteArray();
	}

}
