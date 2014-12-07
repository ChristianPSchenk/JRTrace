/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.enginex.helper;

public class EngineXNameUtil {

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
	 *            the external class name (a.b.Class)
	 * @return the internal class name (e.g. a/b/Class)
	 */
	public static String getInternalName(String value) {
		return ((String) value).replaceAll("\\.", "/");
	}

}
