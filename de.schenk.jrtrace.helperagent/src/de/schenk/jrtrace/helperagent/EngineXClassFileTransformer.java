/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.FileOutputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.List;

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.enginex.helper.EngineXMetadata;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.ClassWriter;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.ClassWriterForClassLoader;

public class EngineXClassFileTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader classLoader, String className,
			Class<?> classObject, ProtectionDomain protectionDomain,
			byte[] classBytes) throws IllegalClassFormatException {

		byte[] oldBytes = classBytes;
		try {

			boolean transformed = false;
			Collection<EngineXMetadata> allEngineXClasses = EngineXHelper
					.getEngineXClasses();

			Class<?> superClass = null;
			Class<?>[] interfaces = null;
			if (classObject != null) {
				superClass = classObject.getSuperclass();
				interfaces = classObject.getInterfaces();
			} else {
				SuperClassExtractor extractor = new SuperClassExtractor(
						classLoader, classBytes);
				extractor.analyze();
				superClass = extractor.getSuperclass();
				interfaces = extractor.getInterfaces().toArray(new Class<?>[0]);
			}

			for (EngineXMetadata entry : allEngineXClasses) {
				List<String> classes = entry.getClasses();
				for (String targetclass : classes) {
					String cname = className == null ? null : Type.getType(
							"L" + className + ";").getClassName();
					if (entry.mayMatchClassHierarchy(cname, superClass,
							interfaces)) {
						JRLog.verbose("Applying rules to class:" + className);

						byte[] returnBytes = applyEngineXClasses(classLoader,
								entry, targetclass, classBytes, superClass);
						if (returnBytes != null) {
							transformed = true;
							classBytes = returnBytes;
						}
					}

				}

			}
			if (transformed) {
				EngineXHelper.setTransformed(className, classLoader);
			}
			if (JRLog.getLogLevel() == JRLog.DEBUG) {
				if (className != null) {
					String tmpdir = System.getProperty("java.io.tmpdir");
					JRLog.debug("Writing classbytes of " + className
							+ " to tempdir " + tmpdir);
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

					JRLog.debug("Done writing classbytes for " + className);
				}

			}
			return transformed ? classBytes : null;
		} catch (Throwable e) {
			JRLog.error("Skipping Transformation of " + className
					+ " due to runtime exception");
			e.printStackTrace();
			return null;
		}
	}

	private byte[] applyEngineXClasses(ClassLoader classLoader,
			EngineXMetadata entry, String targetclass, byte[] classBytes,
			Class<?> superClass) {

		return applyEngineXMethods(classLoader, entry, classBytes, superClass);

	}

	private byte[] applyEngineXMethods(ClassLoader classLoader,
			EngineXMetadata metadata, byte[] classBytes, Class<?> superClass) {
		ClassReader classReader = new ClassReader(classBytes);

		ClassWriter classWriter = new ClassWriterForClassLoader(classLoader,
				classReader, ClassWriter.COMPUTE_FRAMES);
		ClassVisitor classVisitor = new EngineXClassVisitor(classWriter,
				Opcodes.ASM5, metadata, superClass);
		classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
		return classWriter.toByteArray();

	}

}
