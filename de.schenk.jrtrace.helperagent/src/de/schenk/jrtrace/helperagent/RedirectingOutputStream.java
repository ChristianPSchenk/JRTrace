/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperagent;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;

import de.schenk.jrtrace.helperlib.TraceSender;
import de.schenk.jrtrace.helperlib.TraceService;

public class RedirectingOutputStream extends OutputStream {

	private PrintStream outPrinter;
	private StringWriter internalWriter;
	private int comId;

	public RedirectingOutputStream(PrintStream out) {

		this(out, TraceSender.TRACECLIENT_STDOUT_ID);
	}

	public RedirectingOutputStream(PrintStream out, int id) {
		comId = id;
		outPrinter = out;
		internalWriter = new StringWriter();
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		outPrinter.write(arg0, arg1, arg2);
		internalWriter.write(new String(arg0), arg1, arg2);
		writeMessage();
	}

	@Override
	public void write(int b) throws IOException {
		outPrinter.write(b);
		internalWriter.write(b);
		if (b == 13) {
			writeMessage();

		}

	}

	boolean inSend = false;

	synchronized private void writeMessage() {
		String msg = internalWriter.toString();
		if (!inSend) {
			try {
				inSend = true;
				TraceService.getInstance().failSafeSend(comId, msg);
			} finally {
				inSend = false;
			}
		} else {
			outPrinter.print(msg);
		}
		internalWriter.getBuffer().setLength(0);
	}

}
