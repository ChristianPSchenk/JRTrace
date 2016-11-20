/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib.tests;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import de.schenk.jrtrace.helperlib.InspectUtil;

public class InspectUtilTest extends TestCase {

	public enum SampleEnum {
		Value1, Value2;
	}

	static public class SampleClass {
		private String fstring = "string";
		private int fint = 1;
		private byte fbyte = 5;
		private double fdouble = 2;
		private char fchar = 'a';
		private float ffloat = 3;
		private long flong = 4;
		private boolean fbool = true;
		private String[] farray = new String[] { "array", "b", null, "c" };
		private static String STATICSTRING = "staticstring";
		private SampleEnum fenum = SampleEnum.Value1;

	}

	interface TheInterface {
		public void doit();
	}

	public class BaseClass implements TheInterface {
		public String fsub = "sub";

		public void doit() {
		};
	}

	public class MiniClass extends BaseClass {
		public int fint = 5;

		public String toString() {
			return "miniclass.toString()";
		}
	}

	public void testListsBaseClassfields() {
		MiniClass s = new MiniClass();
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(s);
		System.out.println(result);
		assertTrue(result.contains("fsub"));

	}

	public void testShortClass() {
		MiniClass s = new MiniClass();
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(s);
		System.out.println(result);
		assertFalse(result.substring(0, result.length() - 2).contains("\n"));
		assertFalse(result.contains("this$0="));
	}

	public void testArray() {
		SampleClass s = new SampleClass();
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(s.farray);
		System.out.println(result);
		assertTrue(result.contains("\"array\""));
	}

	class BigArrayClass {
		byte[] array = new byte[2000];
	}

	/*
	 * big arrays exceed with small fields should be formatted as groups: [0,0,0
	 * 0,0,0 0,0,0]
	 */
	public void testBigArrayFormat() {
		BigArrayClass s = new BigArrayClass();
		InspectUtil ui = new InspectUtil();

		String result = ui.inspect(s, 5, null, null, false);
		int crcnt = 0;
		for (int i = 0; i < result.length(); i++) {
			if (result.charAt(i) == '\n')
				crcnt++;
		}
		assertTrue(crcnt > 1 && crcnt < 100);
	}

	public void testExcludeRefs() {
		SampleClass s = new SampleClass();
		InspectUtil ui = new InspectUtil();
		List<String> x = new ArrayList<String>();
		x.add("fint");
		String result = ui.inspect(s, 5, null, x, false);

		System.out.println(result);
		// I want to have the class name and all fields in the output
		assertTrue(result.contains("SampleClass"));
		assertFalse(result.contains("fint=1"));

	}

	public void testInspectEnum() {
		SampleClass s = new SampleClass();
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(s);
		System.out.println(result);
		assertTrue(result.contains("fenum=Value1"));
	}

	public void testInspect() {
		SampleClass s = new SampleClass();
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(s);
		System.out.println(result);
		// I want to have the class name and all fields in the output
		assertTrue(result.contains("SampleClass"));
		assertTrue(result.contains("fbool=true"));
		assertTrue(result.contains("fint=1"));
		assertTrue(result.contains("fdouble=2"));
		assertTrue(result.contains("flong=4"));
		assertTrue(result.contains("fbyte=5"));
		assertTrue(result.contains("fchar='a'"));
		assertTrue(result.contains("fstring=\"string\""));
		assertTrue(result.contains("ffloat=3"));
		assertTrue(result.contains("\"array\""));

		assertFalse(result.contains("STATICSTRING"));

		result = ui.inspect(s, 3, null, null, true);
		System.out.println(result);
		assertTrue(result.contains("STATICSTRING"));

	}

