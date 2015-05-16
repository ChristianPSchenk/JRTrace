/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.service.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import de.schenk.jrtrace.service.JRTraceControllerService;
import de.schenk.jrtrace.service.VMInfo;

/**
 * Convenience dialog to choose the proper target process. Will show a list of
 * all available process along with the process description. Allows the user to
 * filter based on the process description using a filter text field.
 * 
 * The advantage of attaching to a process using a substring of the process
 * description is, that it remains stable if the same application is started
 * multiple times and can be used again to attach to the "same" program.
 * 
 * 
 * @author Christian P. Schenk
 */
public class PIDSelectionDialog extends TitleAreaDialog {
	/**
   * 
   */
	private static final int DESCRIPTION_SPLIT_LENGTH = 120;

	private static final int USEFILTER_ID = 3;

	/**
	 * @author CKS2SI
	 *
	 */
	public class VMViewerFilter extends ViewerFilter {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			DialogVMInfo vm = (DialogVMInfo) element;
			String ft = filterText.getText();
			if (ft != null && !ft.isEmpty()) {
				return (vm.getInfo().getName().toLowerCase().contains(ft
						.toLowerCase()));
			} else
				return true;
		}

	}

	private boolean useFilterText = false;

	private boolean showFilterTextButton;

	private Button useFilterButton;

	private Button usePIDButton;

	private boolean attachWorking;

	/**
	 * 
	 * @param parentShell
	 * @param showIdentifyTextButton
	 *            false: standard: show only one button "Use Process ID" to
	 *            choose the proper process. true: if the "Use Filter Text"
	 *            button should be shown in addition to the "Use Process ID"
	 *            button. If both buttons are shown, the dialog can be closed
	 *            with "Use Filter Text" once the filter narrows down the choice
	 *            of matching processes to exactly one process.
	 * 
	 */
	public PIDSelectionDialog(Shell parentShell, boolean showIdentifyTextButton) {
		super(parentShell);
		this.showFilterTextButton = showIdentifyTextButton;
		this.attachWorking = JRTraceControllerService.getInstance()
				.hsperfdataAccessible();

	}

	/**
	 * 
	 * This dialog works around the 250 character per cell restriction by
	 * splitting eac VMInfo in multiple entries. Only the first line will be
	 * shown
	 * 
	 * 
	 * @author Christian Schenk
	 *
	 */
	private class DialogVMInfo {

		private boolean firstEntry;
		private VMInfo vminfo;
		private String partDesc;

		/**
		 * @param pid
		 * @param name
		 */
		public DialogVMInfo(VMInfo info, String partDescription,
				boolean firstEntry) {
			this.vminfo = info;
			this.partDesc = partDescription;
			this.firstEntry = firstEntry;
		}

		public VMInfo getInfo() {
			return vminfo;
		}

		public String getPartialDescription() {
			return partDesc;
		}

		public boolean getFirstEntry() {
			return firstEntry;
		}

	}

	public class VMLabelProvider extends StyledCellLabelProvider {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void update(ViewerCell cell) {
			DialogVMInfo v = (DialogVMInfo) cell.getElement();
			int index = cell.getColumnIndex();
			if (index == 0) {
				if (v.getFirstEntry()) {

					cell.setText(v.getInfo().getId());
				} else {
					cell.setText("");
				}

			}
			if (index == 1) {
				String content = (v.getFirstEntry() ? "" : "   ")
						+ v.getPartialDescription();
				
				cell.setText(content);

				if (!filterText.getText().isEmpty()) {
					int pos = content.toLowerCase().indexOf(filterText.getText().toLowerCase());

					if (pos != -1) {
						StyleRange style = new StyleRange(pos, filterText
								.getText().length(), Display.getDefault()
								.getSystemColor(SWT.COLOR_RED), null);
						cell.setStyleRanges(new StyleRange[] { style });

					} else
						cell.setStyleRanges(new StyleRange[] {});

				} else
					cell.setStyleRanges(new StyleRange[] {});
				super.update(cell);

			}
		}
	}

	public class VMContentProvider implements IStructuredContentProvider {

		private DialogVMInfo[] convertedInput;

		@Override
		public void dispose() {

		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// hack: tableviewer in windows shows only 260 characters width.
			// There
			// often are super long
			// command lines.
			ArrayList<DialogVMInfo> biggerList = new ArrayList<DialogVMInfo>();

			VMInfo[] temp = (VMInfo[]) newInput;

			if (temp == null) {
				convertedInput = null;
				;
				return;
			}

			for (VMInfo t : temp) {

				String dis = t.getName();
				int start = 0;
				boolean first = true;
				while (start < dis.length()) {
					int end = start + DESCRIPTION_SPLIT_LENGTH < dis.length() - 1 ? start
							+ DESCRIPTION_SPLIT_LENGTH
							: dis.length();
					String part = dis.substring(start, end);
					DialogVMInfo v = new DialogVMInfo(t, part, first);
					first = false;
					biggerList.add(v);

					start = end;
				}

			}
			convertedInput = biggerList.toArray(new DialogVMInfo[0]);

		}

		@Override
		public Object[] getElements(Object inputElement) {
			return convertedInput;
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {

		if (showFilterTextButton) {
			useFilterButton = createButton(parent, USEFILTER_ID,
					"Use Filter Text", true);

		}
		usePIDButton = createButton(parent, IDialogConstants.OK_ID,
				"Use Process ID", !showFilterTextButton);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == USEFILTER_ID) {
			useFilterText = true;
			buttonId = IDialogConstants.OK_ID;
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(
				this.convertWidthInCharsToPixels(DESCRIPTION_SPLIT_LENGTH + 20),
				600);
	}

	private String pid = "";

	private VMInfo[] vms;

	private TableViewer viewer;

	private Text filterText;

	private String selectedFilterText = "";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void create() {
		super.create();
		setTitle("JRTrace Target Process Selection");

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public int open() {
		if (vms == null) {
			vms = JRTraceControllerService.getInstance().getVMs();
		}
		return super.open();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Control createDialogArea(Composite parent) {

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridData gd1 = new GridData(GridData.FILL_HORIZONTAL);

		gd1.grabExcessVerticalSpace = false;

		container.setLayoutData(gd1);
		container.setLayout(new GridLayout(1, true));

		filterText = new Text(area, SWT.BORDER | SWT.SEARCH);
		GridData gdFilter = new GridData();
		gdFilter.grabExcessHorizontalSpace = true;
		gdFilter.horizontalAlignment = SWT.FILL;
		filterText.setLayoutData(gdFilter);
		filterText.setText(selectedFilterText);
		filterText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {

				viewer.refresh(true);
				refreshEnablement();

			}

		});

		viewer = new TableViewer(area, SWT.V_SCROLL | SWT.H_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);

		viewer.setContentProvider(new VMContentProvider());
		viewer.addFilter(new VMViewerFilter());

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				refreshEnablement();

			}
		});

		TableViewerColumn pidcolumn = new TableViewerColumn(viewer, SWT.NONE);
		int width_id = this.convertWidthInCharsToPixels(8);
		pidcolumn.getColumn().setWidth(width_id);
		pidcolumn.getColumn().setText("PID");

		pidcolumn.setLabelProvider(new VMLabelProvider());

		TableViewerColumn descriptionColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		int width = this.convertWidthInCharsToPixels(DESCRIPTION_SPLIT_LENGTH);
		descriptionColumn.getColumn().setWidth(width);
		descriptionColumn.getColumn().setText("Description");
		descriptionColumn.getColumn().setResizable(true);
		descriptionColumn.setLabelProvider(new VMLabelProvider());

		final Table table = viewer.getTable();
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.horizontalAlignment = SWT.FILL;
		gd.verticalAlignment = SWT.FILL;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		if (!attachWorking) {
			this.setErrorMessage("In the folder %TMP%/hsperfdata_%USERNAME% no files can be created or the folder itself cannot be created. In this case JVMs cannot be listed and attaching to running processes is not possible. Ensure that this folder is accessible. A restart of the development environment might be required.");
		}

		viewer.setInput(vms);
		return area;

	}

	/**
   * 
   */
	protected void refreshEnablement() {

		StructuredSelection strucSel = (StructuredSelection) viewer
				.getSelection();

		if (strucSel.size() == 1) {
			if (useFilterButton != null)
				useFilterButton.setEnabled(true);
			usePIDButton.setEnabled(true);
		} else {
			if (useFilterButton != null)
				useFilterButton.setEnabled(false);
			usePIDButton.setEnabled(false);
			Object firstElement = viewer.getElementAt(0);
			if (firstElement != null) {
				StructuredSelection sel = new StructuredSelection(firstElement);

				viewer.setSelection(sel);
			}

		}
	}

	/*
	 * By default this dialog shows all JVMs that it can find on the current
	 * machine.
	 * 
	 * Use this method to show only the passed in list of VMs in the dialog.
	 * Call this method before open().
	 */
	public void setVMs(VMInfo[] vms) {
		this.vms = vms;
		if (viewer != null)
			viewer.setInput(vms);

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void okPressed() {

		StructuredSelection sel = (StructuredSelection) viewer.getSelection();

		if (sel.size() == 1) {

			DialogVMInfo vminfo = (DialogVMInfo) sel.getFirstElement();
			selectedFilterText = filterText.getText();
			pid = vminfo.getInfo().getId();

		} else {
			selectedFilterText = "";
			pid = "";
		}
		super.okPressed();
	}

	/**
	 * 
	 * @return true, if the user completed the dialog using the
	 *         "Use Filter Text" button.
	 */
	public boolean wasFilterTextSelection() {
		return useFilterText;
	}

	/**
	 * 
	 * @return the selected PID or null if more than one or no PID was selected.
	 */
	public String getPID() {
		return pid;
	}

	/**
	 * 
	 * @return the text in the fiter that was used to filter.
	 */
	public String getFilterText() {
		return selectedFilterText;
	}

	/**
	 * @param text
	 */
	public void setFilterText(String text) {
		selectedFilterText = text;

	}

}
