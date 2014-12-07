/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperagent;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import de.schenk.enginex.helper.EngineXHelper;
import de.schenk.enginex.helper.EngineXMetadata;
import de.schenk.jrtrace.helperlib.IJRTraceClientListener;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.helperlib.TraceService;

public class InstallEngineXListener implements IJRTraceClientListener {

	EngineXAnnotationReader annotationReader = new EngineXAnnotationReader();

	public InstallEngineXListener() {

	}

	@Override
	public void messageReceived(String clientSentence) {
		if (clientSentence.isEmpty()) {
			EngineXHelper.clearEngineX();

		} else {
			if (clientSentence.endsWith(".jar")) {
				addEngineXJar(clientSentence);
			} else {

				addEngineXFile(clientSentence);
			}
		}
		TraceService.getInstance().failSafeSend(
				TraceSender.TRACECLIENT_ENGINEX_STATUS, "DONE");

	}

	private void addEngineXJar(String clientSentence) {
		JarFile jar = null;
		try {
			List<EngineXMetadata> mdlist = new ArrayList<EngineXMetadata>();
			jar = new JarFile(clientSentence);
			Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				JarEntry jarentry = entries.nextElement();
				String name = jarentry.getName();
				if (!jarentry.isDirectory() && name.endsWith(".class")) {

					InputStream in = new BufferedInputStream(
							jar.getInputStream(jarentry));
					ByteArrayOutputStream out = new ByteArrayOutputStream(
							(int) jarentry.getSize());
					byte[] buffer = new byte[2048];
					while (true) {
						int bytes = in.read(buffer);
						if (bytes <= 0)
							break;
						out.write(buffer, 0, bytes);
					}
					EngineXMetadata metadata = createMetadata(out.toByteArray());
					mdlist.add(metadata);
				}
			}
			EngineXHelper.addEngineXClass(mdlist);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (jar != null)
				try {
					jar.close();
				} catch (IOException e) {
					// do nothing more.
				}
		}

	}

	public void addEngineXFile(String clientSentence) {
		byte[] enginexclass = getFileAsByteArray(clientSentence);
		EngineXMetadata metadata = createMetadata(enginexclass);
		EngineXHelper.addEngineXClass(metadata);
	}

	public EngineXMetadata createMetadata(byte[] enginexclass) {
		EngineXMetadata metadata = annotationReader
				.getMetaInformation(enginexclass);
		metadata.addBytes(enginexclass);
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
