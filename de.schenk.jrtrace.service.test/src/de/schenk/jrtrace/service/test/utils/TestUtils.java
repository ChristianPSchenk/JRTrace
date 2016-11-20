/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test.utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

public class TestUtils {

	public static File getResource(String path) {
		// TODO: not nice to have the hard coded bundle name
		URL bundleurl = Platform.getBundle("de.schenk.jrtrace.service.test")
				.getEntry(path);

		try {
			URL fileURL = FileLocator.toFileURL(bundleurl);
			return new File(fileURL.toURI());
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

}
