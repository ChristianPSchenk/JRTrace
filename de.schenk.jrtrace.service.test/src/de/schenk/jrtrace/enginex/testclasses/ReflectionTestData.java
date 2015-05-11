package de.schenk.jrtrace.enginex.testclasses;

public class ReflectionTestData {

	private static int fkt(Double d, byte[] b) {
		return d.intValue() + (int) (b[0]) + (int) b[1];
	}

	private int fkt2(int j) {
		return 0;
	}

	private int fkt2(int i, ReflectionTestData d) {
		return i * i;
	}
}
