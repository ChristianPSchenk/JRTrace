/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import de.schenk.jrtrace.jdk.init.utils.BundleFilesUtil;

public class JarLocator {

	

	static public String getHelperLibJar() {
	
			return BundleFilesUtil.getFile("de.schenk.jrtrace.helperlib", "/lib/HelperLib.jar");
		
	}

	

	static public String getJRTraceHelperAgent() {

	
			return BundleFilesUtil.getFile("de.schenk.jrtrace.helperagent",
					"/lib/HelperAgent.jar");
	
	}

	public static String getHelperLibSource()  {
		return BundleFilesUtil.getFile("de.schenk.jrtrace.helperlib", "/src");
	}

	public static String getJRTraceHelperAgentSource()
			 {
		return BundleFilesUtil.getFile("de.schenk.jrtrace.helperagent", "src");
	}
}
