/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarByteUtil {

	/**
	 * Just return the contents of the file as bytes
	 * 
	 * @param jarFile
	 * @return
	 */
	static public byte[] getFileBytes(File jarFile) {
		try {
			byte[] jarBytes = Files.readAllBytes(Paths.get(jarFile.toURI()));
			return jarBytes;
		} catch (IOException e) {
			throw new RuntimeException("Error when reading the file "
					+ jarFile.getAbsolutePath());
		}
	}

	/**
	 * 
	 * @param jarFileBytes
	 * @return a byte[][] array, byte[0] containing the byte[] of the first
	 *         class, byte[1] of the second class, ....
	 */
	static public byte[][] convertJarToClassByteArray(byte[] jarFileBytes) {
		List<byte[]> mdlist = null;

		JarInputStream jar = null;

		try {
			mdlist = new ArrayList<byte[]>();
			jar = new JarInputStream(new ByteArrayInputStream(jarFileBytes));

			while (true) {
				JarEntry jarentry = jar.getNextJarEntry();
				if (jarentry == null)
					break;
				String name = jarentry.getName();
				if (!jarentry.isDirectory() && name.endsWith(".class")) {

					InputStream in = new BufferedInputStream(jar);
					int jarentrysize = (int) jarentry.getSize();

					ByteArrayOutputStream out = new ByteArrayOutputStream(
							jarentrysize > 0 ? jarentrysize : 1000000);
					byte[] buffer = new byte[2048];
					while (true) {
						int bytes = in.read(buffer);
						if (bytes <= 0)
							break;
						out.write(buffer, 0, bytes);
					}
					mdlist.add(out.toByteArray());

				}
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (jar != null)
				try {
					jar.close();
				} catch (IOException e) {
					// do nothing more.
				}

		}
		byte[][] classByteArray = new byte[mdlist.size()][];
		for (int i = 0; i < mdlist.size(); i++) {
			classByteArray[i] = mdlist.get(i);
		}
		return classByteArray;
	}

	public static byte[][] convertJarToClassByteArray(File jarFile) {
		return convertJarToClassByteArray(getFileBytes(jarFile));
	}
}
