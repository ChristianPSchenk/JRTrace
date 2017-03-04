package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.swt.SWT;
import org.junit.Test;

import de.schenk.jrtrace.service.ClassUtil;
import de.schenk.jrtrace.superclasstestdata.C1;
import de.schenk.jrtrace.superclasstestdata.C3;
import de.schenk.jrtrace.superclasstestdata.C5;
import de.schenk.jrtrace.superclasstestdata.C6;
import de.schenk.jrtrace.superclasstestdata.C8;
import de.schenk.jrtrace.superclasstestdata.I1;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;

public class CommonSuperClassUtilTest {

	private static final int NCLASSES = 8;

	/** two classes, both cannot be loaded by the classloader. */
	@Test
	public void testGetAllSuperClasses() {
		ClassLoader ldr = this.getClass().getClassLoader();
		CommonSuperClassUtil util = new CommonSuperClassUtil(ldr, "de/schenk/jrtrace/enginex/testscripts/NotLoadedType",
				"java/lang/Object", new Type[0]);
		String commonSuperClass = util.getCommonSuperClass("de/schenk/jrtrace/enginex/testscripts/NotLoadedType",
				"de/schenk/jrtrace/enginex/testscripts/NotLoadedType2");
		assertEquals("java/lang/Object", commonSuperClass);

		CommonSuperClassUtil util2 = new CommonSuperClassUtil(ldr,
				"de/schenk/jrtrace/enginex/testscripts/NotLoadedType2", "java/lang/Object", new Type[0]);
		String commonSuperClass2 = util2.getCommonSuperClass("de/schenk/jrtrace/enginex/testscripts/NotLoadedType",
				"de/schenk/jrtrace/enginex/testscripts/NotLoadedType2");
		assertEquals("java/lang/Object", commonSuperClass2);

	}

	@Test
	public void testGetAllSuperClasses2() {
		ClassLoader ldr = SWT.class.getClassLoader();
		CommonSuperClassUtil util = new CommonSuperClassUtil(ldr, "org/eclipse/swt/graphics/Cursor",
				"org/eclipse/swt/graphics/Resource", new Type[0]);
		String commonSuperClass = util.getCommonSuperClass("org/eclipse/swt/graphics/ImageData",
				"org/eclipse/swt/graphics/PaletteData");
		assertEquals("java/lang/Object", commonSuperClass);

	}

	public class TestCommonSuperClassLoader extends ClassLoader {

		private ClassLoader moduleClassLoader;
		private List<String> excluded;

		public TestCommonSuperClassLoader(ClassLoader mcl, List<String> resourceAsStreamNotAvailable) {
			super(null);
			this.excluded = resourceAsStreamNotAvailable;
			this.moduleClassLoader = mcl;
		}

		@Override
		protected URL findResource(String name) {
			if (!isResourceNotAccessible(name)) {
				URL delegateRes = moduleClassLoader.getResource(name);
				if (delegateRes != null)
					return delegateRes;
			}
			return super.findResource(name);
		}

