/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import de.schenk.enginex.helper.EngineXMethodMetadata;
import de.schenk.enginex.helper.EngineXNameUtil;
import de.schenk.enginex.helper.NotificationUtil;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.helperagent.FieldList.FieldEntry;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.ReflectionUtil;
import de.schenk.objectweb.asm.Handle;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.commons.AdviceAdapter;
import de.schenk.objectweb.asm.commons.Method;

public class EngineXMethodVisitor extends AdviceAdapter {

	private EngineXMethodMetadata injectedMethod;
	private String descriptor;
	private boolean targetMethodStatic = false; // fixme: target injectedMethod
												// may be
	private Type targetReturnType;
	private Type[] injectionMethodArgumentTypes;
	private Type injectionMethodReturnTypes;
	private EngineXClassVisitor classVisitor;
	private Object targetMethodName;
	private String injectionMethodDescriptor;
	private Type[] targetArguments;

	// static

	public EngineXMethodVisitor(EngineXClassVisitor fieldList,
			boolean isStatic, int access, String name, String desc,
			MethodVisitor visitMethod, EngineXMethodMetadata method) {

		super(Opcodes.ASM5, visitMethod, access, name, desc);
		this.targetMethodName = name;
		this.classVisitor = fieldList;
		this.descriptor = desc;
		Method targetMethod = new Method("dontcare", desc);
		this.targetReturnType = targetMethod.getReturnType();
		this.targetArguments = targetMethod.getArgumentTypes();

		this.injectedMethod = method;
		this.targetMethodStatic = isStatic;

		String targetDescriptor = injectedMethod.getDescriptor();
		Method enginexMethod = new Method(injectedMethod.getMethodName(),
				targetDescriptor);
		injectionMethodArgumentTypes = enginexMethod.getArgumentTypes();
		injectionMethodReturnTypes = enginexMethod.getReturnType();
		injectionMethodDescriptor = enginexMethod.getDescriptor();
		if (injectedMethod.getInjectLocation() != XLocation.EXIT) {
			if (!injectionMethodReturnTypes.equals(Type.VOID_TYPE)) {
				fatal(String
						.format("The injected method has a non-empty return type. This is only allowed for methods injected in location XLocation.EXIT"));

			}

		} else {
			if (!injectionMethodReturnTypes.equals(Type.VOID_TYPE)) {
				if (targetReturnType.getSort() != injectionMethodReturnTypes
						.getSort()) {
					fatal(String
							.format("Return type of injected method doesn't match the type %s of the target method %s in class %s.",
									targetReturnType.getClassName(),
									targetMethodName, EngineXNameUtil
											.getExternalName(classVisitor
													.getClassName())));

				}
			}

		}

	}

	@Override
	protected void onMethodEnter() {
		if (injectedMethod.getInjectLocation() == XLocation.ENTRY)
			injectEngineXCall();

		super.onMethodEnter();
	}

	@Override
	protected void onMethodExit(int opcode) {
		if (injectedMethod.getInjectLocation() == XLocation.EXIT
				&& opcode == targetReturnType.getOpcode(Opcodes.IRETURN)) {
			injectEngineXCall();
		}
		super.onMethodExit(opcode);
	}

	private void injectEngineXCall() {

		for (int i = 0; i < injectionMethodArgumentTypes.length; i++) {
			prepareCallArgument(i);
		}
		createVirtualDynamicInvoke();
	}

	private void prepareCallArgument(int i) {

		Object injectionObject = injectedMethod.getInjection(i);

		if (injectionObject != null) {
			if (injectionObject instanceof Integer) {
				Integer injectionSource = (Integer) injectionObject;

				prepareCallerArgumentArgument(i, injectionSource);
			} else {
				String injectionSource = (String) injectionObject;

				prepareFieldInjectedArgument(i, injectionSource);
			}
		} else

			mv.visitInsn(getNullOperand(injectionMethodArgumentTypes[i]));
	}

