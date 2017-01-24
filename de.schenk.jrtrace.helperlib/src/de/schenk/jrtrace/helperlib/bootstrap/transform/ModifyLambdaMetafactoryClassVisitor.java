package de.schenk.jrtrace.helperlib.bootstrap.transform;

import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.Label;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;

import static de.schenk.objectweb.asm.Opcodes.*;

import java.lang.invoke.CallSite;

import de.schenk.jrtrace.helper.JRTraceMethodVisitor;
import de.schenk.jrtrace.helperlib.bind.DynamicBinder;

public class ModifyLambdaMetafactoryClassVisitor extends ClassVisitor {

	public ModifyLambdaMetafactoryClassVisitor(int api, ClassVisitor cv) {
		super(api, cv);
	
	}
	
	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
	
		MethodVisitor visitor=super.visitMethod(access, name, desc, signature, exceptions);
		if(name.equals("altMetafactory"))
		{
			
			
			return new MethodVisitor(api,visitor)
					{
						@Override
						public void visitCode() {
							
							super.visitCode();
							
							createNewAltMetafactoryMethod(this);
							
						}
						
						@Override
						public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
						
						}
						
						
						
					};
		}
		return visitor;
	}
	
	
	/**
	 * Inserts the extra code (specified in brackets below) at the beginning of the altMetafactory method.
	 * 
	 * <p>To get this bytecode follow this steps: <ul><li>Create a java project with a copy of the original LambdaMetafactory class. Insert all classes and interfacces that you  need to compile it. </li>
     * </li><li>
     * Prepend the below code to the altMetafactory() method (directly after the signature) and modify it as required.
	 * </li>
	 * <li>Use e.g. the bytecode outline view (requires install!) to get the bytecode of the new inserted code and insert it here.</ul></p>
	 *</li>
	 * @param mv the methodvisitor to use to generate the code
	 */
	public void createNewAltMetafactoryMethod(MethodVisitor mv)
	{
		/* Code for:
		 {
			
			if(args.length==5&&args[0] instanceof Integer&&args[1] instanceof String)
			{
				CallSite result=null;
				if((Integer)args[0]==JRTraceMethodVisitor.NO_VIRTUAL_CALL)
				{
					result=DynamicBinder.bindEngineXMethods(caller,invokedName,invokedType,(String)args[1],(Integer)args[2],(String)args[3],(String)args[4]);
				} else
				{
					result=DynamicBinder.bindEngineXMethodsToVirtual(caller,invokedName,invokedType,(String)args[1],(Integer)args[2],(String)args[3],(String)args[4]);
				}
				return result;			
			}
		}
		 */
	
		Label l0 = new Label();
		mv.visitLabel(l0);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitInsn(ICONST_5);
		Label l1 = new Label();
		mv.visitJumpInsn(IF_ICMPNE, l1);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(INSTANCEOF, "java/lang/Integer");
		mv.visitJumpInsn(IFEQ, l1);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
		mv.visitJumpInsn(IFEQ, l1);
		Label l2 = new Label();
		mv.visitLabel(l2);
		mv.visitInsn(ACONST_NULL);
		mv.visitVarInsn(ASTORE, 4);
		Label l3 = new Label();
		mv.visitLabel(l3);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		mv.visitIntInsn(SIPUSH, 2377);
		Label l4 = new Label();
		mv.visitJumpInsn(IF_ICMPNE, l4);
		Label l5 = new Label();
		mv.visitLabel(l5);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_2);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_3);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_4);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		mv.visitMethodInsn(INVOKESTATIC, "de/schenk/jrtrace/helperlib/bind/DynamicBinder", "bindEngineXMethods", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/CallSite;", false);
		mv.visitVarInsn(ASTORE, 4);
		Label l6 = new Label();
		mv.visitLabel(l6);
		Label l7 = new Label();
		mv.visitJumpInsn(GOTO, l7);
		mv.visitLabel(l4);		
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 2);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_2);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_3);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_4);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/String");
		mv.visitMethodInsn(INVOKESTATIC, "de/schenk/jrtrace/helperlib/bind/DynamicBinder", "bindEngineXMethodsToVirtual", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;)Ljava/lang/invoke/CallSite;", false);
		mv.visitVarInsn(ASTORE, 4);
		mv.visitLabel(l7);		
		mv.visitVarInsn(ALOAD, 4);
		mv.visitInsn(ARETURN);
		mv.visitLabel(l1);
		/*
		
		mv.visitLabel(l1);
		mv.visitFrame(Opcodes.F_NEW, 4, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;"}, 0, new Object[] {});
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodType");
		mv.visitVarInsn(ASTORE, 4);
		Label l8 = new Label();
		mv.visitLabel(l8);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandle");
		mv.visitVarInsn(ASTORE, 5);
		Label l9 = new Label();
		mv.visitLabel(l9);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_2);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodType");
		mv.visitVarInsn(ASTORE, 6);
		Label l10 = new Label();
		mv.visitLabel(l10);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitInsn(ICONST_3);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		mv.visitVarInsn(ISTORE, 7);
		Label l11 = new Label();
		mv.visitLabel(l11);
		mv.visitInsn(ICONST_4);
		mv.visitVarInsn(ISTORE, 10);
		Label l12 = new Label();
		mv.visitLabel(l12);
		mv.visitVarInsn(ILOAD, 7);
		mv.visitInsn(ICONST_2);
		mv.visitInsn(IAND);
		Label l13 = new Label();
		mv.visitJumpInsn(IFEQ, l13);
		Label l14 = new Label();
		mv.visitLabel(l14);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitVarInsn(ILOAD, 10);
		mv.visitIincInsn(10, 1);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		mv.visitVarInsn(ISTORE, 11);
		Label l15 = new Label();
		mv.visitLabel(l15);
		mv.visitVarInsn(ILOAD, 11);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
		mv.visitVarInsn(ASTORE, 8);
		Label l16 = new Label();
		mv.visitLabel(l16);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitVarInsn(ILOAD, 10);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ILOAD, 11);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
		Label l17 = new Label();
		mv.visitLabel(l17);
		mv.visitVarInsn(ILOAD, 10);
		mv.visitVarInsn(ILOAD, 11);
		mv.visitInsn(IADD);
		mv.visitVarInsn(ISTORE, 10);
		Label l18 = new Label();
		mv.visitLabel(l18);
		Label l19 = new Label();
		mv.visitJumpInsn(GOTO, l19);
		mv.visitLabel(l13);
		mv.visitFrame(Opcodes.F_NEW, 11, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, Opcodes.TOP, Opcodes.TOP, Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitFieldInsn(GETSTATIC, "java/lang/invoke/LambdaMetafactory", "EMPTY_CLASS_ARRAY", "[Ljava/lang/Class;");
		mv.visitVarInsn(ASTORE, 8);
		mv.visitLabel(l19);
		mv.visitFrame(Opcodes.F_NEW, 11, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", Opcodes.TOP, Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitVarInsn(ILOAD, 7);
		mv.visitInsn(ICONST_4);
		mv.visitInsn(IAND);
		Label l20 = new Label();
		mv.visitJumpInsn(IFEQ, l20);
		Label l21 = new Label();
		mv.visitLabel(l21);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitVarInsn(ILOAD, 10);
		mv.visitIincInsn(10, 1);
		mv.visitInsn(AALOAD);
		mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		mv.visitVarInsn(ISTORE, 11);
		Label l22 = new Label();
		mv.visitLabel(l22);
		mv.visitVarInsn(ILOAD, 11);
		mv.visitTypeInsn(ANEWARRAY, "java/lang/invoke/MethodType");
		mv.visitVarInsn(ASTORE, 9);
		Label l23 = new Label();
		mv.visitLabel(l23);
		mv.visitVarInsn(ALOAD, 3);
		mv.visitVarInsn(ILOAD, 10);
		mv.visitVarInsn(ALOAD, 9);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ILOAD, 11);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "arraycopy", "(Ljava/lang/Object;ILjava/lang/Object;II)V", false);
		Label l24 = new Label();
		mv.visitLabel(l24);
		mv.visitVarInsn(ILOAD, 10);
		mv.visitVarInsn(ILOAD, 11);
		mv.visitInsn(IADD);
		mv.visitVarInsn(ISTORE, 10);
		Label l25 = new Label();
		mv.visitLabel(l25);
		Label l26 = new Label();
		mv.visitJumpInsn(GOTO, l26);
		mv.visitLabel(l20);
		mv.visitFrame(Opcodes.F_NEW, 11, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", Opcodes.TOP, Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitFieldInsn(GETSTATIC, "java/lang/invoke/LambdaMetafactory", "EMPTY_MT_ARRAY", "[Ljava/lang/invoke/MethodType;");
		mv.visitVarInsn(ASTORE, 9);
		mv.visitLabel(l26);
		mv.visitFrame(Opcodes.F_NEW, 11, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", "[Ljava/lang/invoke/MethodType;", Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitVarInsn(ILOAD, 7);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IAND);
		Label l27 = new Label();
		mv.visitJumpInsn(IFEQ, l27);
		mv.visitInsn(ICONST_1);
		Label l28 = new Label();
		mv.visitJumpInsn(GOTO, l28);
		mv.visitLabel(l27);
		mv.visitFrame(Opcodes.F_NEW, 11, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", "[Ljava/lang/invoke/MethodType;", Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitInsn(ICONST_0);
		mv.visitLabel(l28);
		mv.visitFrame(Opcodes.F_NEW, 11, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", "[Ljava/lang/invoke/MethodType;", Opcodes.INTEGER}, 1, new Object[] {Opcodes.INTEGER});
		mv.visitVarInsn(ISTORE, 11);
		Label l29 = new Label();
		mv.visitLabel(l29);
		mv.visitVarInsn(ILOAD, 11);
		Label l30 = new Label();
		mv.visitJumpInsn(IFEQ, l30);
		Label l31 = new Label();
		mv.visitLabel(l31);
		mv.visitLdcInsn(Type.getType("Ljava/io/Serializable;"));
		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodType", "returnType", "()Ljava/lang/Class;", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z", false);
		mv.visitVarInsn(ISTORE, 12);
		Label l32 = new Label();
		mv.visitLabel(l32);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ASTORE, 16);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitVarInsn(ISTORE, 15);
		mv.visitInsn(ICONST_0);
		mv.visitVarInsn(ISTORE, 14);
		Label l33 = new Label();
		mv.visitJumpInsn(GOTO, l33);
		Label l34 = new Label();
		mv.visitLabel(l34);
		mv.visitFrame(Opcodes.F_NEW, 17, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", "[Ljava/lang/invoke/MethodType;", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER, "[Ljava/lang/Class;"}, 0, new Object[] {});
		mv.visitVarInsn(ALOAD, 16);
		mv.visitVarInsn(ILOAD, 14);
		mv.visitInsn(AALOAD);
		mv.visitVarInsn(ASTORE, 13);
		Label l35 = new Label();
		mv.visitLabel(l35);
		mv.visitVarInsn(ILOAD, 12);
		mv.visitLdcInsn(Type.getType("Ljava/io/Serializable;"));
		mv.visitVarInsn(ALOAD, 13);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "isAssignableFrom", "(Ljava/lang/Class;)Z", false);
		mv.visitInsn(IOR);
		mv.visitVarInsn(ISTORE, 12);
		Label l36 = new Label();
		mv.visitLabel(l36);
		mv.visitIincInsn(14, 1);
		mv.visitLabel(l33);
		mv.visitFrame(Opcodes.F_NEW, 17, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", "[Ljava/lang/invoke/MethodType;", Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.INTEGER, Opcodes.TOP, Opcodes.INTEGER, Opcodes.INTEGER, "[Ljava/lang/Class;"}, 0, new Object[] {});
		mv.visitVarInsn(ILOAD, 14);
		mv.visitVarInsn(ILOAD, 15);
		mv.visitJumpInsn(IF_ICMPLT, l34);
		Label l37 = new Label();
		mv.visitLabel(l37);
		mv.visitVarInsn(ILOAD, 12);
		mv.visitJumpInsn(IFNE, l30);
		Label l38 = new Label();
		mv.visitLabel(l38);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IADD);
		mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "copyOf", "([Ljava/lang/Object;I)[Ljava/lang/Object;", false);
		mv.visitTypeInsn(CHECKCAST, "[Ljava/lang/Class;");
		mv.visitVarInsn(ASTORE, 8);
		Label l39 = new Label();
		mv.visitLabel(l39);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitInsn(ARRAYLENGTH);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(ISUB);
		mv.visitLdcInsn(Type.getType("Ljava/io/Serializable;"));
		mv.visitInsn(AASTORE);
		mv.visitLabel(l30);
		mv.visitFrame(Opcodes.F_NEW, 12, new Object[] {"java/lang/invoke/MethodHandles$Lookup", "java/lang/String", "java/lang/invoke/MethodType", "[Ljava/lang/Object;", "java/lang/invoke/MethodType", "java/lang/invoke/MethodHandle", "java/lang/invoke/MethodType", Opcodes.INTEGER, "[Ljava/lang/Class;", "[Ljava/lang/invoke/MethodType;", Opcodes.INTEGER, Opcodes.INTEGER}, 0, new Object[] {});
		mv.visitTypeInsn(NEW, "java/lang/invoke/InnerClassLambdaMetafactory");
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 2);
		Label l40 = new Label();
		mv.visitLabel(l40);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 4);
		Label l41 = new Label();
		mv.visitLabel(l41);
		mv.visitVarInsn(ALOAD, 5);
		Label l42 = new Label();
		mv.visitLabel(l42);
		mv.visitVarInsn(ALOAD, 6);
		Label l43 = new Label();
		mv.visitLabel(l43);
		mv.visitVarInsn(ILOAD, 11);
		Label l44 = new Label();
		mv.visitLabel(l44);
		mv.visitVarInsn(ALOAD, 8);
		mv.visitVarInsn(ALOAD, 9);
		Label l45 = new Label();
		mv.visitLabel(l45);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/invoke/InnerClassLambdaMetafactory", "<init>", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/invoke/MethodType;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;Z[Ljava/lang/Class;[Ljava/lang/invoke/MethodType;)V", false);
		Label l46 = new Label();
		mv.visitLabel(l46);
		mv.visitVarInsn(ASTORE, 12);
		Label l47 = new Label();
		mv.visitLabel(l47);
		mv.visitVarInsn(ALOAD, 12);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/AbstractValidatingLambdaMetafactory", "validateMetafactoryArgs", "()V", false);
		Label l48 = new Label();
		mv.visitLabel(l48);
		mv.visitVarInsn(ALOAD, 12);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/AbstractValidatingLambdaMetafactory", "buildCallSite", "()Ljava/lang/invoke/CallSite;", false);
		mv.visitInsn(ARETURN);
		Label l49 = new Label();
		mv.visitLabel(l49);
		mv.visitLocalVariable("caller", "Ljava/lang/invoke/MethodHandles$Lookup;", null, l0, l49, 0);
		mv.visitLocalVariable("invokedName", "Ljava/lang/String;", null, l0, l49, 1);
		mv.visitLocalVariable("invokedType", "Ljava/lang/invoke/MethodType;", null, l0, l49, 2);
		mv.visitLocalVariable("args", "[Ljava/lang/Object;", null, l0, l49, 3);
		mv.visitLocalVariable("result", "Ljava/lang/invoke/CallSite;", null, l3, l1, 4);
		mv.visitLocalVariable("samMethodType", "Ljava/lang/invoke/MethodType;", null, l8, l49, 4);
		mv.visitLocalVariable("implMethod", "Ljava/lang/invoke/MethodHandle;", null, l9, l49, 5);
		mv.visitLocalVariable("instantiatedMethodType", "Ljava/lang/invoke/MethodType;", null, l10, l49, 6);
		mv.visitLocalVariable("flags", "I", null, l11, l49, 7);
		mv.visitLocalVariable("markerInterfaces", "[Ljava/lang/Class;", null, l16, l13, 8);
		mv.visitLocalVariable("markerInterfaces", "[Ljava/lang/Class;", null, l19, l49, 8);
		mv.visitLocalVariable("bridges", "[Ljava/lang/invoke/MethodType;", null, l23, l20, 9);
		mv.visitLocalVariable("bridges", "[Ljava/lang/invoke/MethodType;", null, l26, l49, 9);
		mv.visitLocalVariable("argIndex", "I", null, l12, l49, 10);
		mv.visitLocalVariable("markerCount", "I", null, l15, l18, 11);
		mv.visitLocalVariable("bridgeCount", "I", null, l22, l25, 11);
		mv.visitLocalVariable("isSerializable", "Z", null, l29, l49, 11);
		mv.visitLocalVariable("foundSerializableSupertype", "Z", null, l32, l30, 12);
		mv.visitLocalVariable("c", "Ljava/lang/Class;", "Ljava/lang/Class<*>;", l35, l36, 13);
		mv.visitLocalVariable("mf", "Ljava/lang/invoke/AbstractValidatingLambdaMetafactory;", null, l47, l49, 12);
		mv.visitMaxs(11, 17);
		mv.visitEnd();
		*/
	}
		
	
	
}
