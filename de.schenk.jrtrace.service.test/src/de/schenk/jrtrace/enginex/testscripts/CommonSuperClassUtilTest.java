package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.*;

import org.eclipse.equinox.internal.p2.engine.ProfilePreferences;
import org.eclipse.swt.SWT;
import org.junit.Test;

import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;

public class CommonSuperClassUtilTest {

	/** two classes, both cannot be loaded by the classloader. */
	@Test
	public void testGetAllSuperClasses()
	{
		ClassLoader ldr = this.getClass().getClassLoader();
		CommonSuperClassUtil util = new CommonSuperClassUtil(ldr,"does/really/not/matter","does/not/matter",new Type[0]);
		String commonSuperClass = util.getCommonSuperClass("de/schenk/jrtrace/enginex/testscripts/NotLoadedType", "de/schenk/jrtrace/enginex/testscripts/NotLoadedType2");
		assertEquals("java/lang/Object",commonSuperClass);
		
		
	}
	

	@Test
	public void testGetAllSuperClasses2()
	{
		ClassLoader ldr = SWT.class.getClassLoader();
		CommonSuperClassUtil util = new CommonSuperClassUtil(ldr,"org/eclipse/swt/graphics/Cursor","org/eclipse/swt/graphics/Resource",new Type[0]);
		String commonSuperClass = util.getCommonSuperClass("org/eclipse/swt/graphics/ImageData", "org/eclipse/swt/graphics/PaletteData");
		assertEquals("java/lang/Object",commonSuperClass);
		
		
	}
}