	private void prepareCallerArgumentArgument(int pos,
			Integer callerArgumentPosition) {
		Type argument = injectionMethodArgumentTypes[pos];
		if (callerArgumentPosition == -1) {
			if (pos != 0)
				fatal("@XReturn is only allowed on the first parameter.");
			if (injectedMethod.getInjectLocation() != XLocation.EXIT)
				fatal("@XReturn is only allowed on inject location XLocation.EXIT");
			if (injectionMethodReturnTypes.equals(Type.VOID_TYPE)) {
				if (targetReturnType.getSize() == 1) {
					mv.visitInsn(Opcodes.DUP);
				} else {
					mv.visitInsn(Opcodes.DUP2);
				}
			}
		} else {
			int localVarIndex = getLocalVariablePosition(callerArgumentPosition);

			int code = argument.getOpcode(Opcodes.ILOAD);

			if (localVarIndex > 0) /* not XThis */
			{
				if ((targetArguments[localVarIndex - 1]).getSort() != (argument
						.getSort())) {
					fatal(String
							.format("Argument Type mismatch: target method parameter %d has type %s on method %s in class %s but injected into type %s",
									localVarIndex,
									targetArguments[localVarIndex - 1]
											.toString(), targetMethodName,
									EngineXNameUtil
											.getExternalName(classVisitor
													.getClassName()), argument
											.getClassName()));
				}
			} else {
				if (argument.getSort() != Type.OBJECT) {
					fatal(String
							.format("Argument Type mismatch: Tries to inject this in non-object parameter of type %s in method %s in class %s ",
									argument.getClassName(), targetMethodName,
									EngineXNameUtil
											.getExternalName(classVisitor
													.getClassName())));
				}
			}

			mv.visitIntInsn(code, localVarIndex);
		}
	}

	private void prepareFieldInjectedArgument(int pos, String injectionSource) {
		FieldEntry field = classVisitor.getFieldEntry(injectionSource);
		// if (field == null) {
		// throw new RuntimeException("The field " + injectionSource
		// + " could not be found in this class.");
		// }

		if (field != null) {
			int opCode = GETSTATIC;
			String desc = field.getDescriptor();

			if (!field.isStatic()) {
				opCode = GETFIELD;
				mv.visitIntInsn(ALOAD, 0);
			}
			mv.visitFieldInsn(opCode, classVisitor.getClassName(),
					injectionSource, desc);
		} else {

			if (!targetMethodStatic) {
				mv.visitVarInsn(ALOAD, 0);
			} else {
				mv.visitInsn(ACONST_NULL);
			}
			String className = classVisitor.getClassName();
			mv.visitLdcInsn(Type.getType("L" + className + ";"));

			mv.visitLdcInsn(injectionSource);

			mv.visitMethodInsn(
					INVOKESTATIC,
					Type.getType(ReflectionUtil.class).getInternalName(),
					"getPrivateField",
					"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;",
					false);

			castToTargetArgumentType(pos);
		}

	}

	private void castToTargetArgumentType(int pos) {
		Type targetType = injectionMethodArgumentTypes[pos];
		switch (targetType.getSort()) {
		case Type.INT:
			castToPrimitive("java/lang/Integer", "intValue",
					targetType.getDescriptor());
			break;
		case Type.BOOLEAN:
			castToPrimitive("java/lang/Boolean", "booleanValue",
					targetType.getDescriptor());
			break;
		case Type.LONG:
			castToPrimitive("java/lang/Long", "longValue",
					targetType.getDescriptor());
			break;
		case Type.BYTE:
			castToPrimitive("java/lang/Byte", "byteValue",
					targetType.getDescriptor());
			break;
		case Type.SHORT:
			castToPrimitive("java/lang/Short", "shortValue",
					targetType.getDescriptor());
			break;
		case Type.CHAR:
			castToPrimitive("java/lang/Character", "charValue",
					targetType.getDescriptor());
			break;
		case Type.FLOAT:
			castToPrimitive("java/lang/Float", "floatValue",
					targetType.getDescriptor());
			break;
		case Type.DOUBLE:
			castToPrimitive("java/lang/Double", "doubleValue",
					targetType.getDescriptor());
			break;
		case Type.ARRAY:
			mv.visitTypeInsn(CHECKCAST, targetType.getInternalName());
			break;
		case Type.OBJECT:
			mv.visitTypeInsn(CHECKCAST, targetType.getInternalName());
			break;

		}

	}

