/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test.utils;

import de.schenk.jrtrace.helperlib.HelperLib;

public class TestProcessHeavyLoadCommunication extends HelperLib {

	void callTrigger() {

		for (int i = 1; i < 10000; i++) {

			sendMessage((Integer) i);

			sendMessage("averylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessageaverylongmessage");
		}
	}

}
