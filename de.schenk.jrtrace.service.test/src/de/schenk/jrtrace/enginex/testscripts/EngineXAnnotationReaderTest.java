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

import de.schenk.enginex.helper.EngineXMetadata;
import de.schenk.enginex.helper.EngineXMethodMetadata;
import de.schenk.enginex.helper.Injection;
import de.schenk.enginex.helper.Injection.InjectionType;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.enginex.testclasses.Script1;
import de.schenk.jrtrace.enginex.testclasses.Script2;
import de.schenk.jrtrace.enginex.testclasses.Script3;
import de.schenk.jrtrace.enginex.testclasses.TestClass1;
import de.schenk.jrtrace.enginex.testclasses.TestClass2;
import de.schenk.jrtrace.enginex.testclasses.TestClass3;
import de.schenk.jrtrace.helperagent.EngineXAnnotationReader;
import de.schenk.jrtrace.service.test.utils.JavaUtil;

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
	public void testprivatemethod() throws Exception {
		classBytes = new JavaUtil().getClassBytes(Script1.class);
		EngineXAnnotationReader annoReader = new EngineXAnnotationReader();
		EngineXMetadata metadata = annoReader.getMetaInformation(classBytes);
		assertTrue(metadata.mayMatch(TestClass1.class));

	}

	   @Test
	    public void readAnnotationsTestOfInvokationAndFields() throws Exception {
	        classBytes = new JavaUtil().getClassBytes(Script3.class);
	        EngineXAnnotationReader annoReader = new EngineXAnnotationReader();
	        EngineXMetadata metadata = annoReader.getMetaInformation(classBytes);
	        Set<String> excludedClasses=metadata.getExcludedClasses();
	        assertTrue( excludedClasses.contains("abc.*"));
	        assertTrue( excludedClasses.contains("def.*"));
	        
	        EngineXMethodMetadata theMethod2 = metadata.getMethod("method2");
            assertNotNull(theMethod2);
            assertEquals("a.b.C",theMethod2.getFieldAccessClass());
            assertEquals("field",theMethod2.getFieldAccessName());
            
	        
	        
	        assertNotNull(metadata);
	        EngineXMethodMetadata theMethod = metadata.getMethod("method");
	        assertNotNull(theMethod);
	        assertEquals(XLocation.BEFORE_INVOCATION,theMethod.getInjectLocation());
	        assertEquals("invokedMethod",theMethod.getInvokedMethodName());
	        assertEquals("a.b.C",theMethod.getInvokedMethodClass());
	        assertEquals(-1,theMethod.getInjection(0).getN());
            assertEquals(InjectionType.INVOKE_PARAMETER,theMethod.getInjection(0).getType());
	        assertEquals(3,theMethod.getInjection(1).getN());
	        assertEquals(InjectionType.INVOKE_PARAMETER,theMethod.getInjection(1).getType());
	        assertEquals("field",theMethod.getInjection(2).getFieldname());
	        assertEquals(InjectionType.FIELD,theMethod.getInjection(2).getType());
	           assertEquals(0,theMethod.getInjection(3).getN());
	            assertEquals(InjectionType.INVOKE_PARAMETER,theMethod.getInjection(3).getType());
	   }
	@Test
	public void readAnnotationsTest() throws Exception {
		classBytes = new JavaUtil().getClassBytes(Script1.class);
		EngineXAnnotationReader annoReader = new EngineXAnnotationReader();
		EngineXMetadata metadata = annoReader.getMetaInformation(classBytes);
		assertNotNull(metadata);
		assertTrue(metadata.hasXClassAnnotation());

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

				Map<Integer, Injection> injections = method.getInjections();
				assertEquals(1, injections.size());
				assertEquals(-1, injections.get(0).getN());
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

				Map<Integer, Injection> injections = method.getInjections();
				assertEquals(2, injections.size());
				assertEquals(0, injections.get(0).getN());
				assertNull(method.getInjection(2));
				assertEquals(0, method.getInjection(0).getN());
				assertEquals(1, method.getInjection(1).getN());
				assertEquals(XLocation.ENTRY, method.getInjectLocation());
			} else if ("(Ljava/lang/Object;)I".equals(method.getDescriptor())) {
				assertNotNull(method.getArgumentList());
				assertEquals(0, method.getArgumentList().size());
				Injection par = method.getInjections().get(0);
				assertEquals(par.getFieldname(), "aField");

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
		assertFalse(metadata.hasXClassAnnotation());

	}

}
