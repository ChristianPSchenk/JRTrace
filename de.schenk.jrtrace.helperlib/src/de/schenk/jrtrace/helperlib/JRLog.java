package de.schenk.jrtrace.helperlib;

import java.util.Date;

public class JRLog {

	public final static int ERROR = 0;

	public final static int VERBOSE = 2;
	public final static int DEBUG = 3;

	private JRLog() {
		// just a collection of global static, don't instantiate it.
	}

	static private int loglevel = ERROR;

	static public void setLogLevel(int level) {
		loglevel = level;

	}

	static public int getLogLevel() {
		return loglevel;
	}

	static public void verbose(String msg) {
		log(VERBOSE, msg);
	}

	static public void debug(String msg) {
		log(DEBUG, msg);
	}

	static private void log(int msgLevel, String msg) {
		if (msgLevel <= loglevel) {
			Date d = new Date();
			System.out.println(

			String.format("%TH:%TM:%TS.%TL %s", d, d, d, d, msg));
		}

	}

	public static void error(String string) {
		log(ERROR, string);

	}

}
