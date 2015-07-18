/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.service.internal;

import de.schenk.jrtrace.service.ICancelable;

public class DummyCancelable implements ICancelable {

	@Override
	public boolean isCanceled() {
		return false;
	}

}
