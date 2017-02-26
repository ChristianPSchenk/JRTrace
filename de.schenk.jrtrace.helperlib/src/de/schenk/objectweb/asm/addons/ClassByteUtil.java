/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.objectweb.asm.addons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.schenk.objectweb.asm.Type;

public class ClassByteUtil {

	/**
	 * 
	 * @param theClassName
	 *            an internal class name (a/b/C)
	 * @param classLoader
	 *            the classloader
	 * @return the inputstream for the class, null if not found
	 */
	static InputStream getClassBytesStream(String theClassName,
			ClassLoader classLoader) {
		InputStream stream;
		if (classLoader != null) {
			stream = classLoader
					.getResourceAsStream(convertInternalNameToResourceName(theClassName));
		} else {
			stream = ClassLoader
					.getSystemResourceAsStream(convertInternalNameToResourceName(theClassName));

		}

		return stream;
	}

	static private String convertInternalNameToResourceName(String type1) {
		String result = type1;

		return result + ".class";
	}

	/**
	 * 
	 * @param theClass
	 * @return byte[] with the bytes of the class or null if there is a problem
	 */
	public static byte[] getBytes(Class<?> theClass) {

		InputStream stream = getClassBytesStream(
				Type.getInternalName(theClass), theClass.getClassLoader());

		if (stream == null)
			return null;
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		try {
			int nRead;
			byte[] data = new byte[250000];

			while ((nRead = stream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}

			buffer.flush();
		} catch (IOException e) {
			return null;
		}

		return buffer.toByteArray();
	}

}
