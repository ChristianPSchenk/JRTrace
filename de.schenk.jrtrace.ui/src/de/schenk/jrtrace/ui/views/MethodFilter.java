package de.schenk.jrtrace.ui.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;

public class MethodFilter extends ViewerFilter {

	private String methodName;

	public MethodFilter(String methodName) {
		this.methodName = methodName;
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (methodName == null)
			return true;
		if (element instanceof InjectStatus) {
			InjectStatus s = (InjectStatus) element;
			if (s.getEntityType() == StatusEntityType.JRTRACE_CHECKED_METHOD) {
				if (s.getEntityName().contains(methodName)) {
					return true;
				} else
					return false;
			} else
				return true;
		}
		return true;
	}

}
