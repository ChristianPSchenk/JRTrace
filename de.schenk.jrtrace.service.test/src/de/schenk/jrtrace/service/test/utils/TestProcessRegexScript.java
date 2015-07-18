/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.service.test.utils;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = ".*TestProcess2", classloaderpolicy = XClassLoaderPolicy.TARGET, regex = true)
public class TestProcessRegexScript {
	@XMethod(names = "goin")
	public void method() {

	}
}
