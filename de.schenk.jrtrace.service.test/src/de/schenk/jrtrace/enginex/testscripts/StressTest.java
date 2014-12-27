package de.schenk.jrtrace.enginex.testscripts;

import static org.junit.Assert.fail;

import org.junit.Test;

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
				fail("ops");
			}

		}
		long y = Runtime.getRuntime().freeMemory();
		System.out.println(String.format("delta=%d", (y - x)));

	}
}
