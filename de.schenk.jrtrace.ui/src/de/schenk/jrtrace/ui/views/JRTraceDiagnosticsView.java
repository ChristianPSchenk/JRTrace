/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.views;

import java.util.HashMap;
import java.util.Set;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusEntityType;
import de.schenk.jrtrace.helperlib.status.StatusState;
import de.schenk.jrtrace.ui.JRTraceUIActivator;
import de.schenk.jrtrace.ui.util.JDTUtil;

/**
 * 
 * @author Christian Schenk
 *
 */
public class JRTraceDiagnosticsView extends ViewPart implements
		ISelectionListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "de.schenk.jrtrace.ui.views.JRTraceDiagnosticsView";

	private TreeViewer viewer;

	private Action syncWithSelectionAction;

	@Override
	public void dispose() {
		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
		super.dispose();
	}

	class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {

		private InjectStatus rootStatus;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			this.rootStatus = (InjectStatus) newInput;
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {

			if (rootStatus == null) {
				return new InjectStatus[0];
			} else {
				if (rootStatus.getChildStatus().size() == 0) {
					return new InjectStatus[] { (InjectStatus) rootStatus };
				}
				return rootStatus.getChildStatus().toArray(new InjectStatus[0]);

			}
		}

		public Object getParent(Object child) {
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof InjectStatus) {
				if (((InjectStatus) parent).getChildStatus().size() == 0) {

				}
				InjectStatus s = (InjectStatus) parent;
				Set<InjectStatus> ch = s.getChildStatus();
				return ch.toArray(new InjectStatus[ch.size()]);
			} else {
				return new InjectStatus[0];
			}
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof InjectStatus)
				return ((InjectStatus) parent).getChildStatus().size() > 0;
			return false;
		}

	}

	class ViewLabelProvider extends LabelProvider {

		HashMap<String, Image> imageCache = new HashMap<String, Image>();

		public String getText(Object obj) {
			if (obj instanceof InjectStatus) {
				InjectStatus is = (InjectStatus) obj;
				if (!is.getMessage().isEmpty())
					return is.getEntityName() + " : " + is.getMessage();
				else
					return is.getEntityName();
			}
			return obj.toString();
		}

		@Override
		public void dispose() {
			for (Image i : imageCache.values()) {
				i.dispose();
			}
			super.dispose();
		}

		public Image getImage(Object obj) {

			String imageKey = ISharedImages.IMG_OBJ_FOLDER;
			Image image = PlatformUI.getWorkbench().getSharedImages()
					.getImage(imageKey);

			if (obj instanceof InjectStatus) {
				InjectStatus is = (InjectStatus) obj;

				return getInjectStatusImage(is.getEntityType(),
						is.getInjectionState());
				/*
								*/
			}

			return image;
		}

		private Image getInjectStatusImage(StatusEntityType entityType,
				StatusState injectionState) {
			String key = entityType.toString() + injectionState.toString();
			if (imageCache.get(key) != null)
				return imageCache.get(key);

			Image image = null;
			switch (entityType) {
			case JRTRACE_CHECKED_CLASS:
				image = JavaUI.getSharedImages().getImage(
						JavaUI.getSharedImages().IMG_OBJS_CLASS);
				break;
			case JRTRACE_CHECKED_METHOD:
				image = JavaUI.getSharedImages().getImage(
						JavaUI.getSharedImages().IMG_OBJS_PUBLIC);
				break;
			case JRTRACE_CLASS:
				image = JRTraceUIActivator.getInstance().getImageRegistry()
						.get(JRTraceUIActivator.JRTRACE_CLASS_16PX);
				break;

			case JRTRACE_METHOD:
				image = JRTraceUIActivator.getInstance().getImageRegistry()
						.get(JRTraceUIActivator.JRTRACE_METHOD_16PX);
				break;

			case JRTRACE_MACHINE:
			case JRTRACE_SESSION:
				String imageKey = ISharedImages.IMG_OBJ_FOLDER;
				image = PlatformUI.getWorkbench().getSharedImages()
						.getImage(imageKey);
				break;
			}

			ImageDescriptor desc = null;

			switch (injectionState) {
			case CANT_CHECK:
				desc = PlatformUI
						.getWorkbench()
						.getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING);
			case DOESNT_INJECT:
				desc = PlatformUI.getWorkbench().getSharedImages()
						.getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR);
				break;
			case INJECTS:

				desc = WorkbenchImages
						.getWorkbenchImageDescriptor("/ovr16/running_ovr.png");

				break;

			}
			DecorationOverlayIcon overlayed = new DecorationOverlayIcon(image,
					desc, IDecoration.TOP_RIGHT);

			Image newImage = overlayed.createImage();
			imageCache.put(key, newImage);
			return newImage;
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public JRTraceDiagnosticsView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		ILabelDecorator decorator = PlatformUI.getWorkbench()
				.getDecoratorManager().getLabelDecorator();

		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());

		viewer.setInput(null);

		makeActions();
		hookContextMenu();

		contributeToActionBars();

		getSite().getWorkbenchWindow().getSelectionService()
				.addSelectionListener(this);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				JRTraceDiagnosticsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(syncWithSelectionAction);

	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(syncWithSelectionAction);

		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(syncWithSelectionAction);

	}

	private void makeActions() {

		syncWithSelectionAction = new Action() {
		};
		syncWithSelectionAction.setChecked(true);

		syncWithSelectionAction.setText("Update from Selection");
		syncWithSelectionAction
				.setToolTipText("When enabled, selecting a Java method will update this view automatically.");
		syncWithSelectionAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OPEN_MARKER));

	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	class DiagnosticJobCompletedListener implements
			IDiagnosticJobCompletedListener {

		private String methodName;

		public DiagnosticJobCompletedListener(String methodName) {
			this.methodName = methodName;
		}

		@Override
		public void completed(final InjectStatus result) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (!viewer.getTree().isDisposed()) {
						if (syncWithSelectionAction.isChecked()) {
							result.updateStatusFromChildren(methodName);
							viewer.setInput(result);
							JRTraceMethodFilter filter = new JRTraceMethodFilter(
									methodName);
							viewer.setFilters(new ViewerFilter[] { filter });
							viewer.refresh();
							viewer.expandAll();
						}
					}
				}

			});

		}
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		IMember member = getMember(part, selection);
		if (member != null) {

			if (member.getDeclaringType() != null) {
				String methodName = member.getElementName();
				if (member.getDeclaringType().getElementName()
						.equals(methodName)) {
					methodName = "<init>";
				}
				JRTraceDiagnosticsJob job = new JRTraceDiagnosticsJob(member
						.getDeclaringType().getFullyQualifiedName(),
						new DiagnosticJobCompletedListener(methodName));

				job.schedule();
			}
		}

	}

	public IMember getMember(IWorkbenchPart part, ISelection selection) {

		if (part instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IMember member = JDTUtil.getSelectedFunction(editor);
			if (member != null) {
				return member;
			}

		}

		return JDTUtil.getSelectedFunction(selection);

	}
}