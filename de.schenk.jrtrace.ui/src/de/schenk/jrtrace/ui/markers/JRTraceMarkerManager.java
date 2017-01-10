/**
 * (c) 2014/2015 by Christian Schenk
 **/
package de.schenk.jrtrace.ui.markers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.AttributeChangeNotification;
import javax.management.Notification;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.SearchMatch;

import de.schenk.jrtrace.helperlib.NotificationConstants;
import de.schenk.jrtrace.service.NotificationAndErrorListener;
import de.schenk.jrtrace.ui.JRTraceUIActivator;
import de.schenk.jrtrace.ui.debug.JRTraceDebugTarget;
import de.schenk.jrtrace.ui.debug.MarkerCreateInfo;

/**
 * responsible for keeping the rules in the specified projects in sync with the
 * target
 */
public class JRTraceMarkerManager extends NotificationAndErrorListener {
	private JRTraceJavaSearch searcher;

	private final class JRTraceMarkerCreateJob extends Job {
		private static final String JRTRACEPROBLEM = "com.schenk.jrtrace.core.problemmarker";
		private HashSet<MarkerCreateInfo> createFiles;
		private HashSet<IResource> deleteResources;

		private JRTraceMarkerCreateJob(String name, HashSet<IResource> deleteResources,
				HashSet<MarkerCreateInfo> createFiles) {
			super(name);
			this.deleteResources = deleteResources;
			this.createFiles = createFiles;

		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {

				for (IResource f : deleteResources) {
					if (f.exists()) {
						f.deleteMarkers(JRTRACEPROBLEM, true, IResource.DEPTH_INFINITE);
					}
				}

				for (MarkerCreateInfo f : createFiles) {
					if (f.getMessage() != null) {
						createMarkerForLocation(monitor, f);

					}
				}

			} catch (CoreException e1) {
				logError("Exception while updating the JRTrace problem markers", e1);
				return Status.CANCEL_STATUS;
			}

			return Status.OK_STATUS;
		}

		/**
		 * prepares a new marker with the location specified in the
		 * MarkerCreateInfo
		 * 
		 * @param monitor
		 * @param f
		 * @return
		 * @throws CoreException
		 */
		private IMarker createMarkerForLocation(IProgressMonitor monitor, MarkerCreateInfo f) {
			IMarker marker = null;
			IType theClass = null;

			try {
				String searchName = f.getClassName();
				if (searchName != null && searchName.contains("$")) {
					searchName = searchName.replace('$', '.');
				}
				List<SearchMatch> results = searcher.searchClass(searchName, monitor);

				if (results.size() > 0) {
					for (SearchMatch result : results) {
						IProject matchProject = result.getResource().getProject();
						if (matchProject.equals(target.getProject())) {
							marker = result.getResource().createMarker(JRTRACEPROBLEM);
							theClass = (IType) result.getElement();
							break;
						}
					}
					if(marker==null)
					{
						marker = results.get(0).getResource().createMarker(JRTRACEPROBLEM);
						theClass = (IType) results.get(0).getElement();
					}

				} else {
					if (target.getProject() != null) {
						marker = target.getProject().createMarker(JRTRACEPROBLEM);
					} else {
						ResourcesPlugin.getWorkspace().getRoot().createMarker(JRTRACEPROBLEM);
					}
				}
				IMethod method = null;
				if (theClass != null && f.getMethod() != null && !f.getMethod().isEmpty()) {
					method = searcher.searchMethod(theClass, f.getMethod(), f.getMethodDescriptor());
				}

				IMember theElement = theClass;
				if (method != null) {
					theElement = method;
				}
				if (theElement != null) {
					ISourceRange sourceRange = theElement.getSourceRange();

					marker.setAttribute(IMarker.CHAR_START, sourceRange.getOffset());
					marker.setAttribute(IMarker.CHAR_END, sourceRange.getOffset() + sourceRange.getLength());
				}
				marker.setAttribute(IMarker.LOCATION, "JRTrace Problem");
				marker.setAttribute(IMarker.MESSAGE, f.getMessage().replaceAll("\n", " "));
				marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			} catch (CoreException e) {
				throw new RuntimeException(e);
			}
			return marker;
		}
	}

	Set<IResource> markerDeleteResource = new HashSet<IResource>();
	Set<MarkerCreateInfo> markerCreateFiles = new HashSet<MarkerCreateInfo>();

	private JRTraceDebugTarget target;

	public JRTraceMarkerManager(JRTraceDebugTarget JRTraceDebugTarget) {

		target = JRTraceDebugTarget;

		this.searcher = new JRTraceJavaSearch();
		target.getJRTraceMachine().addClientListener(NotificationConstants.NOTIFY_PROBLEM, this);

	}

	private void logError(String msg, Exception e) {
		JRTraceUIActivator.getInstance().getLog().log(new Status(IStatus.ERROR, JRTraceUIActivator.BUNDLE_ID, msg, e));
	}

	private void updateMarkers() {
		boolean nothingToDo = true;
		HashSet<IResource> copyDeleteResources;
		HashSet<MarkerCreateInfo> copyCreateFiles;
		synchronized (this) {
			nothingToDo = (markerCreateFiles.size() == 0 && markerDeleteResource.size() == 0);

			copyDeleteResources = new HashSet<IResource>();
			copyDeleteResources.addAll(markerDeleteResource);
			copyCreateFiles = new HashSet<MarkerCreateInfo>();
			copyCreateFiles.addAll(markerCreateFiles);
			markerDeleteResource.clear();
			markerCreateFiles.clear();

		}

		if (nothingToDo)
			return;
		Job markerJob = new JRTraceMarkerCreateJob("Creating JRTrace Markers", copyDeleteResources, copyCreateFiles);

		markerJob.schedule(100);
	}

	@Override
	public void sendMessage(Notification notification) {
		AttributeChangeNotification not = (AttributeChangeNotification) notification;

		String classname = (String) not.getOldValue();
		String method = (String) not.getNewValue();
		String msg = not.getMessage();
		String descriptor = not.getAttributeName();

		synchronized (this) {
			markerCreateFiles.add(new MarkerCreateInfo(classname, method, msg, descriptor));
		}
		updateMarkers();

	}

	public void close() {
		clearAllMarkers();
		if (target.getJRTraceMachine() != null)
			target.getJRTraceMachine().removeClientListener(NotificationConstants.NOTIFY_PROBLEM, this);

	}

	public void clearAllMarkers() {
		synchronized (this) {

			markerDeleteResource.add(ResourcesPlugin.getWorkspace().getRoot());

		}
		updateMarkers();

	}

}
