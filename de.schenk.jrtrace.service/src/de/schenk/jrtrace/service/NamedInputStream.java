/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * overrides toString to return a human readable name to be used by jrtrace.
 *
 */
public class NamedInputStream extends InputStream {

	InputStream theWrappedStream;
	String name;
	public NamedInputStream(String osString, InputStream stream) {
		theWrappedStream=stream;
		name=osString;
	}

	@Override
	public String toString() {
		return name;
	}
	@Override
	public int read() throws IOException {
		return theWrappedStream.read();
	}
	
	@Override
	public void close() throws IOException {
		if(theWrappedStream!=null) theWrappedStream.close();
		super.close();
	}

}
