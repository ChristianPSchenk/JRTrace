/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.test.utils;


public class TestProcess {

	public static int value = 0;
	private static int killPort;
	public static String filePath;

	public static void main(String[] args) {
		System.out.println(String.format(
				"+starting main in TestProcess, kill expected on %s", args[0]));

		if (args.length > 1) {
			System.out.println("TestProcess.main(): Writing int to file");
			filePath = args[1];
		}
		killPort = Integer.parseInt(args[0]);

		new TestProcess().doSomeThing();
		System.out.println("+TestProcess.main() terminated.");

	}

	private void doSomeThing() {
		KillThread t = new KillThread(Thread.currentThread(), killPort);
		t.setDaemon(true);
		t.start();
		try {

			System.out.println("+doSomeThing()");
			long total = 0;
			while (true) {
				total += doit();

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					System.out.println("+interrupted");
					break;
				}
			}

			System.out.println("+done");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

	private long doit() {
		new TestProcess2().goin();
		return System.currentTimeMillis();

	}

}
