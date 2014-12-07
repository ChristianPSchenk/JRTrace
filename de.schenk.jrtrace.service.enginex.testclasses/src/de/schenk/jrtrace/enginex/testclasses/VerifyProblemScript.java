package de.schenk.jrtrace.enginex.testclasses;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XMethod;

@XClass(classes = "org.eclipse.core.resources.IResourceChangeListener", derived = true)
public class VerifyProblemScript {

	@XMethod(names = "resourceChanged")
	public void onremove() {

	}
}
