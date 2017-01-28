/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testscripts;

import java.io.File;

public class Test45 {

	public String test45(StringBuffer result) {
		
		try
		{
			File f=new File("gehtnicht");
		} catch(NoClassDefFoundError e)
		{
			result.append("gut");
		}
		return result.toString();
	}

	
}
