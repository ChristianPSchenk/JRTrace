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
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;
import de.schenk.objectweb.asm.commons.JSRInlinerAdapter;
import de.schenk.objectweb.asm.commons.LocalVariablesSorter;

public class EngineXClassVisitor extends ClassVisitor {

	private EngineXMetadata metadata;
	private FieldList fieldList = new FieldList();
	private String className;

	private CommonSuperClassUtil superClassUtil;

	public EngineXClassVisitor(CommonSuperClassUtil superClassUtil,
			ClassVisitor classWriter, int api, EngineXMetadata metadata) {
		super(Opcodes.ASM5, classWriter);
		this.superClassUtil = superClassUtil;

		this.metadata = metadata;

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
				MethodVisitor methodVisitor = super.visitMethod(access,
                    name, desc, signature, exceptions);
				EngineXMethodVisitor jrtraceVisitor = new EngineXMethodVisitor(this,
                    isStatic, access, name, desc, methodVisitor, method);
				LocalVariablesSorter lvs=new LocalVariablesSorter(access, desc, jrtraceVisitor);
				jrtraceVisitor.setLocalVariableSorter(lvs);
				return new JSRInlinerAdapter(lvs,
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

	public CommonSuperClassUtil getCommonSuperClassUtil() {
		return this.superClassUtil;
	}

}
