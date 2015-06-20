package de.schenk.jrtrace.helperlib.status;

import java.io.Serializable;

public enum StatusState implements Serializable {
	/**
	 * This status indi<cates that the entity injects code into the analyzed
	 * method
	 */
	INJECTS,
	/**
	 * The status code indicates that the entity represented by this status
	 * object won't inject any code in the analyzed methods
	 */
	DOESNT_INJECT,

	/**
	 * The status code indicates that it cannot be answered if the entity
	 * represented by this status object injects any code into the analyzed
	 * method
	 */
	CANT_CHECK
}
