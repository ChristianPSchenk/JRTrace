/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import de.schenk.enginex.helper.EngineXMethodMetadata;
import de.schenk.enginex.helper.EngineXNameUtil;
import de.schenk.enginex.helper.Injection;
import de.schenk.enginex.helper.Injection.InjectionType;
import de.schenk.enginex.helper.NotificationUtil;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.helperagent.FieldList.FieldEntry;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.ReflectionUtil;
import de.schenk.objectweb.asm.Handle;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;
import de.schenk.objectweb.asm.addons.TypeCheckUtil;
import de.schenk.objectweb.asm.commons.AdviceAdapter;
import de.schenk.objectweb.asm.commons.LocalVariablesSorter;
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
  private LocalVariablesSorter localVariableSorter;

 

  /**
   */
  public EngineXMethodVisitor(EngineXClassVisitor fieldList, boolean isStatic, int access, String name, String desc,
      MethodVisitor visitMethod, EngineXMethodMetadata method) {
    
    super(Opcodes.ASM5, visitMethod , access, name, desc);
    this.targetMethodName = name;
    this.classVisitor = fieldList;
    this.descriptor = desc;
    Method targetMethod = new Method("dontcare", desc);
    this.targetReturnType = targetMethod.getReturnType();
    this.targetArguments = targetMethod.getArgumentTypes();

    this.injectedMethod = method;
    this.targetMethodStatic = isStatic;

    String targetDescriptor = injectedMethod.getDescriptor();
    Method enginexMethod = new Method(injectedMethod.getMethodName(), targetDescriptor);
    injectionMethodArgumentTypes = enginexMethod.getArgumentTypes();
    injectionMethodReturnTypes = enginexMethod.getReturnType();
    injectionMethodDescriptor = enginexMethod.getDescriptor();
    if (injectedMethod.getInjectLocation() != XLocation.EXIT&&injectedMethod.getInjectLocation()!=XLocation.REPLACE_INVOCATION&&injectedMethod.getInjectLocation()!=XLocation.AFTER_INVOCATION) {
      if (!injectionMethodReturnTypes.equals(Type.VOID_TYPE)) {
        fatal(String
            .format("The injected method has a non-empty return type. This is only allowed for methods injected in location XLocation.EXIT"));

      }

    }
    else {
      if (!injectionMethodReturnTypes.equals(Type.VOID_TYPE)&&injectedMethod.getInjectLocation()==XLocation.EXIT) {

        if (!TypeCheckUtil.isAssignable(injectionMethodReturnTypes, targetReturnType,
            classVisitor.getCommonSuperClassUtil()))

        {
          fatal(String.format(
              "Return type of injected method doesn't match the type %s of the target method %s in class %s.",
              targetReturnType.getClassName(), targetMethodName,
              EngineXNameUtil.getExternalName(classVisitor.getClassName())));

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


  /**
   * {@inheritDoc}
   */
  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    if (injectedMethod.getInjectLocation() == XLocation.BEFORE_INVOCATION ||
        injectedMethod.getInjectLocation() == XLocation.REPLACE_INVOCATION ||
        injectedMethod.getInjectLocation() == XLocation.AFTER_INVOCATION) {
      if (injectedMethod.matchesInvoker(owner,name)) {
        applyOnInvokeInstrumentation(opcode, owner, name, desc, itf);
        return;
      }
    }

    
      super.visitMethodInsn(opcode, owner, name, desc, itf);
  }
  
  


  
  
  /**
   * @param opcode
   * @param owner
   * @param name
   * @param desc
   * @param itf
   */
  private void applyOnInvokeInstrumentation(int opcode, String owner, String name, String desc, boolean itf) {
    boolean invokedMethodStatic = (opcode==Opcodes.INVOKESTATIC);
   
    int[] localArgPositions = moveCallArgumentsFromStackToLocals(desc,owner,invokedMethodStatic);
    if (injectedMethod.getInjectLocation() == XLocation.BEFORE_INVOCATION) {      
      injectEngineXCall(localArgPositions,owner,name,desc);     
    }
    if (injectedMethod.getInjectLocation() == XLocation.REPLACE_INVOCATION) {
      injectEngineXCall(localArgPositions,owner,name,desc);
      if(!TypeCheckUtil.isAssignable(injectionMethodReturnTypes, Type.getReturnType(desc), classVisitor.getCommonSuperClassUtil()))
      {
        fatal(String.format("Type mismatch: @XLocation.REPLACE_INVOCATION requires that the return type %s of the injected method %s is assignable to the return type %s of the replaced method invocation to method %s."
            ,EngineXNameUtil.getExternalName(this.injectionMethodReturnTypes.getClassName())
            ,injectedMethod.getMethodName()
            ,EngineXNameUtil.getExternalName(Type.getReturnType(desc).getClassName()),
            name
            ));
      }
    }
    else {
      moveCallArgumentsFromLocalsToStack(desc,invokedMethodStatic,localArgPositions); 
      super.visitMethodInsn(opcode, owner, name, desc, itf);
      //TODO return replace
    }
    if (injectedMethod.getInjectLocation() == XLocation.AFTER_INVOCATION) {
      injectEngineXCall(localArgPositions,owner,name,desc);
    }
  }


  /**
   * 
   * restores the stack for the coming method invocation properly based on the stored local variables
   * @param desc invoked method signature
   * @param owner invoked method owner
   * @param invokedMethodStatic if the target is static
   * @param localArgPositions  the positions where the local variables hold the stack
   */
  private void moveCallArgumentsFromLocalsToStack(String desc,  boolean invokedMethodStatic, int[] localArgPositions) {
    Type[] invokedMethodType = Type.getArgumentTypes(desc);
    if(!invokedMethodStatic) {
      
      super.visitVarInsn(ALOAD, localArgPositions[0]);
    }
    for (int i = 0;i<invokedMethodType.length;i++) {
      Type t = invokedMethodType[i];          
      super.visitVarInsn(t.getOpcode(ILOAD), localArgPositions[i+1]);
    }
  
    
  }

  /**
   * Assumes: stack is prepared for method invocation of a method with descriptor desc Does: remove all the arguments
   * from the stack, stores them in new local variables and returns the local variable positions
   * 
   * @param desc the method descriptor of the method about to be invoked
   * @param invocationTarget the class on which the method is invoked.
   * @param invokedMethodStatic 
   * @return the positions of the local variables that contain the invoked methods parameters. First argument is entry 1. 
   * Entry 0 will contain the invocation target object for non static methods.
   */
  private int[] moveCallArgumentsFromStackToLocals(String desc, String invocationTarget,boolean invokedMethodStatic) {
    Type[] invokedMethodType = Type.getArgumentTypes(desc);
    int[] localVariablePositions = new int[invokedMethodType.length+1];
    for (int i = invokedMethodType.length - 1; i >= 0; i--) {
      Type t = invokedMethodType[i];
      int theLocal = localVariableSorter.newLocal(t);
      localVariablePositions[i+1] = theLocal;
      super.visitVarInsn(t.getOpcode(ISTORE), theLocal);
    }
    if(!invokedMethodStatic) {
      int thisPos=localVariableSorter.newLocal(Type.getType(invocationTarget));
      localVariablePositions[0]=thisPos;
      super.visitVarInsn(ASTORE,thisPos);
    }
    return localVariablePositions;
  }

  @Override
  protected void onMethodExit(int opcode) {
    if (injectedMethod.getInjectLocation() == XLocation.EXIT && opcode == targetReturnType.getOpcode(Opcodes.IRETURN)) {
      injectEngineXCall();
    }
    super.onMethodExit(opcode);
  }

  private void injectEngineXCall(){injectEngineXCall(null,null,null,null);} 
  
  /**
   * 
   * @param invokeArgPositions the local variable indexes where the parameters of the
   * invokedmethod are stored for invocation instrumentation, else for other types.
   * @param invokeMethodDesc the class of the invoked method
   * @param invokeMethodName the signature of the invoked method
   * @param invokeMethodOwner 
   */
  private void injectEngineXCall(int[] invokeArgPositions, String invokeMethodOwner, String invokeMethodName, String invokeMethodDesc) {

    for (int i = 0; i < injectionMethodArgumentTypes.length; i++) {
      prepareCallArgument(i,invokeArgPositions,invokeMethodOwner,invokeMethodName,invokeMethodDesc);
    }
    createVirtualDynamicInvoke();
  }

  private void prepareCallArgument(int i, int[] localArgPositions, String owner,String name,String desc) {

    Injection injectionObject = injectedMethod.getInjection(i);
    if (injectionObject == null) {
      mv.visitInsn(getNullOperand(injectionMethodArgumentTypes[i]));
    }
    else {
      InjectionType iType = injectionObject.getType();
      int injectionSource = injectionObject.getN();

      int localVarIndex=-1;
      Type localVarType=null;
      switch (iType) {
        case PARAMETER:
          localVarType=targetReturnType;
          if(injectionSource!=-1)
          {
            if(injectionSource==0)
            {
              localVarIndex=0;
              localVarType = Type.getType("L" + classVisitor.getClassName() + ";");
            } else
            {
            localVarIndex = getLocalVariablePosition(injectionSource);
            localVarType=targetArguments[injectionSource - 1];
            }
          }
          prepareCallerArgumentArgument(i, localVarIndex,localVarType);
          break;
        case FIELD:


          prepareFieldInjectedArgument(i, injectionObject.getFieldname());
          break;

        case INVOKE_PARAMETER:

          
          if(injectionSource==-1)
          {
            localVarType=Type.getReturnType(desc);  
          } else
          {
            if(injectionSource==0)
            {
              localVarIndex=localArgPositions[0];
              localVarType = Type.getType("L" + owner + ";");
            } else
            {
            if(injectionSource>=localArgPositions.length)
            {
              fatal(String.format("The method %s of class %s doesn't have a parameter %d for injection.",name,owner,injectionSource));
            }
            localVarIndex = localArgPositions[injectionSource];
            localVarType=(Type.getArgumentTypes(desc))[injectionSource - 1];
            }
          }
          prepareCallerArgumentArgument(i, localVarIndex,localVarType);
          break;


      }
    }


  }

  /**
   * 
   * @param pos the position of the argument of the injected method for which this call argument is intended
   * @param sourceLocalVar the local variable index where it can be obtained, -1 for the call stack
   * @param sourceType the type of the local variable slot.
   */
  private void prepareCallerArgumentArgument(int pos, int sourceLocalVar,Type sourceType) {
    Type argument = injectionMethodArgumentTypes[pos];
    if (sourceLocalVar == -1) {
      if (pos != 0)
        fatal("@XReturn/@XInvokeReturn is only allowed on the first parameter.");
      if (injectedMethod.getInjectLocation() != XLocation.EXIT&&injectedMethod.getInjectLocation()!=XLocation.AFTER_INVOCATION)
        fatal("@XReturn/@XInvokeReturn is only allowed on inject location XLocation.EXIT/XLocation.AFTER_INVOKATION");
      if (injectionMethodReturnTypes.equals(Type.VOID_TYPE)) {
        if (sourceType.getSize() == 1) {
          mv.visitInsn(Opcodes.DUP);
        }
        else {
          mv.visitInsn(Opcodes.DUP2);
        }
      } else
      {
        if(!TypeCheckUtil.isAssignable(sourceType,argument, classVisitor.getCommonSuperClassUtil()))
        {
          fatal(String.format("The type returned by the method %s is %s and cannot be assigned to the first argument of the method %s which is %s.",targetMethodName,EngineXNameUtil.getExternalName(sourceType.getClassName()),injectedMethod.getMethodName(),EngineXNameUtil.getExternalName(argument.getClassName())));
        }
      }
    }
    else {
      int localVarIndex = sourceLocalVar;

      int code = argument.getOpcode(Opcodes.ILOAD);

     
     
        if (!TypeCheckUtil.isAssignable(sourceType, argument,
            classVisitor.getCommonSuperClassUtil())) {
          fatal(String
              .format(
                  "Argument Type mismatch:  method parameter of type %s is injected into type %s of method %s in class %s.",
                   EngineXNameUtil.getExternalName(sourceType.getClassName()), EngineXNameUtil.getExternalName(argument.getClassName()),injectedMethod.getMethodName(),
                  EngineXNameUtil.getExternalName(classVisitor.getClassName()))) ;
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
      String desc = field.getDescriptor();
      Type fieldType = Type.getType(desc);
      if (!TypeCheckUtil.isAssignable(fieldType, injectionMethodArgumentTypes[pos],
          classVisitor.getCommonSuperClassUtil())) {
        fatal(String
            .format(
                "The type of the field " + injectionSource + " of the targetclass " +
                    EngineXNameUtil.getExternalName(classVisitor.getClassName()) +
                    " doesn't match the type of the argument %d of the injected method and cannot be injected with XField.",
                pos));
      }

      int opCode = GETSTATIC;

      if (!field.isStatic()) {
        opCode = GETFIELD;
        mv.visitIntInsn(ALOAD, 0);
      }
      mv.visitFieldInsn(opCode, classVisitor.getClassName(), injectionSource, desc);
    }
    else {

      if (!targetMethodStatic) {
        mv.visitVarInsn(ALOAD, 0);
      }
      else {
        mv.visitInsn(ACONST_NULL);
      }
      String className = classVisitor.getClassName();
      mv.visitLdcInsn(Type.getType("L" + className + ";"));

      mv.visitLdcInsn(injectionSource);

      mv.visitMethodInsn(INVOKESTATIC, Type.getType(ReflectionUtil.class).getInternalName(), "getPrivateField",
          "(Ljava/lang/Object;Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;", false);

      castToTargetArgumentType(pos);
    }

  }

  private void castToTargetArgumentType(int pos) {
    Type targetType = injectionMethodArgumentTypes[pos];
    switch (targetType.getSort()) {
      case Type.INT:
        castToPrimitive("java/lang/Integer", "intValue", targetType.getDescriptor());
        break;
      case Type.BOOLEAN:
        castToPrimitive("java/lang/Boolean", "booleanValue", targetType.getDescriptor());
        break;
      case Type.LONG:
        castToPrimitive("java/lang/Long", "longValue", targetType.getDescriptor());
        break;
      case Type.BYTE:
        castToPrimitive("java/lang/Byte", "byteValue", targetType.getDescriptor());
        break;
      case Type.SHORT:
        castToPrimitive("java/lang/Short", "shortValue", targetType.getDescriptor());
        break;
      case Type.CHAR:
        castToPrimitive("java/lang/Character", "charValue", targetType.getDescriptor());
        break;
      case Type.FLOAT:
        castToPrimitive("java/lang/Float", "floatValue", targetType.getDescriptor());
        break;
      case Type.DOUBLE:
        castToPrimitive("java/lang/Double", "doubleValue", targetType.getDescriptor());
        break;
      case Type.ARRAY:
        mv.visitTypeInsn(CHECKCAST, targetType.getInternalName());
        break;
      case Type.OBJECT:
        mv.visitTypeInsn(CHECKCAST, targetType.getInternalName());
        break;

    }

  }

  private void castToPrimitive(String objecttype, String conversionMethod, String descriptor) {
    mv.visitTypeInsn(CHECKCAST, objecttype);
    mv.visitMethodInsn(INVOKEVIRTUAL, objecttype, conversionMethod, "()" + descriptor, false);
  }

  private void createVirtualDynamicInvoke() {
    MethodType mt =
        MethodType.methodType(CallSite.class, MethodHandles.Lookup.class, String.class, MethodType.class, String.class,
            String.class, String.class);
    Handle bootstrap =
        new Handle(Opcodes.H_INVOKESTATIC, "de/schenk/enginex/helper/DynamicBinder", "bindEngineXMethods",
            mt.toMethodDescriptorString());
    JRLog.debug("Instrumentations is including call to class: " +
        injectedMethod.getClassMetadata().getExternalClassName() + " method: " + injectedMethod.getMethodName());
    mv.visitInvokeDynamicInsn(injectedMethod.getMethodName(), injectedMethod.getDescriptor(), bootstrap, injectedMethod
        .getClassMetadata().getExternalClassName(), injectedMethod.getMethodName(), injectedMethod.getDescriptor());
  }

  private int getLocalVariablePosition(Integer injectionSource) {
    int pos = 0;
    Method m = new Method("doesntmatter", descriptor);

    if (!targetMethodStatic) {
      pos++;
      if (injectionSource == 0)
        return 0;
    }
    else {
      if (injectionSource == 0) {
        String msg =
            String.format("Cannot use @XThis or @XParam(n=0) on static method %s in class %s.", targetMethodName,
                EngineXNameUtil.getExternalName(classVisitor.getClassName()));

        fatal(msg);
      }
    }
    Type[] targetArgumentTypes = m.getArgumentTypes();
    for (int j = 0; j < injectionSource - 1; j++) {
      if (j >= targetArgumentTypes.length) {
        fatal(String.format(
            "There is no argument at position %d on method %s of class %s that can be injected with @Param.",
            injectionSource, targetMethodName, EngineXNameUtil.getExternalName(classVisitor.getClassName())));
      }
      pos += targetArgumentTypes[j].getSize();

    }

    return pos;
  }

  private void fatal(String msg) {

    NotificationUtil.sendProblemNotification(msg,
        EngineXNameUtil.getExternalName(injectedMethod.getClassMetadata().getClassName()),
        injectedMethod.getMethodName(), injectionMethodDescriptor);
    throw new RuntimeException(msg);
  }

  private int getNullOperand(Type arguments) {
    if (arguments.getSort() == Type.OBJECT || arguments.getSort() == Type.ARRAY)
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
    throw new RuntimeException("Unkown argument type..." + arguments.toString());
  }

  /**
   * @param lvs
   */
  public void setLocalVariableSorter(LocalVariablesSorter lvs) {
    this.localVariableSorter=lvs;
    
  }
}
