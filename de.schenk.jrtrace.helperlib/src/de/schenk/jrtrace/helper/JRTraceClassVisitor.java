/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.helper.FieldList.FieldEntry;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.FieldVisitor;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.CommonSuperClassUtil;
import de.schenk.objectweb.asm.commons.JSRInlinerAdapter;

public class JRTraceClassVisitor extends ClassVisitor {

	private JRTraceClassMetadata metadata;
	private FieldList fieldList = new FieldList();
	private String className;

	private CommonSuperClassUtil superClassUtil;
	/**
	 * If not null, the results of the instrumentation process are docummented
	 * on the status
	 */
	private InjectStatus status;

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

		int targetVersion = Opcodes.V1_7;
		if ((version & 0xffff) > Opcodes.V1_7)
			targetVersion = version;

		super.visit(targetVersion, access, name, signature, superName,
				interfaces);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {

		MethodVisitor currentVisitor = super.visitMethod(access, name, desc,
				signature, exceptions);

		InjectStatus checkedMethodStatus = null;
		if (status != null) {
			checkedMethodStatus = new InjectStatus(
					StatusEntityType.JRTRACE_CHECKED_METHOD);
			checkedMethodStatus.setInjected(StatusState.DOESNT_INJECT);
			checkedMethodStatus.setEntityName(MethodUtil.getHumanReadableName(
					name, desc));
			Type.getReturnType(desc).getClassName();
			status.addChildStatus(checkedMethodStatus);
		}

		Map<XLocation, List<JRTraceMethodMetadata>> matchingMethods = new HashMap<XLocation, List<JRTraceMethodMetadata>>();
		for (XLocation l : XLocation.values()) {
			matchingMethods.put(l, new ArrayList<JRTraceMethodMetadata>());
		}

		boolean hasMatchingMethod = false;

		/** ACC_ABSTRACT -> ignore */
		if ((access & Opcodes.ACC_ABSTRACT) == 0) {

			InjectStatus childStatus = null;
			for (JRTraceMethodMetadata method : metadata.getMethods()) {
				if (checkedMethodStatus != null) {
					childStatus = new InjectStatus(
							StatusEntityType.JRTRACE_METHOD);
					childStatus.setEntityName(method.getMethodName());
					checkedMethodStatus.addChildStatus(childStatus);
				}

				if (method.mayMatch(name, desc, access, childStatus)) {

					if (status != null)
						childStatus.setInjected(StatusState.DOESNT_INJECT);
					List<JRTraceMethodMetadata> list = matchingMethods
							.get(method.getInjectLocation());
					list.add(method);
					hasMatchingMethod = true;

				}

			}
		} else {
			if (status != null) {
				checkedMethodStatus.setInjected(StatusState.DOESNT_INJECT);
				checkedMethodStatus
						.setMessage(InjectStatus.MSG_METHOD_IS_ABSTRACT);
			}
		}

		if (hasMatchingMethod) {

			MethodVisitor oldVisitor = currentVisitor;
			JRTraceMethodVisitor newMethodVisitor = new JRTraceMethodVisitor(
					this, access, name, desc, oldVisitor, matchingMethods);
			newMethodVisitor.setStatus(checkedMethodStatus);
			currentVisitor = newMethodVisitor;
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

	public InstantiationPolicy getInstantiationPolicy() {

		return metadata.getInstantiationPolicy();
	}

	public JRTraceClassMetadata getClassMetadata() {
		return metadata;
	}

	public void setStatus(InjectStatus classInjectStatus) {
		if (classInjectStatus != null)
			if (classInjectStatus.getEntityType() != StatusEntityType.JRTRACE_CLASS) {
				throw new RuntimeException(
						"classInjectStatus must have entity type JRTRACE_CLASS");
			}
		status = classInjectStatus;

	}

}