	private void castToPrimitive(String objecttype, String conversionMethod,
			String descriptor) {
		mv.visitTypeInsn(CHECKCAST, objecttype);
		mv.visitMethodInsn(INVOKEVIRTUAL, objecttype, conversionMethod, "()"
				+ descriptor, false);
	}

	private void createVirtualDynamicInvoke() {
		MethodType mt = MethodType.methodType(CallSite.class,
				MethodHandles.Lookup.class, String.class, MethodType.class,
				String.class, String.class, String.class);
		Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC,
				"de/schenk/enginex/helper/DynamicBinder", "bindEngineXMethods",
				mt.toMethodDescriptorString());
		JRLog.debug("Instrumentations is including call to class: "
				+ injectedMethod.getClassMetadata().getExternalClassName()
				+ " method: " + injectedMethod.getMethodName());
		mv.visitInvokeDynamicInsn(injectedMethod.getMethodName(),
				injectedMethod.getDescriptor(), bootstrap, injectedMethod
						.getClassMetadata().getExternalClassName(),
				injectedMethod.getMethodName(), injectedMethod.getDescriptor());
	}

	private int getLocalVariablePosition(Integer injectionSource) {
		int pos = 0;
		Method m = new Method("doesntmatter", descriptor);

		if (!targetMethodStatic) {
			pos++;
			if (injectionSource == 0)
				return 0;
		} else {
			if (injectionSource == 0) {
				String msg = String
						.format("Cannot use @XThis or @XParam(n=0) on static method %s in class %s.",
								targetMethodName, EngineXNameUtil
										.getExternalName(classVisitor
												.getClassName()));

				fatal(msg);
			}
		}
		Type[] targetArgumentTypes = m.getArgumentTypes();
		for (int j = 0; j < injectionSource - 1; j++) {
			if (j >= targetArgumentTypes.length) {
				fatal(String
						.format("There is no argument at position %d on method %s of class %s that can be injected with @Param.",
								injectionSource, targetMethodName,
								EngineXNameUtil.getExternalName(classVisitor
										.getClassName())));
			}
			pos += targetArgumentTypes[j].getSize();

		}

		return pos;
	}

	private void fatal(String msg) {

		NotificationUtil.sendProblemNotification(msg, EngineXNameUtil
				.getExternalName(injectedMethod.getClassMetadata()
						.getClassName()), injectedMethod.getMethodName(),
				injectionMethodDescriptor);
		throw new RuntimeException(msg);
	}

	private int getNullOperand(Type arguments) {
		if (arguments.getSort() == Type.OBJECT
				|| arguments.getSort() == Type.ARRAY)
			return Opcodes.ACONST_NULL;
		if (arguments.equals(Type.BOOLEAN_TYPE)) {
			return Opcodes.ICONST_0; // false
		}
		if (arguments.equals(Type.INT_TYPE)) {
			return Opcodes.ICONST_0;
		}
		if (arguments.equals(Type.LONG_TYPE)) {
			return Opcodes.LCONST_0;
		}
		if (arguments.equals(Type.FLOAT_TYPE)) {
			return Opcodes.FCONST_0;
		}
		if (arguments.equals(Type.DOUBLE_TYPE)) {
			return Opcodes.DCONST_0;
		}

		if (arguments.equals(Type.SHORT_TYPE)) {
			return Opcodes.ICONST_0;
		}
		if (arguments.equals(Type.BYTE_TYPE)) {
			return Opcodes.ICONST_0;
		}
		throw new RuntimeException("Unkown argument type..."
				+ arguments.toString());
	}
}
