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

@XClass( classes = "de.schenk.jrtrace.enginex.testscripts.Test26", derived=true)
public class Test26Script {

	public int haveit=0;
	Runnable runnable=new Test26ScriptRunnable(this);
			
//			new Runnable()
//	{
//		
//		
//		@Override
//		public void run() {
//			haveit=1;
//			
//		}
//	};
	
	@XMethod(names="test26",location=XLocation.EXIT)
	public String instr()
	{
		runnable.run();
		return String.format("%d",haveit);	}
	
}