package de.schenk.jrtrace.helperagent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.OpenResourceAction;
import org.junit.Test;

import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;
import de.schenk.objectweb.asm.addons.TypeCheckUtil;

public class TypeCheckUtilTest {

	CommonSuperClassUtil util = new CommonSuperClassUtil(
			TypeCheckUtilTest.class.getClassLoader());

	@Test
	public void testIsInterface() {
		int[] x = new int[5];

		assertFalse(TypeCheckUtil.isInterface(Type.getType(x.getClass()), util));
	}

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

		Object[] oarray = new Object[1];

		String[][] stringArray = new String[5][5];
		Type a = Type.getType((new Object[5]).getClass());
		Type b = Type.getType((new TypeCheckUtilTest[5]).getClass());

		assertFalse(TypeCheckUtil.isAssignable(a, b, util));
		assertTrue(TypeCheckUtil.isAssignable(b, a, util));
		assertTrue(TypeCheckUtil.isAssignable(a, a, util));

		assertTrue(TypeCheckUtil.isAssignable(b, Type.getType(Object.class),
				util));
		assertFalse(TypeCheckUtil.isAssignable(Type.getType(Object.class), b,
				util));

		assertTrue(TypeCheckUtil.isAssignable(
				Type.getType(stringArray.getClass()),
				Type.getType(oarray.getClass()), util));
		assertFalse(TypeCheckUtil.isAssignable(Type.getType(oarray.getClass()),
				Type.getType(stringArray.getClass()), util));
	}

	@Test
	public void testArrays3() {

		Type a = Type.getType((new boolean[3]).getClass());
		Type b = Type.getType((new int[5]).getClass());

		assertFalse(TypeCheckUtil.isAssignable(a, b, util));
		assertFalse(TypeCheckUtil.isAssignable(b, a, util));

		Type a2 = Type.getType(new Object[3].getClass());
		Type b2 = Type.getType(new char[3][3].getClass());

		assertTrue(TypeCheckUtil.isAssignable(b2, a2, util));
		assertFalse(TypeCheckUtil.isAssignable(a2, b2, util));
	}

	@Test
	public void testOpenResourceIAction() {

		Type a = Type.getType(OpenResourceAction.class);
		Type b = Type.getType(IAction.class);
		Type o = Type.getType(Object.class);

		assertFalse(TypeCheckUtil.isAssignable(b, a, util));
		assertTrue(TypeCheckUtil.isAssignable(a, b, util));

		assertTrue(TypeCheckUtil.isAssignable(b, o, util));
		assertFalse(TypeCheckUtil.isAssignable(o, b, util));

	}

	@Test
	public void testAliasManagerAndIResourceChangeListener() {
		// Type a = Type.getType(IRegistryChangeListener.class);
		// Type b = Type.getType(AliasManager.class);
		// assertFalse(TypeCheckUtil.isAssignable(a, b, util));
		// assertTrue(TypeCheckUtil.isAssignable(b, a, util));

	}

}
