/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.launch;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ListDialog;

import de.schenk.jrtrace.service.internal.VMInfo;

public class PIDSelectionDialog {

	public class VMLabelProvider extends LabelProvider {

		@Override
		public String getText(Object element) {
			VMInfo v = (VMInfo) element;
			return v.getId() + ": " + v.getName();
		}

	}

	public class VMContentProvider implements IStructuredContentProvider {

		private VMInfo[] input;

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// hack: tableviewer in windows shows only 260 characters width. We
			// often have super long
			// command lines.
			ArrayList<VMInfo> biggerList = new ArrayList<VMInfo>();

			VMInfo[] temp = (VMInfo[]) newInput;
			if (temp == null) {
				input = temp;
				return;
			}
			for (VMInfo t : temp) {
				String id = t.getId();
				String dis = t.getName();
				int start = 0;
				boolean first = true;
				while (start < dis.length()) {
					int end = start + 250 < dis.length() - 1 ? start + 250
							: dis.length();
					String part = dis.substring(start, end);
					VMInfo v = new VMInfo(id, (first ? " " : "     ") + part);
					first = false;
					biggerList.add(v);

					start = end;
				}

			}
			input = biggerList.toArray(new VMInfo[0]);

		}

		@Override
		public Object[] getElements(Object inputElement) {
			return input;
		}

	}

	private String pid = "";

	private VMInfo[] vms;

	private VMInfo[] getVMS() {
		return vms;
	}

	public PIDSelectionDialog() {

	}

	/*
	 * Restrict the selection to those VMs containing the identify text string
	 */
	public void setVMs(VMInfo[] vms) {
		this.vms = vms;
	}

	public boolean show(Shell shell) {

		VMInfo[] input = getVMS();

		ListDialog dlg = new ListDialog(shell);
		dlg.setContentProvider(new VMContentProvider());
		dlg.setLabelProvider(new VMLabelProvider());
		dlg.setTitle("Select VM to Attach");
		dlg.setInput(input);
		dlg.setWidthInChars(1024);

		dlg.setAddCancelButton(true);
		dlg.open();
		Object[] selection = dlg.getResult();

		if (selection != null && selection.length == 1) {
			VMInfo vminfo = (VMInfo) selection[0];
			pid = vminfo.getId();
			return true;

		} else {
			return false;
		}

	}

	public String getPID() {
		return pid;
	}

}
