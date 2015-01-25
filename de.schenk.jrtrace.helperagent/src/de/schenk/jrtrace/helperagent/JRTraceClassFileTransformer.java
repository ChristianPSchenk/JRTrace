/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;

import de.schenk.jrtrace.helper.JRTraceHelper;
import de.schenk.jrtrace.helper.JRTraceClassMetadata;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.ClassWriter;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.ClassWriterForClassLoader;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;
import de.schenk.objectweb.asm.addons.ExtendedCheckClassAdapter;

public class JRTraceClassFileTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader classLoader, String className,
			Class<?> classObject, ProtectionDomain protectionDomain,
			byte[] classBytes) throws IllegalClassFormatException {

		byte[] oldBytes = classBytes;
		boolean transformed = false;
		Collection<JRTraceClassMetadata> allEngineXClasses = JRTraceHelper
				.getEngineXClasses();

		Class<?> superClass = null;
		Class<?>[] interfaces = null;
		if (classObject != null) {

			if (classObject.isInterface()) {
				superClass = Object.class;
			} else

			{
				superClass = classObject.getSuperclass();
			}

			interfaces = classObject.getInterfaces();
		} else {
			SuperClassExtractor extractor = new SuperClassExtractor(
					classLoader, classBytes);
			extractor.analyze();
			superClass = extractor.getSuperclass();
			interfaces = extractor.getInterfaces().toArray(new Class<?>[0]);
		}

		for (JRTraceClassMetadata entry : allEngineXClasses) {
			try {
			  String cname = className == null ? null : Type.getType(
                  "L" + className + ";").getClassName();
			    if(entry.excludesClass(cname))
			    {
			      continue;
			    }
			  
				List<String> classes = entry.getClasses();
				for (String targetclass : classes) {
					

					if (superClass == null) {
						JRLog.error(String.format("Superclass null for class: "
								+ cname));
					}
					if (entry.mayMatchClassHierarchy(cname, superClass,
							interfaces)) {
						JRLog.verbose("Applying rules to class:" + className);

						byte[] returnBytes = applyEngineXClasses(classLoader,
								className, entry, targetclass, classBytes,
								superClass, interfaces);
						if (returnBytes != null) {
							transformed = true;
							classBytes = returnBytes;
							break;
						}
					}

				}

			} catch (Throwable e) {
				JRLog.error("Skipped applying jrtrace class "
						+ entry.getClassName() + " to class " + className
						+ " due to runtime exception");
				e.printStackTrace();
				return null;
			}
		}
		if (transformed) {
			JRTraceHelper.setTransformed(className, classLoader);
		}
		try {
			if (JRLog.getLogLevel() == JRLog.DEBUG && transformed) {
				if (className != null) {
					String tmpdir = System.getProperty("java.io.tmpdir");

					FileOutputStream fileOutputStream = new FileOutputStream(
							tmpdir + "\\" + className.replace('/', '_')
									+ "before.class");
					fileOutputStream.write(oldBytes);
					fileOutputStream.close();
					FileOutputStream fileOutputStream2 = new FileOutputStream(
							tmpdir + "\\" + className.replace('/', '_')
									+ "after.class");
					fileOutputStream2.write(classBytes);
					fileOutputStream2.close();

					JRLog.debug("Writing bytes before/after transformation of class "
							+ className + " to directory " + tmpdir);
				}

			}
		} catch (IOException e) {
			JRLog.error("Error when trying to write transformed classbytes.");
		}
		return transformed ? classBytes : null;

	}

	private byte[] applyEngineXClasses(ClassLoader classLoader,
			String targetClassName, JRTraceClassMetadata entry, String targetclass,
			byte[] classBytes, Class<?> superClass, Class<?>[] interfaces) {

		return applyEngineXMethods(classLoader, targetClassName, entry,
				classBytes, superClass, interfaces);

	}

	private byte[] applyEngineXMethods(ClassLoader classLoader,
			String targetClassName, JRTraceClassMetadata metadata,
			byte[] classBytes, Class<?> superClass, Class<?>[] interfaces) {
		ClassReader classReader = new ClassReader(classBytes);

		Type[] theInterfaceTypes = new Type[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			theInterfaceTypes[i] = Type.getType(interfaces[i]);
		}
		CommonSuperClassUtil superClassUtil = new CommonSuperClassUtil(
				classLoader, targetClassName, Type.getType(superClass)
						.getInternalName(), theInterfaceTypes);
		ClassWriter classWriter = new ClassWriterForClassLoader(classReader,
				superClassUtil, ClassWriter.COMPUTE_FRAMES);

		ClassVisitor visitor = classWriter;

		ClassVisitor classVisitor = new JRTraceClassVisitor(superClassUtil,
				visitor, Opcodes.ASM5, metadata);
		classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

		/**
		 * keeping this permanently disabled since the SimpleVerifier of ASM
		 * still has some gaps
		 * 
		 */
		boolean checkClasses = false;
		if (checkClasses) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ExtendedCheckClassAdapter
					.verify(new ClassReader(classWriter.toByteArray()),
							classLoader, pw);
			String result = sw.getBuffer().toString();
			if (!result.isEmpty()) {

				throw new RuntimeException(
						String.format(
								"Verification failed for transformed bytecode of: %s\n%s",
								targetClassName, result));
			}
		}

		return classWriter.toByteArray();

	}

}