		@Override
		protected Enumeration<URL> findResources(String name) throws IOException {
			if (!isResourceNotAccessible(name)) {
				Enumeration<URL> delegateRes = moduleClassLoader.getResources(name);
				if (delegateRes != null)
					return delegateRes;
			}
			return super.findResources(name);
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {

		
			return super.loadClass(name);
		}

		private boolean isResourceNotAccessible(String name) {
			if(excluded==null) return false;
			for(String n:excluded)
			{
				if(name.contains(n))return true;
			}
			return false;

		}

		@Override
		protected Class<?> findClass(String name) throws ClassNotFoundException {
			

			try {
				InputStream stream = moduleClassLoader.getResourceAsStream(name.replaceAll("\\.", "/") + ".class");
				if (stream != null) {
					byte[] bytes = ClassUtil.getStreamAsBytes(stream);
					return defineClass(name, bytes, 0, bytes.length);
				}

			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			return super.findClass(name);

		}

		@Override
		public InputStream getResourceAsStream(String name) {
			if (isResourceNotAccessible(name))
				return null;
			return super.getResourceAsStream(name);

		}
	}

	/**
	 * c5 (impl I1) extends c4 extends c1 extends c2 
	 * </br> c3 extends c1 c6 (impl I1)
	 * extends c4 </br> c7 (impl I1) </br> c8 extends c7
	 */
	@Test
	public void testClassHierarchy() throws Exception{
		CommonSuperClassUtil util = new CommonSuperClassUtil(this.getClass().getClassLoader());
		List<String> allClasses = new ArrayList<>();
		for (int i = 1; i <= NCLASSES; i++)
			allClasses.add(String.format("de/schenk/jrtrace/superclasstestdata/C%d", i));
		C5 c5 = new C5();
		C3 c3 = new C3();
		C6 c6 = new C6();
		C8 c8 = new C8();

		String[][] referenceResult = new String[NCLASSES][NCLASSES];

		for (int i = 0; i < NCLASSES; i++) {
			for (int j = 0; j < NCLASSES; j++) {
				referenceResult[i][j] = util.getCommonSuperClass(allClasses.get(i), allClasses.get(j));
			}
		}
		
		// assert some cases
		assertCorrectSuperClass(allClasses,referenceResult,"C3","C7","Object");
		assertCorrectSuperClass(allClasses,referenceResult,"C5","C6","C4");
		assertCorrectSuperClass(allClasses,referenceResult,"C6","C3","C1");
		assertCorrectSuperClass(allClasses,referenceResult,"C1","C2","C2");
		

		// CSC of A and A is always A
		for (int i = 0; i < NCLASSES; i++) {
			assertEquals(allClasses.get(i), referenceResult[i][i]);
		}
		// CSC of A and B is same as that of B and A
		for (int i = 0; i < NCLASSES; i++) {
			for (int j = 0; j < NCLASSES; j++) {
				assertEquals(referenceResult[i][j], referenceResult[j][i]);
			}
		}
		ClassLoader c = new TestCommonSuperClassLoader(this.getClass().getClassLoader(), null);
		CommonSuperClassUtil su2 = new CommonSuperClassUtil(c);
		

		for (int i = 0; i < NCLASSES; i++) {
			for (int j = 0; j < NCLASSES; j++) {
				String result = su2.getCommonSuperClass(allClasses.get(i), allClasses.get(j));
				
				boolean same = result.equals(referenceResult[i][j]);
				if (!same)
				{
					result = su2.getCommonSuperClass(allClasses.get(i), allClasses.get(j));
				System.out.println(String.format("Problem: %s / %s   > %s  / %s", allClasses.get(i), allClasses.get(j),
						referenceResult[i][j], result));
				}
				assertTrue(same);
			}
		}
		
		ClassLoader cl3 = new TestCommonSuperClassLoader(this.getClass().getClassLoader(), null);
		CommonSuperClassUtil su3 = new CommonSuperClassUtil(cl3);
		

		Class.forName(C3.class.getName(),true,cl3);
		for (int i = 0; i < NCLASSES; i++) {
			for (int j = 0; j < NCLASSES; j++) {
				String result = su3.getCommonSuperClass(allClasses.get(i), allClasses.get(j));
				boolean same = result.equals(referenceResult[i][j]);
				if (!same)
				{
					result = su3.getCommonSuperClass(allClasses.get(i), allClasses.get(j));
				System.out.println(String.format("Problem: %s / %s   > %s  / %s", allClasses.get(i), allClasses.get(j),
						referenceResult[i][j], result));
				}
				assertTrue(same);
			}
		}

		
		// a classloader with no access to c5
		ClassLoader cl4 = new TestCommonSuperClassLoader(this.getClass().getClassLoader(),Arrays.asList(new String[] {"C5"}));
		CommonSuperClassUtil su4 = new CommonSuperClassUtil(cl4);
		
		Class.forName(C1.class.getName(),true,cl4);

		for (int i = 0; i < NCLASSES; i++) {
			for (int j = 0; j < NCLASSES; j++) {
				String result = su4.getCommonSuperClass(allClasses.get(i), allClasses.get(j));
				boolean same = result.equals(referenceResult[i][j]);
				if (!same)
				{
					if(allClasses.get(i).contains("C5")||allClasses.get(j).contains("C5"))
							{
								assertEquals("java/lang/Object", result);
								continue;
							}
					result = su4.getCommonSuperClass(allClasses.get(i), allClasses.get(j));
				System.out.println(String.format("Problem: %s / %s   > %s  / %s", allClasses.get(i), allClasses.get(j),
						referenceResult[i][j], result));
				}
				assertTrue(same);
			}
		}
		
		
	}

	private void assertCorrectSuperClass(List<String> allClasses, String[][] referenceResult, String string,
			String string2, String string3) {
		int i1=findIndex(allClasses,string);
		int i2=findIndex(allClasses,string2);
		assertTrue(referenceResult[i1][i2].contains(string3));
		
	}

	private int findIndex(List<String> allClasses, String string) {
		for(int i=0;i<allClasses.size();i++)
		{
			if(allClasses.get(i).contains(string)) return i;
		}
		throw new RuntimeException();
	}
}
