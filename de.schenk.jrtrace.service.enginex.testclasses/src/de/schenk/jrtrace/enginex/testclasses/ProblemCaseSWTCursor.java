/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

/** to instrument selected eclipse classes that were problematic once */

@XClass(classes = {

"org\\.eclipse\\.swt\\.graphics\\.Cursor" }, regex = true)
public class ProblemCaseSWTCursor {

	@XMethod(names = "isDisposed")
	public void doit() {

	}
}
