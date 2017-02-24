/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.LambdaMetafactory;
import java.security.ProtectionDomain;

import javax.print.attribute.standard.MediaSize.Engineering;

import de.schenk.jrtrace.helper.JRTraceNameUtil;
import de.schenk.jrtrace.helper.JRTraceOneClassTransformer;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.bootstrap.transform.LambdaMetafactoryTransformer;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;

public class JRTraceClassFileTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader classLoader, String className, Class<?> classObject,
			ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {

	
		
		byte[] transformedBytes = null;
		if (JRTraceNameUtil.getInternalName(LambdaMetafactory.class.getCanonicalName()).equals(className)) {
			LambdaMetafactoryTransformer javaLangObjectTransformer = new LambdaMetafactoryTransformer(classBytes);
			transformedBytes = javaLangObjectTransformer.doTransform();
		} else {
			InjectStatus injectStatus = new InjectStatus(
					StatusEntityType.JRTRACE_CHECKED_CLASS);
			JRTraceOneClassTransformer oneTransformer = new JRTraceOneClassTransformer(classLoader, className,
					classObject, classBytes,injectStatus);
			transformedBytes=oneTransformer.doTransform();
		}
		if (JRLog.getLogLevel() == JRLog.DEBUG && transformedBytes != null) {
			logTransformedClassBytes(classBytes, transformedBytes, className);
		}
		return transformedBytes;
	}

	private void logTransformedClassBytes(byte[] classBytes, byte[] transformedBytes, String className) {
		String tmpdir = "";
		try {
			String classId = className;
			if (className == null) {
				classId = String.format("null%d", System.currentTimeMillis());
			}
			tmpdir = System.getProperty("java.io.tmpdir");

			FileOutputStream fileOutputStream = new FileOutputStream(
					tmpdir + File.separator + classId.replace('/', '_') + "before.class");
			fileOutputStream.write(classBytes);
			fileOutputStream.close();
			FileOutputStream fileOutputStream2 = new FileOutputStream(
					tmpdir + File.separator + classId.replace('/', '_') + "after.class");
			fileOutputStream2.write(transformedBytes);
			fileOutputStream2.close();

			JRLog.debug("Writing bytes before/after transformation of class " + classId + " to directory " + tmpdir);
		} catch (IOException e) {
			JRLog.error("Error (" + e.getMessage() + ") when trying to write transformed classbytes to  " + tmpdir);
		}
	}

}
