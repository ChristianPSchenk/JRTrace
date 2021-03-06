/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.helper.FieldList.FieldEntry;
import de.schenk.jrtrace.helper.Injection.InjectionType;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.ReflectionUtil;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
import de.schenk.objectweb.asm.Handle;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.TypeCheckUtil;
import de.schenk.objectweb.asm.commons.AdviceAdapter;
import de.schenk.objectweb.asm.commons.Method;

public class JRTraceMethodVisitor extends AdviceAdapter {

	public static final int NO_VIRTUAL_CALL = 2377;
	public static final int VIRTUAL_CALL = 345345;
	private Map<XLocation, List<JRTraceMethodMetadata>> injectedMethodsMap;
	private String descriptor;
	private boolean targetMethodStatic = false; // fixme: target injectedMethod
	// may be
	private Type targetReturnType;

	private JRTraceClassVisitor classVisitor;
	private String targetMethodName;

	private Type[] targetArguments;

	private int targetMethodAccess;
	/**
	 * If the @XClass is annoted with methodinstance=true, a local variable is
	 * created at the start of each method which will get a new instance of the
	 * JRTrace class. This variable stores the local variable index where this
	 * variable is available.
	 */
	private int localJRTraceInstancePos = -1;
	private InjectStatus status;

	/**
   */
	public JRTraceMethodVisitor(JRTraceClassVisitor fieldList, int access,
			String name, String desc, MethodVisitor visitMethod,
			Map<XLocation, List<JRTraceMethodMetadata>> injectedMethods) {

		super(Opcodes.ASM5, visitMethod, access, name, desc);
		this.targetMethodName = name;
		this.classVisitor = fieldList;
		this.descriptor = desc;
		Method targetMethod = new Method("dontcare", desc);
		this.targetReturnType = targetMethod.getReturnType();
		this.targetArguments = targetMethod.getArgumentTypes();

		this.injectedMethodsMap = injectedMethods;
		boolean isStatic = ((access & Opcodes.ACC_STATIC) != 0) ? true : false;
		this.targetMethodStatic = isStatic;
		this.targetMethodAccess = access;

		for (XLocation loc : XLocation.values())
			for (JRTraceMethodMetadata injectedMethod : injectedMethods
					.get(loc)) {

				Method enginexMethod = new Method(
						injectedMethod.getMethodName(),
						injectedMethod.getDescriptor());

				Type injectionMethodReturnTypes = enginexMethod.getReturnType();

				if (injectedMethod.getInjectLocation() != XLocation.EXIT
						&& injectedMethod.getInjectLocation() != XLocation.REPLACE_INVOCATION
						&& injectedMethod.getInjectLocation() != XLocation.AFTER_INVOCATION
						&& injectedMethod.getInjectLocation() != XLocation.EXCEPTION) {
					if (!injectionMethodReturnTypes.equals(Type.VOID_TYPE)) {
						fatal(injectedMethod,
								String.format("The injected method has a non-empty return type. This is only allowed for methods injected in locations: XLocation.EXIT,REPLACE_INVOCATION,AFTER_INVOCATION and EXCEPTION"));

					}

				} else {
					if (!injectionMethodReturnTypes.equals(Type.VOID_TYPE)
							&& injectedMethod.getInjectLocation() == XLocation.EXIT) {

						if (!TypeCheckUtil.isAssignable(
								injectionMethodReturnTypes, targetReturnType,
								classVisitor.getCommonSuperClassUtil()))

						{
							fatal(injectedMethod,
									String.format(
											"Return type of injected method doesn't match the type %s of the target method %s in class %s.",
											targetReturnType.getClassName(),
											targetMethodName,
											JRTraceNameUtil
													.getExternalName(classVisitor
															.getClassName())));

						}
					}

				}
			}

	}

	@Override
	protected void onMethodEnter() {

		if (classVisitor.getInstantiationPolicy() == InstantiationPolicy.METHOD) {
			createLocalJRTraceInstance();
		}

		for (JRTraceMethodMetadata injectedMethod : injectedMethodsMap
				.get(XLocation.ENTRY)) {
			injectEngineXCall(injectedMethod);
		}

		super.onMethodEnter();
	}

