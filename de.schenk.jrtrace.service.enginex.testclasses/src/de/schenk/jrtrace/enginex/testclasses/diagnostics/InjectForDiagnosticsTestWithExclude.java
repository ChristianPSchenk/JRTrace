/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses.diagnostics;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.*", exclude = "de.schenk.*EngineX.*ForExclude", regex = true)
public class InjectForDiagnosticsTestWithExclude {

	@XMethod(names = "doesntmatter")
	public void injector1() {

	}
}
