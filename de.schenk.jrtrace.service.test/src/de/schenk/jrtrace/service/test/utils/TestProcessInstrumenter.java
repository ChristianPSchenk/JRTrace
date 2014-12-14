package de.schenk.jrtrace.service.test.utils;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;
import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.helperlib.TraceService;

@XClass(classes = "de.schenk.jrtrace.service.test.utils.TestProcess2")
public class TestProcessInstrumenter {

	@XMethod(names = "goin")
	public void method() {

		System.out.println("Hit: TestProcessInstrumenter");
		TraceService.getInstance().failSafeSend(
				TraceSender.TRACECLIENT_TESTMESSAGES_ID, "msg");
	}
}
