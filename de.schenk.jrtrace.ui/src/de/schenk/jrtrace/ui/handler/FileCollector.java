/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.ui.handler;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

public class FileCollector implements IResourceVisitor {

	private ArrayList<IFile> files;

	
	
	public FileCollector(ArrayList<IFile> files) {
		this.files=files;
	}

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if(resource instanceof IFile) files.add((IFile)resource);
		return true;
	}

}
