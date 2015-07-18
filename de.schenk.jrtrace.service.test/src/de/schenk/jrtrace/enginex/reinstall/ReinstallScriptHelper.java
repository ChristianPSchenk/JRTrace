/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.reinstall;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;

@XClass(classloaderpolicy = XClassLoaderPolicy.BOOT)
public class ReinstallScriptHelper {

	static int counter = 0;

	public static void count()

	{
		counter++;
	}
}
