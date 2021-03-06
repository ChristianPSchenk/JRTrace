/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.ui;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class JRTraceUIActivator extends AbstractUIPlugin {

	static final public String BUNDLE_ID = "de.schenk.jrtrace.ui";
	public static final String JRTRACE_16PX_GIF = "jrtrace.16px";
	public static final String JRTRACE_256PX_GIF = "jrtrace.256px";
	public static final String NATURE_ID = "de.schenk.jrtrace.nature.id";
	public static final String JRTRACE_CLASS_16PX = "jrtrace_class.16px";
	public static final String JRTRACE_METHOD_16PX = "jrtrace_method.16px";
	static private JRTraceUIActivator activator;

	/**
	 * 
	 * Get the ImageDescriptor for one of the icons.
	 * 
	 * @param relPath
	 *            the relative path to the icon starting from the icons/
	 *            directory
	 * @return the image descriptor
	 */
	public ImageDescriptor getDescriptor(String relPath) {
		URL url = null;

		url = FileLocator.find(this.getBundle(), new Path("icons/" + relPath),
				null);

		ImageDescriptor image;
		try {
			image = ImageDescriptor.createFromURL(FileLocator.toFileURL(url));

			return image;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void start(BundleContext context) throws Exception {
		activator = this;

		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				JRTraceUIActivator.this.getImageRegistry().put(
						JRTRACE_16PX_GIF,
						getDescriptor("jrtrace_icon_16px.png"));
				JRTraceUIActivator.this.getImageRegistry().put(
						JRTRACE_256PX_GIF,
						getDescriptor("jrtrace_icon_256px.png"));
				JRTraceUIActivator.this.getImageRegistry().put(
						JRTRACE_CLASS_16PX,
						getDescriptor("jrtrace_class_16px.png"));
				JRTraceUIActivator.this.getImageRegistry().put(
						JRTRACE_METHOD_16PX,
						getDescriptor("jrtrace_method_16px.png"));
			}
		});

		super.start(context);
	}

	static public JRTraceUIActivator getInstance() {
		return activator;
	}

}
