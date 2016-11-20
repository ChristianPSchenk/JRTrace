/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.service.test.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.service.test.utils.TestProcess2", classloaderpolicy = XClassLoaderPolicy.TARGET)
public class TestProcessInstrumenter {

	@XMethod(names = "goin")
	public void method() {
		int oldValue = TestProcess.value;
		TestProcess.value = 1;

		if (TestProcess.value != oldValue && TestProcess.filePath != null) {
			System.out.print("Writing file!");
			try {
				File outputFile = new File(TestProcess.filePath);
				FileOutputStream fos = new FileOutputStream(outputFile);

				fos.write(String.format("%d", TestProcess.value).getBytes());
				fos.close();

			} catch (IOException e) {

				e.printStackTrace();
			}
		}

	}
}
