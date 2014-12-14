package de.schenk.jrtrace.helperagent;

public interface JRTraceMXBean {

	public void connect(int port);

	public void disconnect();

	public void installEngineXClass(String classOrJarLocation);
}
