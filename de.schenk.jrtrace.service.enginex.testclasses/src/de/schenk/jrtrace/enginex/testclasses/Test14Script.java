/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XField;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.enginex.testscripts.Test14;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.Test14", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class Test14Script {

	@XMethod(names = { "test14" }, arguments = {})
	public void testinstrumentation(@XField(name = "test14") int m,
			@XField(name = "static_test14") int m2,
			@XField(name = "test14base") int m3,
			@XField(name = "test14base") int m4,
			@XField(name = "test14base_boolean") boolean bool,
			@XField(name = "test14base_long") long longValue,
			@XField(name = "test14base_byte") byte byteValue,
			@XField(name = "test14base_short") short shortValue,
			@XField(name = "test14base_float") float floatValue,
			@XField(name = "test14base_double") double doubleValue,
			@XField(name = "test14base_char") char charValue,
			@XField(name = "test14base_array") String[] stringArray,
			@XField(name = "test14base_object") String aString) {
		boolean a = (m == 1234);
		boolean a2 = (m2 == 1234);
		boolean a3 = (m3 == 1234);
		boolean a4 = (m4 == 1234);

		if (a && a2 && a3 && a4)
			Test14.success = true;

		if (!bool)
			Test14.success = false;
		if (longValue != 1234)
			Test14.success = false;
		if (byteValue != 12)
			Test14.success = false;
		if (shortValue != 12)
			Test14.success = false;
		if (floatValue != 12.0F)
			Test14.success = false;
		if (doubleValue != 12.0)
			Test14.success = false;
		if (charValue != 'x')
			Test14.success = false;
		if (stringArray.length != 1)
			Test14.success = false;
		if (!aString.equals("Object"))
			Test14.success = false;

	}

	public void fkt(int i) {

	}

	@XMethod(names = { "staticTest14" }, arguments = {})
	public void testinstrumentation(@XField(name = "static_test14") int m2,
			@XField(name = "static_test14base") int m3) {

		boolean a2 = (m2 == 1234);
		boolean a3 = (m3 == 1234);

		if (a2 && a3)
			Test14.staticsuccess = true;

	}

}