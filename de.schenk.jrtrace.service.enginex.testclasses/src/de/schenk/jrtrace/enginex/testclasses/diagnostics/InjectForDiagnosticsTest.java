/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses.diagnostics;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "de.schenk.jrtrace.enginex.testscripts.EngineXDiagnosticsTarget")
public class InjectForDiagnosticsTest {

	@XMethod(names = "matchingMethod")
	public void injector1() {

	}
}
