/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.ClassWriter;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.ClassWriterForClassLoader;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;
import de.schenk.objectweb.asm.addons.ExtendedCheckClassAdapter;

public class JRTraceOneClassTransformer {

	final private byte[] classBytes;
	final private Class<?> classObject;
	final private String className;
	final private ClassLoader classLoader;

	private Class<?> superClass;
	private Class<?>[] interfaces;
	private InjectStatus status;

	public JRTraceOneClassTransformer(ClassLoader classLoader,
			String className, Class<?> classObject, byte[] classBytes) {
		this.classLoader = classLoader;
		this.className = className;
		this.classObject = classObject;
		this.classBytes = classBytes;
	}

	private byte[] applyEngineXMethods(JRTraceClassMetadata metadata,
			byte[] classBytes, InjectStatus classInjectStatus) {
		ClassReader classReader = new ClassReader(classBytes);

		Type[] theInterfaceTypes = new Type[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			theInterfaceTypes[i] = Type.getType(interfaces[i]);
		}
		CommonSuperClassUtil superClassUtil = new CommonSuperClassUtil(
				classLoader, className, Type.getType(superClass)
						.getInternalName(), theInterfaceTypes);
		ClassWriter classWriter = new ClassWriterForClassLoader(classReader,
				superClassUtil, ClassWriter.COMPUTE_FRAMES);

		ClassVisitor visitor = classWriter;

		JRTraceClassVisitor classVisitor = new JRTraceClassVisitor(
				superClassUtil, visitor, Opcodes.ASM5, metadata);
		classVisitor.setStatus(classInjectStatus);
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
								className, result));
			}
		}

		return classWriter.toByteArray();

	}

	public byte[] doTransform() {

		byte[] transformedBytes = classBytes;
		boolean transformed = false;
		Collection<JRTraceClassMetadata> allEngineXClasses = null;

		synchronized (JRTraceHelper.lock) {
			allEngineXClasses = JRTraceHelper.getEngineXClasses();

		}
		String cname = className == null ? null : Type.getType(
				"L" + className + ";").getClassName();

		InjectStatus classInjectStatus = null;
		if (!BuiltInExcludes.isExcludedClassName(cname, status))

			for (JRTraceClassMetadata entry : allEngineXClasses) {
				try {

					if (status != null) {
						classInjectStatus = new InjectStatus(
								StatusEntityType.JRTRACE_CLASS);
						classInjectStatus.setEntityName(entry
								.getExternalClassName());
						status.addChildStatus(classInjectStatus);
					}

					lazyInitSuperClassAndInterfaces();
					if (entry.mayMatchClassHierarchy(cname, superClass,
							interfaces, classInjectStatus)) {
						JRLog.verbose("Applying rules to class:" + className);

						byte[] returnBytes = applyEngineXMethods(entry,
								transformedBytes, classInjectStatus);
						if (returnBytes != null) {
							transformed = true;
							transformedBytes = returnBytes;

						}
					}

				} catch (Throwable e) {
					if (classInjectStatus != null) {
						classInjectStatus
								.setMessage(InjectStatus.MSG_EXCEPTION);
						classInjectStatus.removeChildStatus();
						classInjectStatus
								.setInjected(StatusState.DOESNT_INJECT);
					}
					JRLog.error("Skipped applying jrtrace class "
							+ entry.getClassName() + " to class " + className
							+ " due to runtime exception");
					e.printStackTrace();
					return null;
				}

			}

		if (transformed) {

			JRTraceHelper.setTransformed(className, classLoader);
		} else {
			if (status != null) {
				status.setInjected(StatusState.DOESNT_INJECT);

			}
		}

		
		return transformed ? transformedBytes : null;
	}

	private void lazyInitSuperClassAndInterfaces() {
		if (superClass == null) {

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

			if (superClass == null) {
				JRLog.error(String
						.format("Superclass/Interfaces cannot be calculated  for class: "
								+ className));
			}
		}

	

	
	}

	/**
	 * 
	 * @param stat
	 *            the status to use to report results of the injection
	 */
	public void setStatus(InjectStatus stat) {
		this.status = stat;

	}
}
