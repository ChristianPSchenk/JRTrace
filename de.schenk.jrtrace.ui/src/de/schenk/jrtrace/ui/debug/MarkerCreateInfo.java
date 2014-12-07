/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.debug;

import org.eclipse.core.resources.IFile;

public class MarkerCreateInfo {
	public MarkerCreateInfo(IFile resource, String message, int i) {
		file=resource; msg=message; line=i;
	}
	public IFile file;
	public String msg;
	public int line;
	public IFile getFile() {
		return file;
	}
	public int getLine() { return line; }
	public String getMessage()  { return msg; }
}