	/**
	 * create a local variable that will hold a new instance of the current
	 * JRTrace class. Used for InstantiationPolicy.METHOD.
	 */
	private void createLocalJRTraceInstance() {
		Type jrtraceType = Type.getType("L"
				+ classVisitor.getClassMetadata().getClassName() + ";");
		localJRTraceInstancePos = newLocal(jrtraceType);
		Type instrumentedType = Type.getType("L" + classVisitor.getClassName()
				+ ";");

		visitLdcInsn(classVisitor.getClassMetadata().getExternalClassName());
		visitIntInsn(SIPUSH, JRTraceHelper.getCurrentClassSetId());
		visitLdcInsn(instrumentedType);
		visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader",
				"()Ljava/lang/ClassLoader;", false);
		visitMethodInsn(
				INVOKESTATIC,
				"de/schenk/jrtrace/helper/JRTraceHelper",
				"createEngineXObject",
				"(Ljava/lang/String;ILjava/lang/ClassLoader;)Ljava/lang/Object;",
				false);
		mv.visitVarInsn(jrtraceType.getOpcode(ISTORE), localJRTraceInstancePos);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		List<JRTraceMethodMetadata> beforeInvokeInjections = getMatchingInjections(
				owner, name, XLocation.BEFORE_INVOCATION);
		List<JRTraceMethodMetadata> replaceInvokes = getMatchingInjections(
				owner, name, XLocation.REPLACE_INVOCATION);
		List<JRTraceMethodMetadata> afterInvokes = getMatchingInjections(owner,
				name, XLocation.AFTER_INVOCATION);
		if (afterInvokes.size() == 0 && beforeInvokeInjections.size() == 0
				&& replaceInvokes.size() == 0) {
			super.visitMethodInsn(opcode, owner, name, desc, itf);
			return;
		}

		boolean invokedMethodStatic = (opcode == Opcodes.INVOKESTATIC);

		int[] localArgPositions = moveCallArgumentsFromStackToLocals(desc,
				owner, invokedMethodStatic);

		for (JRTraceMethodMetadata injectedMethod : beforeInvokeInjections) {
			injectEngineXCall(injectedMethod, localArgPositions, owner, name,
					desc);
		}

		if (replaceInvokes.size() > 1) {
			fatal(replaceInvokes.get(0),
					String.format(
							"Multiple methods of type XLocation.REPLACE_INVOKE match the invokation of method %s of class %s which is called from within the method %s of the class %s",
							name, JRTraceNameUtil.getExternalName(owner),
							targetMethodName, JRTraceNameUtil
									.getExternalName(classVisitor
											.getClassName())));

		} else {
			if (replaceInvokes.size() == 1) {
				JRTraceMethodMetadata injectedMethod = replaceInvokes.get(0);

				injectEngineXCall(injectedMethod, localArgPositions, owner,
						name, desc);
				Method enginexMethod = new Method(
						injectedMethod.getMethodName(),
						injectedMethod.getDescriptor());

				Type injectionMethodReturnTypes = enginexMethod.getReturnType();

				if (!TypeCheckUtil.isAssignable(injectionMethodReturnTypes,
						Type.getReturnType(desc),
						classVisitor.getCommonSuperClassUtil())) {
					String msg = String
							.format("Type mismatch: @XLocation.REPLACE_INVOCATION requires that the return type %s of the injected method %s is assignable to the return type %s of the replaced method invocation to method %s.",
									JRTraceNameUtil
											.getExternalName(injectionMethodReturnTypes
													.getClassName()),
									injectedMethod.getMethodName(),
									JRTraceNameUtil
											.getExternalName(Type
													.getReturnType(desc)
													.getClassName()), name);
					fatal(injectedMethod, msg);

				}

			} else {
				moveCallArgumentsFromLocalsToStack(desc, invokedMethodStatic,
						localArgPositions);
				super.visitMethodInsn(opcode, owner, name, desc, itf);
			}
		}

