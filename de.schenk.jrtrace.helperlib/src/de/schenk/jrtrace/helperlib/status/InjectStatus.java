package de.schenk.jrtrace.helperlib.status;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the status of an entity (JRTrace instance, class or method) as to
 * whether it injects code or not including a description of the reason
 * 
 * @author Christian Schenk
 *
 */

public class InjectStatus implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6454290809499559369L;
	/** entity types */
	public static final int JRTRACE_SESSION = 1;
	public static final int JRTRACE_CLASS = 2;
	public static final int JRTRACE_METHOD = 3;

	/* Messages */
	public static final String MSG_NOT_CONNECTED = "Not Connected";
	public static final String MSG_NO_JRTRACE_CLASSES = "No JRTrace classes are currently installed.";
	public static final String MSG_CLASS_NOT_LOADED = "The class is not loaded yet by the Target JVM. ";
	/**
	 * This status indicates that the entity injects code into the analyzed
	 * method
	 */
	public static final int STATE_INJECTS = 1;
	/**
	 * The status code indicates that the entity represented by this status
	 * object won't inject any code in the analyzed methods
	 */
	public static final int STATE_DOESNT_INJECT = 2;
	/**
	 * The status code indicates that it cannot be answered if the entity
	 * represented by this status object injects any code into the analyzed
	 * method
	 */
	public static final int STATE_CANT_CHECK = 3;
	public static final Object MSG_SYSTEM_EXCLUDE = "The class is a JRTrace build in exclude and cannot be instrumented.";

	private String msg;
	private int injectionState = STATE_INJECTS;
	private int entityType;
	private Set<InjectStatus> children = new HashSet<InjectStatus>();

	public InjectStatus(int entityType) {
		this.entityType = entityType;
	}

	/**
	 * 
	 * @return true, if this entity injects code
	 */
	public int getInjectionState() {
		return injectionState;
	}

	/**
	 * 
	 * @return the type of JRTrace entity that this status represents
	 */
	public int getEntityType() {
		return entityType;
	}

	public String getMessage() {
		return msg;
	}

	public void setInjected(int b) {

		injectionState = b;

	}

	public void setMessage(String message) {
		msg = message;

	}

	public void addChildStatus(InjectStatus childStatus) {
		children.add(childStatus);

	}

	public Set<InjectStatus> getChildStatus() {
		return Collections.unmodifiableSet(children);
	}

}
