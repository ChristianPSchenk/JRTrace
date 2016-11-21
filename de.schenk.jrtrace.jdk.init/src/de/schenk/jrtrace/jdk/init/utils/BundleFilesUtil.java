package de.schenk.jrtrace.jdk.init.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Utlity to get a file from a bundle.
 * @author Christian P. Schenk
 *
 */
public class BundleFilesUtil {

	/**
	 * returns the absolute path of a file inside a bundle and returns it.
	 * 
	 * @param bundleId 
	 * @param bundleRelativeResourcePath
	 * @return null if the file doesn't exist.
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	static public String getFile(String bundleId, String bundleRelativeResourcePath)
			 {

		Bundle bundle = Platform.getBundle(bundleId);
		Path path = new Path(bundleRelativeResourcePath);
		URL fileURL = FileLocator.find(bundle, path, null);
		if(fileURL==null) return null;

	
		try {
			return new File(FileLocator.resolve(fileURL).toURI()).getAbsolutePath();
		} catch (URISyntaxException | IOException e) {
					throw new RuntimeException(e);
		}
	
	}
	
	
}