		for (JRTraceMethodMetadata injectedMethod : injectedMethodsMap
				.get(XLocation.AFTER_INVOCATION)) {
			injectEngineXCall(injectedMethod, localArgPositions, owner, name,
					desc);
		}

	}

	private List<JRTraceMethodMetadata> getMatchingInjections(String owner,
			String name, XLocation location) {
		List<JRTraceMethodMetadata> replaceInvokes = new ArrayList<JRTraceMethodMetadata>();
		for (JRTraceMethodMetadata injectedMethod : injectedMethodsMap
				.get(location)) {
			if (injectedMethod.matchesInvoker(owner, name)) {
				replaceInvokes.add(injectedMethod);
			}

		}
		return replaceInvokes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {

		super.visitFieldInsn(opcode, owner, name, desc);
		for (JRTraceMethodMetadata injectedMethod : injectedMethodsMap
				.get(XLocation.GETFIELD)) {

			if (injectedMethod.matchesField(owner, name)) {
				if (((opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC)))
					injectEngineXCall(injectedMethod);
			}
		}
		for (JRTraceMethodMetadata injectedMethod : injectedMethodsMap
				.get(XLocation.PUTFIELD)) {
			if (injectedMethod.matchesField(owner, name)) {
				if (((opcode == Opcodes.PUTFIELD || opcode == Opcodes.PUTSTATIC)))
					injectEngineXCall(injectedMethod);
			}
		}

	}

	/**
	 * 
	 * restores the stack for the coming method invocation properly based on the
	 * stored local variables
	 * 
	 * @param desc
	 *            invoked method signature
	 * @param owner
	 *            invoked method owner
	 * @param invokedMethodStatic
	 *            if the target is static
	 * @param localArgPositions
	 *            the positions where the local variables hold the stack
	 */
	private void moveCallArgumentsFromLocalsToStack(String desc,
			boolean invokedMethodStatic, int[] localArgPositions) {
		Type[] invokedMethodType = Type.getArgumentTypes(desc);
		if (!invokedMethodStatic) {

			mv.visitVarInsn(ALOAD, localArgPositions[0]);
		}
		for (int i = 0; i < invokedMethodType.length; i++) {
			Type t = invokedMethodType[i];
			mv.visitVarInsn(t.getOpcode(ILOAD), localArgPositions[i + 1]);
		}

	}

	/**
	 * Assumes: stack is prepared for method invocation of a method with
	 * descriptor desc Does: remove all the arguments from the stack, stores
	 * them in new local variables and returns the local variable positions
	 * 
	 * @param desc
	 *            the method descriptor of the method about to be invoked
	 * @param invocationTarget
	 *            the class on which the method is invoked.
	 * @param invokedMethodStatic
	 * @return the positions of the local variables that contain the invoked
	 *         methods parameters. First argument is entry 1. Entry 0 will
	 *         contain the invocation target object for non static methods.
	 */
	private int[] moveCallArgumentsFromStackToLocals(String desc,
			String invocationTarget, boolean invokedMethodStatic) {
		Type[] invokedMethodType = Type.getArgumentTypes(desc);
		int[] localVariablePositions = new int[invokedMethodType.length + 1];
		for (int i = invokedMethodType.length - 1; i >= 0; i--) {
			Type t = invokedMethodType[i];
			int theLocal = newLocal(t);
			localVariablePositions[i + 1] = theLocal;
			mv.visitVarInsn(t.getOpcode(ISTORE), theLocal);
		}
		if (!invokedMethodStatic) {
			int thisPos = newLocal(Type.getType(invocationTarget));
			localVariablePositions[0] = thisPos;
			mv.visitVarInsn(ASTORE, thisPos);
		}
		return localVariablePositions;
	}

	@Override
	protected void onMethodExit(int opcode) {
		for (JRTraceMethodMetadata injectedMethod : injectedMethodsMap
				.get(XLocation.EXIT)) {
			if (opcode == targetReturnType.getOpcode(Opcodes.IRETURN)) {
				injectEngineXCall(injectedMethod);
			}
		}
		for (JRTraceMethodMetadata injectedMethod : injectedMethodsMap
				.get(XLocation.EXCEPTION)) {
			if (opcode == Opcodes.ATHROW) {
				injectEngineXCall(injectedMethod);
			}
		}

		super.onMethodExit(opcode);
	}

	private void injectEngineXCall(JRTraceMethodMetadata injectedMethod) {
		injectEngineXCall(injectedMethod, null, null, null, null);
	}

	/**
	 * 
	 * @param injectedMethod
	 *            the method to be injected
	 * @param invokeArgPositions
	 *            the local variable indexes where the parameters of the
	 *            invokedmethod are stored for invocation instrumentation, else
	 *            for other types.
	 * @param invokeMethodDesc
	 *            the class of the invoked method
	 * @param invokeMethodName
	 *            the signature of the invoked method
	 * @param invokeMethodOwner
	 */
	private void injectEngineXCall(JRTraceMethodMetadata injectedMethod,
			int[] invokeArgPositions, String invokeMethodOwner,
			String invokeMethodName, String invokeMethodDesc) {

		Method enginexMethod = new Method(injectedMethod.getMethodName(),
				injectedMethod.getDescriptor());
		Type[] injectionMethodArgumentTypes = enginexMethod.getArgumentTypes();

		if (classVisitor.getInstantiationPolicy() == InstantiationPolicy.METHOD) {
			if (injectionMethodArgumentTypes.length == 0) {
				mv.visitVarInsn(ALOAD, localJRTraceInstancePos);
			}
		}
		for (int i = 0; i < injectionMethodArgumentTypes.length; i++) {
			prepareCallArgument(injectedMethod, i, invokeArgPositions,
					invokeMethodOwner, invokeMethodName, invokeMethodDesc);
			if (i == 0
					&& classVisitor.getInstantiationPolicy() == InstantiationPolicy.METHOD) {

				mv.visitVarInsn(ALOAD, localJRTraceInstancePos);
				if (injectionMethodArgumentTypes[0].getSize() == 1) {
					visitInsn(Opcodes.DUP_X1);
				} else {
					visitInsn(Opcodes.DUP_X2);
				}
				visitInsn(Opcodes.POP);
			}

		}
		createVirtualDynamicInvoke(injectedMethod);
	}

	private void prepareCallArgument(JRTraceMethodMetadata injectedMethod,
			int i, int[] localArgPositions, String owner, String name,
			String desc) {

		Method enginexMethod = new Method(injectedMethod.getMethodName(),
				injectedMethod.getDescriptor());
		Type[] injectionMethodArgumentTypes = enginexMethod.getArgumentTypes();

		Injection injectionObject = injectedMethod.getInjection(i);
		if (injectionObject == null) {
			visitInsn(getNullOperand(injectionMethodArgumentTypes[i]));
		} else {
			InjectionType iType = injectionObject.getType();
			int injectionSource = injectionObject.getN();

			int localVarIndex = -1;
			Type localVarType = null;
			switch (iType) {
			case METHODNAME:
				StringBuffer buffer = new StringBuffer();
				boolean first = true;
				for (Type arg : targetArguments) {
					if (!first)
						buffer.append(",");
					buffer.append(arg.getClassName());
					first = false;
				}

				visitLdcInsn(String.format("%s %s.%s(%s)", targetReturnType
						.getClassName(), JRTraceNameUtil
						.getExternalName(classVisitor.getClassName()),
						targetMethodName, buffer.toString()));
				break;
			case PARAMETER:
			case EXCEPTION:
				localVarType = targetReturnType;
				if (iType == InjectionType.EXCEPTION) {
					localVarType = Type.getType(Throwable.class);
				} else if (injectionSource != -1) {
					if (injectionSource == 0) {
						localVarIndex = getLocalVariablePosition(
								injectedMethod, injectionSource);
						localVarType = Type.getType("L"
								+ classVisitor.getClassName() + ";");
					} else {
						localVarIndex = getLocalVariablePosition(
								injectedMethod, injectionSource);
						localVarType = targetArguments[injectionSource - 1];
					}
				}
				prepareArgumentAndReturnInjection(injectedMethod, i,
						localVarIndex, localVarType);
				break;
			case FIELD:

				prepareFieldInjectedArgument(injectedMethod, i,
						injectionObject.getFieldname());
				break;

			case INVOKE_PARAMETER:

				if (injectionSource == -1) {
					localVarType = Type.getReturnType(desc);
				} else {
					if (injectionSource == 0) {
						localVarIndex = localArgPositions[0];
						localVarType = Type.getType("L" + owner + ";");
					} else {
						if (injectionSource >= localArgPositions.length) {
							fatal(injectedMethod,
									String.format(
											"The method %s of class %s doesn't have a parameter %d for injection.",
											name, owner, injectionSource));
						}
						localVarIndex = localArgPositions[injectionSource];
						localVarType = (Type.getArgumentTypes(desc))[injectionSource - 1];
					}
				}
				prepareArgumentAndReturnInjection(injectedMethod, i,
						localVarIndex, localVarType);
				break;

			}
		}

	}

	/**
	 * 
	 * @param targetParameter
	 *            the position of the argument of the injected method for which
	 *            this call argument is intended
	 * @param sourceLocalVar
	 *            the local variable index where it can be obtained, -1 for the
	 *            call stack
	 * @param sourceType
	 *            the type of the local variable slot.
	 */
	private void prepareArgumentAndReturnInjection(
			JRTraceMethodMetadata injectedMethod, int targetParameter,
			int sourceLocalVar, Type sourceType) {
		Method enginexMethod = new Method(injectedMethod.getMethodName(),
				injectedMethod.getDescriptor());
		Type[] injectionMethodArgumentTypes = enginexMethod.getArgumentTypes();
		Type injectionMethodReturnTypes = enginexMethod.getReturnType();

		Type targetParameterType = injectionMethodArgumentTypes[targetParameter];
		if (sourceLocalVar == -1) {

			injectReturnOrThrowsInParameter(injectedMethod, targetParameter,
					sourceType, injectionMethodReturnTypes, targetParameterType);
		} else {
			int localVarIndex = sourceLocalVar;

			int code = targetParameterType.getOpcode(Opcodes.ILOAD);

			if (!TypeCheckUtil.isAssignable(sourceType, targetParameterType,
					classVisitor.getCommonSuperClassUtil())) {
				fatal(injectedMethod,
						String.format(
								"Argument Type mismatch:  method parameter of type %s is injected into type %s of method %s in class %s.",
								JRTraceNameUtil.getExternalName(sourceType
										.getClassName()), JRTraceNameUtil
										.getExternalName(targetParameterType
												.getClassName()),
								injectedMethod.getMethodName(), JRTraceNameUtil
										.getExternalName(classVisitor
												.getClassName())));
			}

			visitIntInsn(code, localVarIndex);
		}

	}

	private void injectReturnOrThrowsInParameter(
			JRTraceMethodMetadata injectedMethod, int targetParameter,
			Type sourceType, Type injectionMethodReturnType,
			Type targetParameterType) {
		if (targetParameter != 0)
			fatal(injectedMethod,
					"@XReturn/@XInvokeReturn/@XException is only allowed on the first parameter.");
		if (injectedMethod.getInjection(targetParameter).getType() == InjectionType.EXCEPTION) {
			if (injectedMethod.getInjectLocation() != XLocation.EXCEPTION) {
				fatal(injectedMethod,
						"@Exception is only allowed on inject location @XLocation.EXCEPTION");
			}
		} else if (injectedMethod.getInjectLocation() != XLocation.EXIT
				&& injectedMethod.getInjectLocation() != XLocation.AFTER_INVOCATION)
			fatal(injectedMethod,
					"@XReturn/@XInvokeReturn is only allowed on inject location XLocation.EXIT/XLocation.AFTER_INVOKATION");

		if (!TypeCheckUtil.isAssignable(sourceType, targetParameterType,
				classVisitor.getCommonSuperClassUtil())) {
			fatal(injectedMethod,
					String.format(
							"The type returned/thrown by the method %s is %s and cannot be assigned to the first argument of the method %s which is %s.",
							targetMethodName,
							JRTraceNameUtil.getExternalName(sourceType
									.getClassName()), injectedMethod
									.getMethodName(), JRTraceNameUtil
									.getExternalName(targetParameterType
											.getClassName())));
		}
		if (injectionMethodReturnType.equals(Type.VOID_TYPE)) {
			if (sourceType.getSize() == 1) {
				visitInsn(Opcodes.DUP);
			} else {
				visitInsn(Opcodes.DUP2);
			}

		} else {
			if (!TypeCheckUtil.isAssignable(sourceType,
					injectionMethodReturnType,
					classVisitor.getCommonSuperClassUtil())) {
				fatal(injectedMethod,
						String.format(
								"The type returned /thrown by the method %s is %s and cannot be assigned to the return value of the method %s which is %s.",
								targetMethodName, JRTraceNameUtil
										.getExternalName(sourceType
												.getClassName()),
								injectedMethod.getMethodName(), JRTraceNameUtil
										.getExternalName(targetParameterType
												.getClassName())));
			}
		}
	}

	private void prepareFieldInjectedArgument(
			JRTraceMethodMetadata injectedMethod, int pos,
			String injectionSource) {
		FieldEntry field = classVisitor.getFieldEntry(injectionSource);
		Method enginexMethod = new Method(injectedMethod.getMethodName(),
				injectedMethod.getDescriptor());
		Type[] injectionMethodArgumentTypes = enginexMethod.getArgumentTypes();

		if (field != null) {
			String desc = field.getDescriptor();
			Type fieldType = Type.getType(desc);
			if (!TypeCheckUtil.isAssignable(fieldType,
					injectionMethodArgumentTypes[pos],
					classVisitor.getCommonSuperClassUtil())) {
				fatal(injectedMethod,
						String.format(
								"The type of the field "
										+ injectionSource
										+ " of the targetclass "
										+ JRTraceNameUtil
												.getExternalName(classVisitor
														.getClassName())
										+ " doesn't match the type of the argument %d of the injected method and cannot be injected with XField.",
								pos));
			}

			int opCode = GETSTATIC;

			if (!field.isStatic()) {
				opCode = GETFIELD;
				visitIntInsn(ALOAD, 0);
			}
			super.visitFieldInsn(opCode, classVisitor.getClassName(),
					injectionSource, desc);
		} else {

			if (!targetMethodStatic) {
				visitVarInsn(ALOAD, 0);
			} else {
				visitInsn(ACONST_NULL);
			}
			String className = classVisitor.getClassName();
			visitLdcInsn(Type.getType("L" + className + ";"));

			visitLdcInsn(injectionSource);

			visitMethodInsn(
					INVOKESTATIC,
					Type.getType(ReflectionUtil.class).getInternalName(),
					"getPrivateField",
					"(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;",
					false);

			castToTargetArgumentType(injectedMethod, pos);
		}

	}

	private void castToTargetArgumentType(JRTraceMethodMetadata injectedMethod,
			int pos) {
		Method enginexMethod = new Method(injectedMethod.getMethodName(),
				injectedMethod.getDescriptor());
		Type[] injectionMethodArgumentTypes = enginexMethod.getArgumentTypes();

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
			visitTypeInsn(CHECKCAST, targetType.getInternalName());
			break;
		case Type.OBJECT:
			visitTypeInsn(CHECKCAST, targetType.getInternalName());
			break;

		}

	}

	private void castToPrimitive(String objecttype, String conversionMethod,
			String descriptor) {
		visitTypeInsn(CHECKCAST, objecttype);
		visitMethodInsn(INVOKEVIRTUAL, objecttype, conversionMethod, "()"
				+ descriptor, false);
	}

	private void createVirtualDynamicInvoke(JRTraceMethodMetadata injectedMethod) {
		JRLog.debug("Instrumentations is including call to class: "
				+ injectedMethod.getClassMetadata().getExternalClassName()
				+ " method: " + injectedMethod.getMethodName());

		if (status != null) {
			
			InjectStatus m = status.getChildByEntityName(injectedMethod
					.getMethodName()+"#"+injectedMethod.getDescriptor());
			
			m.setInjected(StatusState.INJECTS);
			m.setMessage("");
			
		}

		String bindingMethodName = "bindEngineXMethods";
		Integer useVirtual=new Integer(NO_VIRTUAL_CALL);
		

		Type methodDescriptorType = Type.getMethodType(injectedMethod
				.getDescriptor());

		if (classVisitor.getInstantiationPolicy() == InstantiationPolicy.METHOD) {
			bindingMethodName = "bindEngineXMethodsToVirtual";
			useVirtual=new Integer(VIRTUAL_CALL);;
			Type[] argumentTypes = methodDescriptorType.getArgumentTypes();
			Type[] newArgumentTypes = new Type[argumentTypes.length + 1];
			newArgumentTypes[0] = Type.getType("L"
					+ classVisitor.getClassMetadata().getClassName() + ";");
			newArgumentTypes[0] = Type.getType("Ljava/lang/Object;");
			for (int i = 0; i < argumentTypes.length; i++) {
				newArgumentTypes[i + 1] = argumentTypes[i];
			}
			methodDescriptorType = Type.getMethodType(
					methodDescriptorType.getReturnType(), newArgumentTypes);

		}

		MethodType mt = MethodType.methodType(CallSite.class,
				MethodHandles.Lookup.class, String.class, MethodType.class,
				(new Object[0].getClass()));
		Handle bootstrap = new Handle(Opcodes.H_INVOKESTATIC,
				"java/lang/invoke/LambdaMetafactory", "altMetafactory",
				mt.toMethodDescriptorString(),false);

		visitInvokeDynamicInsn(String.format("%s%d",
				injectedMethod.getMethodName(), System.nanoTime()),
				methodDescriptorType.getDescriptor(), bootstrap, useVirtual,injectedMethod
						.getClassMetadata().getExternalClassName(),
				JRTraceHelper.getCurrentClassSetId(),
				injectedMethod.getMethodName(), injectedMethod.getDescriptor());

	}

	private int getLocalVariablePosition(JRTraceMethodMetadata injectedMethod,
			Integer injectionSource) {
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
								targetMethodName, JRTraceNameUtil
										.getExternalName(classVisitor
												.getClassName()));

				fatal(injectedMethod, msg);
			}
		}
		Type[] targetArgumentTypes = m.getArgumentTypes();
		if (injectionSource - 1 >= targetArgumentTypes.length) {
			fatal(injectedMethod,
					String.format(
							"There is no argument at position %d on method %s of class %s that can be injected with @Param.",
							injectionSource, targetMethodName, JRTraceNameUtil
									.getExternalName(classVisitor
											.getClassName())));
		}
		for (int j = 0; j < injectionSource - 1; j++) {

			pos += targetArgumentTypes[j].getSize();

		}

		return pos;
	}

	private void fatal(JRTraceMethodMetadata injectedMethod, String msg) {
		Method enginexMethod = new Method(injectedMethod.getMethodName(),
				injectedMethod.getDescriptor());

		String injectionMethodDescriptor = enginexMethod.getDescriptor();
		NotificationUtil.sendProblemNotification(msg, JRTraceNameUtil
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

	public void setStatus(InjectStatus status) {

		if (status != null
				&& status.getEntityType() != StatusEntityType.JRTRACE_CHECKED_METHOD) {
			throw new RuntimeException(
					"status on jrtracemethodvisitor must be of entity type JRTRACE_CHECKED_METHOD");
		}
		this.status = status;

	}

}
