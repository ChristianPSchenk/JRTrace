/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.objectweb.asm.addons;

import java.util.List;

import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.tree.analysis.SimpleVerifier;

public class ExtendedSimpleVerifier extends SimpleVerifier {

	private CommonSuperClassUtil superClassUtil;

	@Override
	protected Type getSuperClass(Type t) {
		Type r = null;
		try {
			r = super.getSuperClass(t);
		} catch (RuntimeException e) {
			/* super.getSuperClass may fail if a class is not loadable */
			/* fallback to the superClassUtil */
			String internalName = superClassUtil
					.getSuperAndInterfacesFromBytes(t.getInternalName())
					.getSuperClass();
			if (internalName == null)
				r = null;
			else
				r = Type.getType("L" + internalName + ";");
		}

		return r;
	}

	@Override
	protected boolean isAssignableFrom(Type t, Type u) {

		boolean newerg = TypeCheckUtil.isAssignable(u, t, superClassUtil);

		return newerg;
	}

	@Override
	protected boolean isInterface(Type t) {
		boolean isInterface = TypeCheckUtil.isInterface(t, superClassUtil);

		return isInterface;

	}

	public ExtendedSimpleVerifier(Type objectType, Type superType,
			List<Type> interfaces, boolean b, ClassLoader c) {
		super(objectType, superType, interfaces, b);
		this.setClassLoader(c);
		superClassUtil = new CommonSuperClassUtil(c,
				objectType.getInternalName(),
				superType != null ? superType.getInternalName() : null,
				interfaces.toArray(new Type[interfaces.size()]));

	}

}
