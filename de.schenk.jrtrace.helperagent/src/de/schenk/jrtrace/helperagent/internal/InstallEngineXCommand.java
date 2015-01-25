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

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.enginex.helper.EngineXMetadata;
import de.schenk.enginex.helper.NotificationUtil;
import de.schenk.jrtrace.helperagent.EngineXAnnotationReader;

public class InstallEngineXCommand {

	EngineXAnnotationReader annotationReader = new EngineXAnnotationReader();

	public InstallEngineXCommand() {

	}

	public void installEngineX(byte[][] jarBytes) {

		setEngineXClasses(jarBytes);

	}

	private void setEngineXClasses(byte[][] jarFileBytes) {
		List<EngineXMetadata> mdlist = new ArrayList<EngineXMetadata>();
		for (int i = 0; i < jarFileBytes.length; i++) {
			EngineXMetadata md = createMetadata(jarFileBytes[i]);
			if (md != null) {
				mdlist.add(md);
			}
		}
		EngineXHelper.addEngineXClass(mdlist);

	}

	/**
	 * 
	 * @param enginexclassbytes
	 *            the jrtrace class
	 * @return the metadata structure extracted from the annotations or null if
	 *         there is a severe error.
	 */
	public EngineXMetadata createMetadata(byte[] enginexclassbytes) {
		EngineXMetadata metadata = annotationReader
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
