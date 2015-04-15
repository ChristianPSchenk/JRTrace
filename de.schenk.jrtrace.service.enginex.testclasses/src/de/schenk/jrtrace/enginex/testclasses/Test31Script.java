/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.annotations.XModifier;

@XClass(classes = "de\\.schenk\\.jrtrace\\.enginex\\.testscripts\\.Test31", regex = true)
public class Test31Script {

	@XMethod(names = "method.*", location = XLocation.EXIT, modifier = { XModifier.STATIC })
	public int instr() {

		return 1;
	}

	@XMethod(names = "method.*", location = XLocation.EXIT, modifier = { XModifier.PRIVATE })
	public int instr2() {

		return 2;
	}

}