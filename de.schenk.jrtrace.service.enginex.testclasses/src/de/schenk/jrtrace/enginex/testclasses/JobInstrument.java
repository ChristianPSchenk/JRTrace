/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "org.eclipse.core.runtime.jobs.Job")
public class JobInstrument {

	@XMethod(names = "schedule")
	public void test() {
		System.out.println("schedule");

	}
}
