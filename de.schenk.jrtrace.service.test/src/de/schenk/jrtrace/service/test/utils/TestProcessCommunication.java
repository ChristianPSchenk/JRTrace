/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.service.test.utils;

import de.schenk.jrtrace.helperlib.HelperLib;

public class TestProcessCommunication extends HelperLib {

	public void called(String msg, int[] i) {
		System.out.println("TestProccessCommunication.called(Object o)");

		if (msg.equals("ping") && (i.length == 2) && i[0] == 1 && i[1] == 2) {
			if (trigger)
				sendMessage(new Object[] { "pong", -1 });
			else {
				sendMessage("kaputt");
			}
		}

	}

	static volatile boolean trigger = false;

	void callTrigger(Object o) {

		trigger = true;
		System.out.println("Calltrigger called");
	}

}
