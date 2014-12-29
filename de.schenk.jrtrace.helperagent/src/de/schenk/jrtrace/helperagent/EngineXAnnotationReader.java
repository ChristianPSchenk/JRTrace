/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import de.schenk.enginex.helper.EngineXMetadata;
import de.schenk.enginex.helper.EngineXMethodMetadata;
import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XField;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XParam;
import de.schenk.jrtrace.annotations.XReturn;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.objectweb.asm.AnnotationVisitor;
import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.MethodVisitor;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.Type;

public class EngineXAnnotationReader {

	public class MetadataParameterAnnotationVisitor extends AnnotationVisitor {

		private EngineXMethodMetadata method;
		private int param;

		public MetadataParameterAnnotationVisitor(
				EngineXMethodMetadata methodmd, int parameter,
				AnnotationVisitor visitParameterAnnotation) {

			super(Opcodes.ASM5, visitParameterAnnotation);

			this.param = parameter;
			this.method = methodmd;
		}

		@Override
		public void visit(String name, Object value) {
			if ("n".equals(name)) {
				method.addInjection(param, (int) value);
			}
			if ("name".equals(name)) {
				method.addInjection(param, (String) value);
			}
			super.visit(name, value);
		}

	}

	public class MetadataMethodMethodVisitor extends MethodVisitor {

		private EngineXMethodMetadata methodmd;

		public MetadataMethodMethodVisitor(EngineXMethodMetadata md,
				MethodVisitor visitMethod) {
			super(Opcodes.ASM5);
			this.methodmd = md;
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

			return new MethodMetadataAnnotationVisitor(methodmd, null,
					super.visitAnnotation(desc, visible));
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter,
				String desc, boolean visible) {

			if (Type.getType(XThis.class).equals(Type.getType(desc))) {
				methodmd.addInjection(parameter, 0);
			}
			if (Type.getType(XReturn.class).equals(Type.getType(desc))) {

				methodmd.addInjection(parameter, -1);
			}
			boolean injectField = Type.getType(XField.class).equals(
					Type.getType(desc));
			boolean injectArgument = Type.getType(XParam.class).equals(
					Type.getType(desc));
			if (injectArgument || injectField) {
				return new MetadataParameterAnnotationVisitor(methodmd,
						parameter, super.visitParameterAnnotation(parameter,
								desc, visible));
			}
			return super.visitParameterAnnotation(parameter, desc, visible);
		}
	}

	public class MethodMetadataAnnotationVisitor extends AnnotationVisitor {

		private EngineXMethodMetadata method;

		private String context;
		boolean dataAdded = false;

		public MethodMetadataAnnotationVisitor(EngineXMethodMetadata md,
				String context, AnnotationVisitor visitor) {
			super(Opcodes.ASM5, visitor);
			this.context = context;
			this.method = md;
		}

		@Override
		public void visitEnd() {
			if (context == null && !method.getTargetMethodNames().isEmpty()) {
				method.getClassMetadata().addMethod(method);
			}
			if ("arguments".equals(context) && !dataAdded) {
				method.setArgumentListEmpty();
			}
			super.visitEnd();
		}

		@Override
		public AnnotationVisitor visitArray(String name) {

			return new MethodMetadataAnnotationVisitor(method, name,
					super.visitArray(name));
		}

		@Override
		public void visitEnum(String name, String desc, String value) {
			if ("location".equals(name)) {
				method.setInjectLocation(XLocation.valueOf(value));
			}

			super.visitEnum(name, desc, value);
		}

		@Override
		public void visit(String name, Object value) {
			switch (context) {
			case "names":

				method.addTargetMethodName((String) value);
				break;

			case "arguments":

				method.addArgument((String) value);
				dataAdded = true;
				break;
			}
			super.visit(name, value);
		}

	}

	public class ClassMetadataAnnotationVisitor extends AnnotationVisitor {

		private EngineXMetadata md;

		private String context;

		public ClassMetadataAnnotationVisitor(EngineXMetadata md,
				String context, AnnotationVisitor visitor) {
			super(Opcodes.ASM5, visitor);
			this.context = context;
			this.md = md;
		}

		@Override
		public AnnotationVisitor visitArray(String name) {

			return new ClassMetadataAnnotationVisitor(md, name,
					super.visitArray(name));
		}

		@Override
		public void visitEnum(String name, String desc, String value) {
			if ("classloaderpolicy".equals(name)) {
				md.setClassLoaderPolicy(XClassLoaderPolicy.valueOf(value));

			}

			super.visitEnum(name, desc, value);
		}

		@Override
		public void visit(String name, Object value) {
			if ("derived".equals(name)) {
				md.setDerived((boolean) value);
			}
			if ("regex".equals(name)) {
				md.setUseRegex((boolean) value);
			}
			if ("classloadername".equals(name)) {
				md.setClassLoaderName((String) value);
			}

			if ("classes".equals(context)) {
				if (!(value instanceof String)) {
					md.setInvalid("classes field is not type String");
					return;
				}
				md.addClassesEntry((String) value);
			}
			super.visit(name, value);
		}

	}

	public class MetadataClassVisitor extends ClassVisitor {

		private EngineXMetadata md;
		private boolean foundEngineXAnnotation = false;

		public MetadataClassVisitor(EngineXMetadata md) {
			super(Opcodes.ASM5);
			this.md = md;
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			EngineXMethodMetadata methodMetadata = new EngineXMethodMetadata(
					md, name, desc);
			return new MetadataMethodMethodVisitor(
					methodMetadata,
					super.visitMethod(access, name, desc, signature, exceptions));
		}

		@Override
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			md.setClassName(name);
			super.visit(version, access, name, signature, superName, interfaces);
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {

			if (Type.getType(desc).equals(Type.getType(XClass.class))) {
				foundEngineXAnnotation = true;
			}
			return new ClassMetadataAnnotationVisitor(md, null,
					super.visitAnnotation(desc, visible));
		}

		@Override
		public void visitEnd() {
			if (!foundEngineXAnnotation) {
				md.setInvalid();
			}
			super.visitEnd();
		}

	}

	/**
	 * 
	 * @param classBytes
	 *            the class from which to read the metadata
	 * @return a a EngineXMetadata object that describes the processing
	 *         information in the class.
	 */
	public EngineXMetadata getMetaInformation(byte[] classBytes) {
		final EngineXMetadata md = new EngineXMetadata();

		ClassReader cr = new ClassReader(classBytes);
		MetadataClassVisitor mdcvisitor = new MetadataClassVisitor(md);
		cr.accept(mdcvisitor, 0);

		md.addBytes(classBytes);
		
		return md;

	}

}
