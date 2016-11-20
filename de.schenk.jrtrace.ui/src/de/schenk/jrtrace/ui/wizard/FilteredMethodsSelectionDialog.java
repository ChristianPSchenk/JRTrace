/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.ui.wizard;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;

import de.schenk.jrtrace.ui.JRTraceUIActivator;

public class FilteredMethodsSelectionDialog extends
		FilteredItemsSelectionDialog {

	public class MethodLabelProvider implements ILabelProvider {

		@Override
		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public void dispose() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		@Override
		public Image getImage(Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public String getText(Object element) {
			IMethod m = (IMethod) element;
			if (m == null)
				return "-";
			else
				return m.getElementName();
		}

	}

	private static final String METHOD_SETTINGS_ID = "method.selection.id";
	private Collection<IMethod> methods = new HashSet<IMethod>();

	public FilteredMethodsSelectionDialog(Shell shell, String typename,
			HashSet<IMethod> foundMethods) {
		super(shell);
		this.methods.addAll(foundMethods);

		setTitle("Method Selection");

		setMessage("Choose a non-argument method of class " + typename
				+ " to execute.");
		setInitialPattern("**"); //$NON-NLS-1$
		setListLabelProvider(new MethodLabelProvider());
		setDetailsLabelProvider(new MethodLabelProvider());

	}

	@Override
	protected Control createExtendedContentArea(Composite parent) {

		return null;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = JRTraceUIActivator.getInstance().getDialogSettings();
		IDialogSettings section = settings.getSection(METHOD_SETTINGS_ID);
		if (section == null) {
			section = settings.addNewSection(METHOD_SETTINGS_ID);
		}
		return section;

	}

	@Override
	protected IStatus validateItem(Object item) {
		return Status.OK_STATUS;
	}

	@Override
	protected ItemsFilter createFilter() {
		return new ItemsFilter() {

			@Override
			public boolean matchItem(Object item) {
				if (!(item instanceof IMethod))
					return false;
				return matches(((IMethod) item).getElementName());

			}

			@Override
			public boolean isConsistentItem(Object item) {
				if (!(item instanceof IMethod))
					return false;
				return true;
			}

		};
	}

	@Override
	protected Comparator<IMethod> getItemsComparator() {

		return new Comparator<IMethod>() {

			@Override
			public int compare(IMethod o1, IMethod o2) {
				return o1.getElementName().compareTo(o2.getElementName());
			}
		};
	}

	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
			ItemsFilter itemsFilter, IProgressMonitor progressMonitor)
			throws CoreException {
		for (IMethod m : methods) {
			contentProvider.add(m, itemsFilter);
		}

	}

	@Override
	public String getElementName(Object item) {
		IMethod m = (IMethod) item;
		return m.getElementName();
	}

}