	public void testShortLongContent() {
		Object[] a = new Object[2];
		SampleClass s = new SampleClass();
		s.fstring = "kjdhfaksjhfskjfhsakjfhsakfhskdfhskfhskdfhskjfhskfhsdkjfhsdkjfhhskjfhskfhslkfhsddkjfhskjfhskjlf";
		;
		a[1] = s;

		a[0] = new MiniClass();
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(a, 6);
		System.out.println(result);
		// proper indentation of "big" class
		assertTrue(result.contains("\n InspectUtilTest$SampleClass"));
	}

	public class MainClass {
		SubClass sub;
	};

	public class SubClass {
		MainClass main;
	};

	public void testBackRef() {
		MainClass m = new MainClass();
		SubClass s = new SubClass();
		m.sub = s;
		s.main = m;
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(m);
		System.out.println(result);
		assertTrue(result.contains("<parent>"));
	}

	public class Recurse {
		public Recurse recurse;
		public String text;
	}

	public void testDepth() {
		Recurse r1 = new Recurse();
		Recurse r2 = new Recurse();
		r1.recurse = r2;
		Recurse r3 = new Recurse();
		r2.recurse = r3;
		Recurse r4 = new Recurse();
		r3.recurse = r4;
		r4.text = "du!";
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(r1, 3);
		System.out.println(result);
		assertFalse(result.contains("du!"));
		String result2 = ui.inspect(r1, 4);
		System.out.println(result2);
		assertTrue(result2.contains("du!"));

	}

	public void testCollection() {
		ArrayList<Integer> x = new ArrayList<Integer>();
		x.add(5);

		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(x);
		System.out.println(result);
		assertTrue(result.contains("5"));
	}

	public void testToStringClasses() {
		MiniClass mc = new MiniClass();

		InspectUtil ui = new InspectUtil();
		List<String> excludedStrings = new ArrayList<String>();
		excludedStrings.add("MiniClass");
		String result = ui.inspect(mc, 3, excludedStrings);
		System.out.println(result);
		assertTrue(result.contains("miniclass.toString()"));
	}

	public void testToStringClaseForBaseClass() {
		MiniClass mc = new MiniClass();

		InspectUtil ui = new InspectUtil();
		List<String> excludedStrings = new ArrayList<String>();
		excludedStrings.add("BaseClass");
		String result = ui.inspect(mc, 3, excludedStrings);
		System.out.println(result);
		assertTrue(result.contains("miniclass.toString()"));
	}

	public void testToStringClaseForInterfaces() {
		MiniClass mc = new MiniClass();

		InspectUtil ui = new InspectUtil();
		List<String> excludedStrings = new ArrayList<String>();
		excludedStrings.add("TheInterface");
		String result = ui.inspect(mc, 3, excludedStrings);
		System.out.println(result);
		assertTrue(result.contains("miniclass.toString()"));
	}

	public void testMap() {
		HashMap<String, Integer> x = new HashMap<String, Integer>();
		x.put("a", 5);
		x.put("b", 7);

		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(x);
		System.out.println(result);
		assertTrue(result.contains("5"));
	}

	public void testEmptyMap() {
		Map<?, ?> map = Collections.emptyMap();
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(map);
		System.out.println(result);
		// assertTrue(result.contains(""));
	}

	public void testCharArray() {
		char[] ca = new char[] { 'a', 'b' };
		InspectUtil ui = new InspectUtil();
		String result = ui.inspect(ca);
		System.out.println(result);
		assertTrue(result.contains("'a'"));
	}
	
	public String detailFormatter(String o)
	{
	  return ((String)o).toUpperCase();
	}

	// TODO: not nice that the test needs to know it's bundle... better way to
	// get the test data required.
	public void testDetailFormatter() throws Exception {
		SampleClass s = new SampleClass();
		
		InspectUtil ui = new InspectUtil(this);
		Map<String, String> formatter = new HashMap<String, String>();
		formatter.put("fstring", "detailFormatter");
		String result = ui.inspect(s, 3, null, null, false, formatter);
		assertTrue(result.contains("STRING"));
	}

}
