/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import de.schenk.jrtrace.helper.JRTraceOneClassTransformer;

public class JRTraceClassFileTransformer implements ClassFileTransformer {

	@Override
	public byte[] transform(ClassLoader classLoader, String className,
			Class<?> classObject, ProtectionDomain protectionDomain,
			byte[] classBytes) throws IllegalClassFormatException {

		JRTraceOneClassTransformer oneTransformer = new JRTraceOneClassTransformer(
				classLoader, className, classObject, classBytes);

		byte[] trasnformedBytes = oneTransformer.doTransform();

		return trasnformedBytes;
	}

}
