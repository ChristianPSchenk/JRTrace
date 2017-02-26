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


/**
 * 
 * A transformer to apply the bytecode modifications required to inject jrtrace method calls.
 * 
 * @author Christian Schenk
 *
 */
public class JRTraceOneClassTransformer {

	final private byte[] classBytes;
	final private Class<?> classObject;
	private String className;
	final private ClassLoader classLoader;

	private Class<?> superClass;
	private Class<?>[] interfaces;
	private InjectStatus status;

	/**
	 * 
	 * @param classLoader the classloader that loads the class about to be transformed
	 * @param className the name of the class to transform
	 * @param classObject the Class<?> that is being transformed
	 * @param classBytes the bytes of the class to transform
	 * @param stat Output: A status object that will report details on the result of the injection process. 
	 */
	public JRTraceOneClassTransformer(ClassLoader classLoader, String className, Class<?> classObject,
			byte[] classBytes,InjectStatus stat) {
		this.status = stat;
		this.classLoader = classLoader;
		this.className = className;
		this.classObject = classObject;
		this.classBytes = classBytes;
	}

	private byte[] applyEngineXMethods(JRTraceClassMetadata metadata, byte[] classBytes,
			InjectStatus classInjectStatus) {
		ClassReader classReader = new ClassReader(classBytes);

		Type[] theInterfaceTypes = new Type[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			theInterfaceTypes[i] = Type.getType(interfaces[i]);
		}
		CommonSuperClassUtil superClassUtil = new CommonSuperClassUtil(classLoader, className,
				Type.getType(superClass).getInternalName(), theInterfaceTypes);
		ClassWriter classWriter = new ClassWriterForClassLoader(classReader, superClassUtil,
				ClassWriter.COMPUTE_FRAMES);

		ClassVisitor visitor = classWriter;

		JRTraceClassVisitor classVisitor = new JRTraceClassVisitor(superClassUtil, visitor, Opcodes.ASM5, metadata);
		classVisitor.setStatus(classInjectStatus);
		classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);

		
		return classWriter.toByteArray();

	}
	public byte[] doTransform() {

		status.setInjected(StatusState.DOESNT_INJECT);
		byte[] transformedBytes = classBytes;
		
		Collection<JRTraceClassMetadata> allEngineXClasses = null;

		synchronized (JRTraceHelper.lock) {
			allEngineXClasses = JRTraceHelper.getEngineXClasses();

		}
		if (className == null) {
			lazyInitSuperClassAndInterfaces();
		}
		String cname = Type.getType("L" + className + ";").getClassName();

		InjectStatus classInjectStatus = null;
		if (!BuiltInExcludes.isExcludedClassName(cname, status))

			for (JRTraceClassMetadata entry : allEngineXClasses) {
				try {

					if (status != null) {
						classInjectStatus = new InjectStatus(StatusEntityType.JRTRACE_CLASS);
						classInjectStatus.setEntityName(entry.getExternalClassName());
						status.addChildStatus(classInjectStatus);
					}

					lazyInitSuperClassAndInterfaces();
					if (entry.mayMatchClassHierarchy(cname, superClass, interfaces, classInjectStatus)) {
						JRLog.verbose("Applying rules to class:" + className);

						byte[] returnedBytes = null;
						returnedBytes=applyEngineXMethods(entry, transformedBytes, classInjectStatus);
						if(returnedBytes!=null)
						{
							transformedBytes=returnedBytes;
						}
						
					}
				} catch (Throwable e) {
					if (classInjectStatus != null) {
						classInjectStatus.setMessage(InjectStatus.MSG_EXCEPTION);
						classInjectStatus.removeChildStatus();
						classInjectStatus.setInjected(StatusState.DOESNT_INJECT);
					}
					JRLog.error("Skipped applying jrtrace class " + entry.getClassName() + " to class " + className
							+ " due to runtime exception");
					e.printStackTrace();
					return null;
				}

			}

		status.updateStatusFromChildren();
		if (status.getInjectionState()== StatusState.INJECTS) {			
			JRTraceHelper.setTransformed(className, classLoader);			
		} else
		{
			transformedBytes=null;
		}
		return  transformedBytes;
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
				SuperClassExtractor extractor = new SuperClassExtractor(classLoader, classBytes);
				extractor.analyze();
				superClass = extractor.getSuperclass();
				interfaces = extractor.getInterfaces().toArray(new Class<?>[0]);
				if (className == null) {
					className = extractor.getClassname();
				} else {
					if (!className.equals(extractor.getClassname())) {
						String msg = String.format(
								"Inconsistency when transforming class %s. The bytecode says that the name of the class is %s. Using the classname from the Bytecode.",
								className, extractor.getClassname());
						JRLog.error(msg);						
						className=extractor.getClassname();
					}
				}
			}

			if (superClass == null) {
				JRLog.error(String.format("Superclass/Interfaces cannot be calculated  for class: " + className));
			}
		}

	}


	
}
