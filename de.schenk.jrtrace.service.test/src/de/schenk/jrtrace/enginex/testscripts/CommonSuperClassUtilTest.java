package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.*;

import org.eclipse.swt.SWT;
import org.junit.Test;

import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;

public class CommonSuperClassUtilTest {

	@Test
	public void testGetAllSuperClasses()
	{
		ClassLoader ldr = SWT.class.getClassLoader();
		CommonSuperClassUtil util = new CommonSuperClassUtil(ldr,"org/eclipse/swt/graphics/Cursor","org/eclipse/swt/graphics/Resource",new Type[0]);
		String commonSuperClass = util.getCommonSuperClass("org/eclipse/swt/graphics/ImageData", "org/eclipse/swt/graphics/PaletteData");
		assertEquals("java/lang/Object",commonSuperClass);
		
		
	}
}
