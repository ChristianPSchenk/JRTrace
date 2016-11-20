/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.jrtrace.annotations.XField;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XThis;
import de.schenk.jrtrace.enginex.testscripts.Test23;

@XClass( classes = "de.schenk.jrtrace.enginex.testscripts.Test25", derived=true)
public class Test25Script {

	@XMethod(names="test25",location=XLocation.EXIT)
	public String instr()
	{
		return "instrumented";
	}
	
}