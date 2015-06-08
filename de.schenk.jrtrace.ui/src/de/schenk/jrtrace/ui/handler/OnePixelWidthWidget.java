package de.schenk.jrtrace.ui.handler;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class OnePixelWidthWidget extends Composite {

	public OnePixelWidthWidget(Composite parent, int style) {
		super(parent, style);

	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return new Point(1, 1);
	}

}
