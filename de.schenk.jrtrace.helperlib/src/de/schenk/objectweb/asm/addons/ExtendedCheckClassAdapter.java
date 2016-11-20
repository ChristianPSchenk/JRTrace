/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.objectweb.asm.addons;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.tree.ClassNode;
import de.schenk.objectweb.asm.tree.MethodNode;
import de.schenk.objectweb.asm.tree.analysis.Analyzer;
import de.schenk.objectweb.asm.tree.analysis.BasicValue;
import de.schenk.objectweb.asm.tree.analysis.SimpleVerifier;
import de.schenk.objectweb.asm.util.CheckClassAdapter;

public class ExtendedCheckClassAdapter extends CheckClassAdapter {

	protected ExtendedCheckClassAdapter(int api, ClassVisitor cv,
			boolean checkDataFlow) {
		super(api, cv, checkDataFlow);

	}

	/**
	 * Checks a given class.
	 * 
	 * @param cr
	 *            a <code>ClassReader</code> that contains bytecode for the
	 *            analysis.
	 * @param loader
	 *            a <code>ClassLoader</code> which will be used to load
	 *            referenced classes. This is useful if you are verifiying
	 *            multiple interdependent classes.
	 * @param dump
	 *            true if bytecode should be printed out not only when errors
	 *            are found.
	 * @param pw
	 *            write where results going to be printed
	 */
	public static void verify(final ClassReader cr, final ClassLoader loader,
			final PrintWriter pw) {
		ClassNode cn = new ClassNode();
		cr.accept(new CheckClassAdapter(cn, false), ClassReader.SKIP_DEBUG);

		Type syperType = cn.superName == null ? null : Type
				.getObjectType(cn.superName);
		List<MethodNode> methods = cn.methods;

		List<Type> interfaces = new ArrayList<Type>();
		for (Iterator<String> i = cn.interfaces.iterator(); i.hasNext();) {
			interfaces.add(Type.getObjectType(i.next()));
		}

		for (int i = 0; i < methods.size(); ++i) {
			MethodNode method = methods.get(i);

			SimpleVerifier verifier = new ExtendedSimpleVerifier(
					Type.getObjectType(cn.name), syperType, interfaces,
					(cn.access & Opcodes.ACC_INTERFACE) != 0, loader);
			Analyzer<BasicValue> a = new Analyzer<BasicValue>(verifier);

			try {

				a.analyze(cn.name, method);

			} catch (Exception e) {
				pw.println(String.format("Problem in Method %s (%s)",
						method.name, method.desc));
				e.printStackTrace(pw);
			}
			printAnalyzerResult(method, a, pw);

		}
		pw.flush();
	}

}
