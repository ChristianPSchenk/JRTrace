/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

/** to instrument selected eclipse classes that were problematic once */

@XClass(classes = { "org\\.eclipse\\.jdt\\.ui\\.text\\.JavaSourceViewerConfiguration" }, regex = true)
public class ProblemCaseJavaSourceViewerconfiguration {

	@XMethod(names = ".*")
	public void doit() {

	}
}
