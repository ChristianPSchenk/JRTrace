/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.example;

import java.util.Map;
import java.util.Map.Entry;

import de.schenk.jrtrace.helperlib.HelperLib;

public class StackTraceUtility extends HelperLib {

	public void getStackTrace() {
		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		StringBuilder b = new StringBuilder();

		for (Entry<Thread, StackTraceElement[]> e : traces.entrySet()) {
			b.append(e.getKey().getName() + "\n");
			for (StackTraceElement element : e.getValue()) {
				b.append("   " + element.toString() + "\n");
			}

		}
		sendMessage(b.toString());
	}

}
