/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helper;

public class BuiltInExcludes {

	/**
	 * All classes that start with the following strings are to be excluded
	 * because they can never be instrumented.
	 */
	private static final String[] excludedPrefixes = { "java.lang.Byte",
			"java.lang.Class", "java.lang.ClassLoader", "java.lang.Integer",
			"java.lang.Math", "java.lang.Object", "java.lang.String",
			"java.lang.System", "java.lang.ref.Reference",
			"java.lang.ref.ReferenceQueue", "java.util.Arrays",
			"java.util.HashMap", "java.util.Objects", "de.schenk.jrtrace.help",
			"de.schenk.objectweb", "java.lang.invoke",
			"java.util.concurrent.ConcurrentHashMap", "java.lang.reflect",
			"java.util.ArrayList", "jdk.internal", "sun.invoke", "sun.reflect",
			"java.util.regex.Pattern" };

	/**
	 * 
	 * @param classname
	 *            a non-null fully qualified classname
	 * @return true, if the class cannot be converted by JRTrace due to system
	 *         restrictions.
	 */
	public boolean isBuiltInExclude(String classname) {
		for (String prefix : excludedPrefixes) {
			if (classname.startsWith(prefix))
				return true;
		}
		return false;
	}

}
