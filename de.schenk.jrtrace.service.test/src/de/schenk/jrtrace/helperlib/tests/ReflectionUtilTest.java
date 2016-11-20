/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.schenk.jrtrace.enginex.testclasses.ReflectionTestData;
import de.schenk.jrtrace.helperlib.ReflectionUtil;

public class ReflectionUtilTest {

	@Test
	public void testInvokeStatic() {
		assertEquals(3, ReflectionUtil.invokeMethod(ReflectionTestData.class,
				"fkt", 1.0, new byte[] { 1, 1 }));
	}

	@Test
	public void testInvokeNonStatic() {
		ReflectionTestData d = new ReflectionTestData();
		assertEquals(4, ReflectionUtil.invokeMethod(d, "fkt2", 2, null));
		assertEquals(4, ReflectionUtil.invokeMethod(d, "fkt2", 2, d));
		assertEquals(0, ReflectionUtil.invokeMethod(d, "fkt2", 2));
	}

}
