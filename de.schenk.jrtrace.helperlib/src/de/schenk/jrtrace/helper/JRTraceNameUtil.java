/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helper;

public class JRTraceNameUtil {

	/**
	 * 
	 * @param theClassName
	 *            internal class name representation ( a/b/Class )
	 * @return the external versoin ( a.b.Class)
	 */
	public static String getExternalName(String theClassName) {
		return theClassName.replace('/', '.');
	}

	/**
	 * Converter:
	 * 
	 * @param value
	 *            the external class name (a.b.Class) as obtained e.g. by Class<?>.getCanonicalName()
	 * @return the internal class name (e.g. a/b/Class) 
	 */
	public static String getInternalName(String value) {
		return ((String) value).replaceAll("\\.", "/");
	}

}
