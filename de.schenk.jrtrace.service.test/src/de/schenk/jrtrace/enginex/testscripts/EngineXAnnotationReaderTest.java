/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import de.schenk.jrtrace.enginex.testclasses.Script1;
import de.schenk.jrtrace.enginex.testclasses.Script2;
import de.schenk.jrtrace.enginex.testclasses.TestClass1;
import de.schenk.jrtrace.enginex.testclasses.TestClass2;
import de.schenk.jrtrace.enginex.testclasses.TestClass3;
import de.schenk.jrtrace.helperagent.EngineXAnnotationReader;
import de.schenk.jrtrace.service.test.utils.JavaUtil;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.enginex.helper.EngineXMetadata;
import de.schenk.enginex.helper.EngineXMethodMetadata;

public class EngineXAnnotationReaderTest {

	private byte[] classBytes;

	@Test
	public void testRegexread() throws Exception {
		classBytes = new JavaUtil().getClassBytes(Script2.class);
		EngineXAnnotationReader annoReader = new EngineXAnnotationReader();
		EngineXMetadata metadata = annoReader.getMetaInformation(classBytes);
		assertTrue(metadata.getUseRegEx());
	}

	@Test
	public void testMayMatch() throws Exception {
		classBytes = new JavaUtil().getClassBytes(Script1.class);
		EngineXAnnotationReader annoReader = new EngineXAnnotationReader();
		EngineXMetadata metadata = annoReader.getMetaInformation(classBytes);
		assertTrue(metadata.mayMatch(TestClass1.class));
		assertFalse(metadata.mayMatch(TestClass2.class));
		assertFalse(metadata.mayMatch(TestClass3.class));

	}

	@Test
	public void readAnnotationsTest() throws Exception {
		classBytes = new JavaUtil().getClassBytes(Script1.class);
		EngineXAnnotationReader annoReader = new EngineXAnnotationReader();
		EngineXMetadata metadata = annoReader.getMetaInformation(classBytes);
		assertNotNull(metadata);
		assertTrue(metadata.isValidEngineXClass());

		assertEquals(Script1.class.getName().replace(".", "/"),
				metadata.getClassName());
		assertEquals(Script1.class.getName(), metadata.getExternalClassName());
		List<String> classes = metadata.getClasses();
		assertEquals(2, classes.size());

		assertContains("de/schenk/jrtrace/enginex/testclasses/TestClass1",
				classes);
		assertContains("de/schenk/jrtrace/enginex/testclasses/TestClass3",
				classes);

		assertTrue(metadata.getDerived());
		assertFalse(metadata.getUseRegEx());
		assertEquals(XClassLoaderPolicy.NAMED, metadata.getClassLoaderPolicy());
		assertEquals("a.b.c", metadata.getClassLoaderName());

		List<EngineXMethodMetadata> methods = metadata.getMethods();
		assertEquals(3, methods.size());

		for (EngineXMethodMetadata method : methods) {
			if ("(Ljava/lang/Object;)V".equals(method.getDescriptor())) {
				Set<String> usedFor = method.getTargetMethodNames();
				assertEquals(1, usedFor.size());
				String entry = usedFor.iterator().next();
				assertEquals("doit2", (entry));

				Map<Integer, Object> injections = method.getInjections();
				assertEquals(1, injections.size());
				assertEquals(new Integer(-1), injections.get(0));
				assertEquals(XLocation.EXIT, method.getInjectLocation());
				assertNull(method.getArgumentList());

			} else if ("(Ljava/lang/Object;I)V".equals(method.getDescriptor())) {
				Set<String> usedFor = method.getTargetMethodNames();
				assertEquals(1, usedFor.size());
				String entry = usedFor.iterator().next();
				assertEquals("doit", (entry));

				List<String> argumentList = method.getArgumentList();
				assertEquals(1, argumentList.size());
				assertEquals("java.lang.String", argumentList.get(0));

				Map<Integer, Object> injections = method.getInjections();
				assertEquals(2, injections.size());
				assertEquals(new Integer(0), injections.get(0));
				assertNull(method.getInjection(2));
				assertEquals(new Integer(0), method.getInjection(0));
				assertEquals(new Integer(1), method.getInjection(1));
				assertEquals(XLocation.ENTRY, method.getInjectLocation());
			} else if ("(Ljava/lang/Object;)I".equals(method.getDescriptor())) {
				assertNotNull(method.getArgumentList());
				assertEquals(0, method.getArgumentList().size());
				Object par = method.getInjections().get(0);
				assertEquals(par, "aField");

			} else

				fail();
		}

	}

	private void assertContains(String string, List<String> classes) {
		for (String c : classes) {
			if (c.equals(string))
				return;
		}
		// fail
		assertTrue(classes.toString() + " doesn't contain " + string, false);

	}

	@Test
	public void tryreadAnnotationsOfClassWithoutEngineXAnnotation()
			throws Exception {
		classBytes = new JavaUtil().getClassBytes(this.getClass());
		EngineXAnnotationReader annoReader = new EngineXAnnotationReader();
		EngineXMetadata metadata = annoReader.getMetaInformation(classBytes);
		assertNotNull(metadata);
		assertFalse(metadata.isValidEngineXClass());

	}

}
