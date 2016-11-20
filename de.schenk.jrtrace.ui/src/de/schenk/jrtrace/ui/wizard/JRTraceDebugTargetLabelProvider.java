/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.wizard;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;

public class JRTraceDebugTargetLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// do nothing

	}

	@Override
	public void dispose() {
		// do nothing

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// do nothing
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// do nothing

	}

	@Override
	public Image getImage(Object element) {

		return null;
	}

	@Override
	public String getText(Object element) {
		JRTraceDebugTarget target = (JRTraceDebugTarget) element;
		try {
			return target.getLaunch().getLaunchConfiguration().getName();

		} catch (Exception e) {
			return "<debug problem>";
		}

	}

}
