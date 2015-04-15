package de.schenk.jrtrace.enginex.reinstall;

public class ReinstallClass3 extends ReinstallBaseClass {
	@Override
	public void method() {

	}

	@Override
	public int call(int a) {
		for (int i = 0; i < 30; i++) {
			method();
			a = a + i;
		}
		return 3 * super.call(a);
	}
}
