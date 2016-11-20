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

@XClass( classes = "de.schenk.jrtrace.enginex.testscripts.Test28", derived=true)
public class Test28Script {

	public String haveit="not";
	Runnable runnable=
			
			new Runnable()
	{
		
		
		@Override
		public void run() {
			haveit="done";
			
		}
	};
	
	@XMethod(names="test28",location=XLocation.EXIT)
	public String instr()
	{
		runnable.run();
		return "instrumented";	}
	
}