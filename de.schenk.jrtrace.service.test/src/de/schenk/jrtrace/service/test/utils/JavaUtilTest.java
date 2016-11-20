/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service.test.utils;

import junit.framework.TestCase;

public class JavaUtilTest  extends TestCase {
	
	public void testTestProcess() throws Exception
	{
		JavaUtil util = new JavaUtil();
		util.obtainKillPort();
		 Thread t = util.startTestProcessInSameVM();
		for(int i=0;i<40;i++)
		{
		util.sendKillUDPPacket();
		t.join(50);
		if(!t.isAlive()) break;
		}
		assertTrue(!t.isAlive());
	}
}
