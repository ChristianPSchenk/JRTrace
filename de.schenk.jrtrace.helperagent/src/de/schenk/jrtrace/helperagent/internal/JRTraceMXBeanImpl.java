package de.schenk.jrtrace.helperagent.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarFile;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.enginex.helper.INotificationSender;
import de.schenk.jrtrace.helperagent.AgentMain;
import de.schenk.jrtrace.helperagent.JRTraceMXBean;
import de.schenk.jrtrace.helperlib.JRLog;

public class JRTraceMXBeanImpl extends NotificationBroadcasterSupport implements
		JRTraceMXBean, INotificationSender {

	private AgentMain agent;

	public JRTraceMXBeanImpl(AgentMain agent) {
		this.agent = agent;
	}

	@Override
	public boolean connect() {

		agent.connect();
		return true;
	}

	@Override
	public boolean stop(boolean disconnectOnly) {
		agent.stop(disconnectOnly);
		return true;
	}

	@Override
	public boolean disconnect() {
		agent.disconnect();
		return true;

	}

	@Override
	public void addToBootClassPath(byte[] jarFile) {
		try {
			File temp = File
					.createTempFile(
							String.format("bootclasspath%s", System.nanoTime()),
							".jar");
			temp.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(temp);

			fos.write(jarFile);
			fos.close();

			agent.appendToBootstrapClassLoaderSearch(new JarFile(temp));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setEnvironmentVariable(String key, String value) {

		System.setProperty(key, value);

	}

	@Override
	public void installEngineXClass(byte[][] classByteArray) {
		new InstallEngineXCommand().installEngineX(classByteArray);

	}

	@Override
	public void runJava(String pathToJar, String referenceClassName,
			String mainClass, String mainMethod) {
		new RunJavaCommand().runJava(pathToJar, referenceClassName, mainClass,
				mainMethod);

	}

	@Override
	public void sendMessage(Notification notification) {
		this.sendNotification(notification);

	}

	@Override
	public void setLogLevel(int level) {
		JRLog.setLogLevel(level);

	}

	@Override
	public void clearEngineX() {
		EngineXHelper.clearEngineX();

	}

	@Override
	public void abort() {
		EngineXHelper.abort();

	}
}
