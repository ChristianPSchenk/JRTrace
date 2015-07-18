/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public class SerializationUtil {
	/**
	 * 
	 * @param arguments
	 *            byte[] containing a serialized Java Object
	 * @return the deserialized Object
	 * 
	 * @throws RuntimeException
	 *             if there is any problem during deserialization
	 */
	static public Object deserialize(byte[] arguments,
			final ClassLoader classLoader) {
		Object x = null;
		;
		try {
			if (arguments != null) {
				InputStream is = new ByteArrayInputStream(arguments);
				ObjectInputStream os = new ObjectInputStream(is) {
					protected Class<?> resolveClass(ObjectStreamClass desc)
							throws IOException, ClassNotFoundException {
						try {
							return super.resolveClass(desc);
						} catch (ClassNotFoundException e) {
							if (classLoader != null)

							{
								String className = desc.getName();
								return classLoader.loadClass(className);
							} else
								throw e;

						}

					}
				};

				x = (Object) os.readObject();
			}
		} catch (IOException | ClassNotFoundException e) {

			throw new RuntimeException(e);
		}
		return x;
	}

	/**
	 * Convenience method to serialize an Object to a byte[]
	 * 
	 * @param object
	 *            the object to serialize
	 * @return a byte[] array containing the serialized object
	 * 
	 * @throws RuntimeException
	 *             on problem (e.g. if the object cannot be serialized)
	 */
	static public byte[] serialize(final Object object) {
		byte[] bytes = null;
		ByteArrayOutputStream theBytes = null;
		try {

			theBytes = new ByteArrayOutputStream();
			ObjectOutputStream os = new ObjectOutputStream(theBytes);

			os.writeObject(object);

		} catch (IOException e) {

			throw new RuntimeException(e);
		}
		bytes = theBytes.toByteArray();
		return bytes;
	}
}
