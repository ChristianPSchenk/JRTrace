/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent.internal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.helper.Injection;
import de.schenk.jrtrace.helper.Injection.InjectionType;
import de.schenk.jrtrace.helper.JRTraceClassMetadata;
import de.schenk.jrtrace.helper.JRTraceHelper;
import de.schenk.jrtrace.helper.JRTraceMethodMetadata;
import de.schenk.jrtrace.helper.NotificationUtil;
import de.schenk.jrtrace.helperagent.JRTraceAnnotationReader;

public class InstallEngineXCommand {

	JRTraceAnnotationReader annotationReader = new JRTraceAnnotationReader();

	public InstallEngineXCommand() {

	}

	public void installEngineX(byte[][] jarBytes) {

		setEngineXClasses(jarBytes);

	}

	private void setEngineXClasses(byte[][] jarFileBytes) {
		List<JRTraceClassMetadata> mdlist = new ArrayList<JRTraceClassMetadata>();
		for (int i = 0; i < jarFileBytes.length; i++) {
			JRTraceClassMetadata md = createMetadata(jarFileBytes[i]);
			if (md != null) {
				mdlist.add(md);
			}
		}
		JRTraceHelper.addEngineXClass(mdlist);

	}

	/**
	 * 
	 * @param enginexclassbytes
	 *            the jrtrace class
	 * @return the metadata structure extracted from the annotations or null if
	 *         there is a severe error.
	 */
	public JRTraceClassMetadata createMetadata(byte[] enginexclassbytes) {
		JRTraceClassMetadata metadata = annotationReader
				.getMetaInformation(enginexclassbytes);

		String version = System.getProperty("java.version");
		int maxClassFileVersion = 52;
		if (version.startsWith("1.7")) {
			maxClassFileVersion = 51;
		}
		if (metadata.getClassVersion() > maxClassFileVersion) {
			NotificationUtil
					.sendProblemNotification(
							String.format(
									"The JRTrace class has classfile version %d. But a the target JVM is version %s and supports a maximum of classfile version %d.",
									metadata.getClassVersion(), version,
									maxClassFileVersion), metadata
									.getExternalClassName(), null, null);
			return null;
		}

		for (JRTraceMethodMetadata methodmetadata : metadata.getMethods()) {
			Injection returnParam = methodmetadata.getInjection(0);
			if (returnParam != null
					&& InjectionType.INVOKE_PARAMETER.equals(returnParam
							.getType()) && returnParam.getN() == -1) {
				if (methodmetadata.getInjectLocation() != XLocation.AFTER_INVOCATION
						&& methodmetadata.getInjectLocation() != XLocation.REPLACE_INVOCATION) {
					methodmetadata.removeInjection(0);
					NotificationUtil
							.sendProblemNotification(
									String.format(
											"The method specifies @XInvokeReturn. This is only allowed for location AFTER_INVOCATION and REPLACE_INVOCATION. This annotation will be ignored.",
											methodmetadata.getMethodName()),
									metadata.getExternalClassName(),
									methodmetadata.getMethodName(),
									methodmetadata.getDescriptor());
				}
			}
			if (returnParam != null
					&& InjectionType.PARAMETER.equals(returnParam.getType())
					&& returnParam.getN() == -1) {
				if (methodmetadata.getInjectLocation() != XLocation.EXIT) {
					methodmetadata.removeInjection(0);
					NotificationUtil
							.sendProblemNotification(
									String.format(
											"The method specifies @XReturn. This is only allowed for the location EXIT. This annotation will be ignored.",
											methodmetadata.getMethodName()),
									metadata.getExternalClassName(),
									methodmetadata.getMethodName(),
									methodmetadata.getDescriptor());
				}
			}
		}

		return metadata;
	}

	private byte[] getFileAsByteArray(String clientSentence) {
		File f = new File(clientSentence);
		if (!f.exists()) {
			throw new RuntimeException("Class file " + clientSentence
					+ " doesn't exist");
		}
		FileInputStream ins = null;
		try {
			ins = new FileInputStream(f);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = ins.read(data, 0, data.length)) != -1) {
				bos.write(data, 0, nRead);
			}

			bos.flush();

			return bos.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Error while converting the file "
					+ clientSentence + " to a bytearray.", e);
		} finally {
			try {
				ins.close();
			} catch (IOException e) {
				// do nothing
			}
		}

	}

}
