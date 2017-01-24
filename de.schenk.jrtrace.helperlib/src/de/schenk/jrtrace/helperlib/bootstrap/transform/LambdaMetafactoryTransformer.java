package de.schenk.jrtrace.helperlib.bootstrap.transform;

import de.schenk.jrtrace.helper.JRTraceClassVisitor;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.objectweb.asm.ClassReader;
import de.schenk.objectweb.asm.ClassVisitor;
import de.schenk.objectweb.asm.ClassWriter;
import de.schenk.objectweb.asm.Opcodes;
import de.schenk.objectweb.asm.addons.ClassWriterForClassLoader;

public class LambdaMetafactoryTransformer {

	private byte[] bytes;

	public LambdaMetafactoryTransformer(byte[] classBytes) {
		bytes=classBytes;
	}

	public byte[] doTransform() {
		
			try
			{
		
		ClassReader classReader = new ClassReader(bytes);
		ClassWriter classWriter = new ClassWriter(classReader,ClassWriter.COMPUTE_FRAMES);

		ClassVisitor visitor = classWriter;

		ClassVisitor classVisitor = new ModifyLambdaMetafactoryClassVisitor(
				 Opcodes.ASM5,visitor);
		
		classReader.accept(classVisitor,0);
		byte[] newBytes = classWriter.toByteArray();
		return newBytes;
			} catch(Throwable e)
			{
				e.printStackTrace();
			}
			return null;
		
	
	}

}
