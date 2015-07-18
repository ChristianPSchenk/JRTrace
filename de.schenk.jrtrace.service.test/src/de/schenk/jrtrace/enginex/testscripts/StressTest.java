/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.enginex.testscripts;

import org.junit.Test;

/**
 * 
 * @author Christian Schenk
 *
 */
public class StressTest {

	@Test
	public void testLoad() {
		System.gc();
		long x = Runtime.getRuntime().freeMemory();
		EngineXDetailsTest details = new EngineXDetailsTest();
		for (int i = 0; i < 100; i++) {
			System.out.println("Mem:");
			System.out.println(Runtime.getRuntime().freeMemory());
			try {
				details.before();
				details.verifyErrorTest();
				details.after();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

		}
		long y = Runtime.getRuntime().freeMemory();
		System.out.println(String.format("delta=%d", (y - x)));

	}
}
