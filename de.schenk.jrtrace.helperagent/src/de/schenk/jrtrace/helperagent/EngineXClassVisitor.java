/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import de.schenk.enginex.helper.EngineXMetadata;
import de.schenk.enginex.helper.EngineXMethodMetadata;
import de.schenk.jrtrace.helperagent.FieldList.FieldEntry;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.FieldVisitor;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.addons.ClassWriterForClassLoader;
import de.schenk.objectweb.asm.commons.JSRInlinerAdapter;

public class EngineXClassVisitor extends ClassVisitor {

	private EngineXMetadata metadata;
	private FieldList fieldList = new FieldList();
	private String className;
	private Class<?> superClass;
	private ClassWriterForClassLoader classWriter;

	public EngineXClassVisitor(ClassWriterForClassLoader classWriter, int api,
			EngineXMetadata metadata, Class<?> superClass) {
		super(Opcodes.ASM5, classWriter);
		this.classWriter = classWriter;
		this.metadata = metadata;
		this.superClass = superClass;

	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value) {
		fieldList.put(name, access, desc);

		return super.visitField(access, name, desc, signature, value);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.className = name;
		classWriter.setSuperInformation(superName);
		super.visit(Opcodes.V1_7, access, name, signature, superName,
				interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
		for (EngineXMethodMetadata method : metadata.getMethods()) {
			if (method.mayMatch(name, desc)) {
				boolean isStatic = ((access & Opcodes.ACC_STATIC) != 0) ? true
						: false;
				return new JSRInlinerAdapter(new EngineXMethodVisitor(this,
						isStatic, access, name, desc, super.visitMethod(access,
								name, desc, signature, exceptions), method),
						access, name, desc, signature, exceptions);
			}
		}

		return new JSRInlinerAdapter(super.visitMethod(access, name, desc,
				signature, exceptions), access, name, desc, signature,
				exceptions);
	}

	public FieldEntry getFieldEntry(String injectionSource) {
		return fieldList.getFieldEntry(injectionSource);
	}

	public String getClassName() {
		return className;
	}

	public Class<?> getSuperClass() {
		return superClass;
	}

}
