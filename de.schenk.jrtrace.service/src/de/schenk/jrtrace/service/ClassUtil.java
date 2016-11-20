/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ClassUtil {

	/**
	 * Utility to obtain the bytes that represent a class. Bytes are retrieved
	 * using the class' classloader. If the bytes cannot be found using the
	 * classloader, the method will throw an IOException.
	 * 
	 * Example use:
	 * 
	 * Include the JRTrace classes in your application and use getClassBytes to
	 * obtain the bytes to use for installJRTraceClasses(...).
	 * 
	 * @param clazz
	 * @return the bytes that represent the class
	 * @throws IOException
	 */
	static public byte[] getClassBytes(Class<?> clazz) throws IOException {

		String className = clazz.getName();
		String classAsPath = className.replace('.', '/') + ".class";
		InputStream stream = clazz.getClassLoader().getResourceAsStream(
				classAsPath);
		return getStreamAsBytes(stream);
	}

	public static byte[] getStreamAsBytes(InputStream stream)
			throws IOException {
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
