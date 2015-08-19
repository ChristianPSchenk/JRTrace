/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses.diagnostics;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XParam;

/* a class that causes an injection exception */
@XClass(classes = ".*CandidateForFaultyInjection", regex = true)
public class InjectForDiagnosticsTestCausingException {

	@XMethod(names = "method")
	public void injector1(@XParam(n = 1) String x) {

	}
}
