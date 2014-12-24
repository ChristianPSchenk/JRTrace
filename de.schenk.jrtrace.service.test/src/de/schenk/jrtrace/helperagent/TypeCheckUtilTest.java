package de.schenk.jrtrace.helperagent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;
import de.schenk.objectweb.asm.addons.TypeCheckUtil;

public class TypeCheckUtilTest {

	CommonSuperClassUtil util = new CommonSuperClassUtil(
			TypeCheckUtilTest.class.getClassLoader());

	@Test
	public void testBasics() {
		Type a = Type.getType(int.class);
		Type b = Type.getType(long.class);

		assertFalse(TypeCheckUtil.isAssignable(b, a, util));

		assertTrue(TypeCheckUtil.isAssignable(a, a, util));

	}

	@Test
	public void testObject() {
		Type a = Type.getType(Object.class);
		Type b = Type.getType(TypeCheckUtilTest.class);

		assertTrue(TypeCheckUtil.isAssignable(b, a, util));
		assertFalse(TypeCheckUtil.isAssignable(a, b, util));

		assertTrue(TypeCheckUtil.isAssignable(a, a, util));
		assertTrue(TypeCheckUtil.isAssignable(b, b, util));

	}

	@Test
	public void testArrays() {
		Type a = Type.getType((new int[5]).getClass());
		Type b = Type.getType((new int[5][5]).getClass());

		assertFalse(TypeCheckUtil.isAssignable(a, b, util));
		assertFalse(TypeCheckUtil.isAssignable(b, a, util));
		assertTrue(TypeCheckUtil.isAssignable(a, a, util));

	}

	@Test
	public void testArrays2() {
		Object[] ac = new Object[5];
		TypeCheckUtilTest[] bc = new TypeCheckUtilTest[5];

		Type a = Type.getType((new Object[5]).getClass());
		Type b = Type.getType((new TypeCheckUtilTest[5]).getClass());

		assertFalse(TypeCheckUtil.isAssignable(a, b, util));
		assertTrue(TypeCheckUtil.isAssignable(b, a, util));

		assertTrue(TypeCheckUtil.isAssignable(a, a, util));

	}

}
