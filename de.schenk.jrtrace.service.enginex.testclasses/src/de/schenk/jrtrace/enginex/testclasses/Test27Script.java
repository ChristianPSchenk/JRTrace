/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;


@XClass( classes = "de.schenk.jrtrace.enginex.testscripts.Test27")
public class Test27Script  {
	 
	private Test27Script(){
		
	}
	public Test27Script(Object anything) {
	
	}
	
	@XMethod(names="test27",location=XLocation.EXIT)
	public String instrument()
	{
		return "instrumented";
	}

}
