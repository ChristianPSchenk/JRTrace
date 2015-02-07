/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.util.ArrayList;
import java.util.List;

import de.schenk.jrtrace.helper.JRTraceClassMetadata;
import de.schenk.jrtrace.helper.JRTraceMethodMetadata;
import de.schenk.jrtrace.helperagent.FieldList.FieldEntry;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.FieldVisitor;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;
import de.schenk.objectweb.asm.commons.JSRInlinerAdapter;
import de.schenk.objectweb.asm.commons.LocalVariablesSorter;

public class JRTraceClassVisitor extends ClassVisitor {

	private JRTraceClassMetadata metadata;
	private FieldList fieldList = new FieldList();
	private String className;

	private CommonSuperClassUtil superClassUtil;

	public JRTraceClassVisitor(CommonSuperClassUtil superClassUtil,
			ClassVisitor classWriter, int api, JRTraceClassMetadata metadata) {
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

		MethodVisitor currentVisitor = super.visitMethod(access, name, desc,
				signature, exceptions);

		List<JRTraceMethodMetadata> matchingMethods = new ArrayList<JRTraceMethodMetadata>();
		for (JRTraceMethodMetadata method : metadata.getMethods()) {
			if (method.mayMatch(name, desc, access)) {

				matchingMethods.add(method);

			}
		}

		if (!matchingMethods.isEmpty()) {

			MethodVisitor oldVisitor = currentVisitor;
			JRTraceMethodVisitor newMethodVisitor = new JRTraceMethodVisitor(
					this, access, name, desc, oldVisitor, matchingMethods);
			LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc,
					newMethodVisitor);
			newMethodVisitor.setLocalVariableSorter(lvs);
			currentVisitor = lvs;
		}

		return new JSRInlinerAdapter(currentVisitor, access, name, desc,
				signature, exceptions);

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