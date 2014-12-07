/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class JarLocator {

	static public String getGroovyLibJar() throws URISyntaxException,
			IOException {
		return getFile("de.schenk.jrtrace.helperlib",
				"/groovylib/groovy-all-2.3.6-indy.jar");
	}

	static public String getHelperLibJar() throws URISyntaxException,
			IOException {
		return getFile("de.schenk.jrtrace.helperlib", "/lib/HelperLib.jar");
	}

	static public String getFile(String bundleId, String jarpath)
			throws URISyntaxException, IOException {

		Bundle bundle = Platform.getBundle(bundleId);
		Path path = new Path(jarpath);
		URL fileURL = FileLocator.find(bundle, path, null);

		String pathString = FileLocator.resolve(fileURL).toURI()
				.toASCIIString();
		return pathString.replace("file:/", "");
	}

	static public String getJRTraceHelperAgent() throws URISyntaxException,
			IOException {

		return getFile("de.schenk.jrtrace.helperagent", "/lib/HelperAgent.jar");
	}

	public static String getHelperLibSource() throws URISyntaxException,
			IOException {
		return getFile("de.schenk.jrtrace.helperlib", "/src");
	}

	public static String getJRTraceHelperAgentSource()
			throws URISyntaxException, IOException {
		return getFile("de.schenk.jrtrace.helperagent", "src");
	}
}
