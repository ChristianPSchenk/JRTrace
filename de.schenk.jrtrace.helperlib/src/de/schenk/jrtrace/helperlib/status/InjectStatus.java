/**
 * (c) 2014/2015 by Christian Schenk
 **/
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

	/* Messages */
	public static final String MSG_NO_JRTRACE_SESSION = "There is no active JRTrace session.";
	public static final String MSG_METHOD_MODIFIERS_DONT_MATCH = "The modifiers of the method don't match the 'modifiers' specification of the @XMethod.";
	public static final String MSG_PARAMETERS_DONT_MATCH = "The parameters of the method don't match the 'parameters' specification of the @XMethod.";
	public static final String MSG_METHODNAME_DOESNT_MATCH = "The name of the method doesn't match the 'names' specification of the @XMethod.";
	public static final String MSG_NOT_CONNECTED = "Not Connected";
	public static final String MSG_NO_JRTRACE_CLASSES = "No JRTrace classes are currently installed.";
	public static final String MSG_CLASS_NOT_LOADED = "The class is not loaded yet by the Target JVM. ";
	public static final String MSG_SYSTEM_EXCLUDE = "The class is one of the JRTrace built-in excluded class and therefore cannot be instrumented with JRTrace";
	public static final String MSG_JRTRACE_CLASS_CANNOT_BE_INSTRUMENTED = "The class is one of the classes that have been installed with JRTrace and therefore cannot be instrumented with JRTrace.";
	public static final String MSG_CLASS_NAME_DOESNT_MATCH = "The name of the class doesn't match any of the classes specified in 'classes' of the @XClass.";
	public static final String MSG_METHOD_IS_ABSTRACT = "An interface or abstract method cannot be instrumented.";

	public static final String MSG_METHOD_DOESNT_INVOKE_SPECIFIED_METHOD = "The method doesn't invoke a method that matches the attributes 'invokedname' and/or 'invokedclass' specified in the @XMethod.";
	public static final String MSG_METHOD_DOESNT_ACCESS_SPECIFIED_FIELD = "The method doesn't access a field with the specified name 'fieldname' and/or of the specified class  'fieldclass' specified in the @XMethod.";
	public static final String MSG_METHOD_DOESNT_THROW_SPECIFIED_EXCEPTION = "The method doesn't throw any exception. ";

	public static final String MSG_THATS_ODD = "That's odd: every method should be entered or exited. Something is not right with JRTrace here...";

	public static final String MSG_CLASSES_ATTRIBUTE_NOT_SET = "The class doesn't specify a 'classes' attribute in the @XClass and therefore will never match any class for injection.";

	public static final String MSG_CLASS_IS_EXCLUDED = "This JRTrace class specifies an 'exclude' list of the @XClass that matches the class to be instrumented. Therefore the class will not be instrumented.";

	public static final String MSG_COMMUNICATION_PROBLEM = "An exception occured while trying to get the injection status.";

	private String msg = "";
	private StatusState injectionState = StatusState.INJECTS;
	private StatusEntityType entityType;
	private Set<InjectStatus> children = new HashSet<InjectStatus>();
	private String entityName = "";

	public InjectStatus(StatusEntityType entityType) {
		this.entityType = entityType;
	}

	public void setEntityName(String name) {
		entityName = name;
	}

	/**
	 * 
	 * @return the injection state of this entity
	 */
	public StatusState getInjectionState() {
		return injectionState;
	}

	/**
	 * 
	 * @return the type of JRTrace entity that this status represents
	 */
	public StatusEntityType getEntityType() {
		return entityType;
	}

	public String getMessage() {
		return msg;
	}

	public void setInjected(StatusState b) {

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

	public String getEntityName() {
		return entityName;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();

		b.append(entityType.toString() + ":");

		b.append(injectionState.toString() + " ");
		b.append(" " + entityName);
		b.append(":" + this.msg);

		return b.toString();
	}

	/**
	 * recursively traverses all the children and sets the cumulative status: -
	 * if at least one child injects, the overall status is inject - if at least
	 * one child says: don't know, the overall status is don't know - if all
	 * children say: don't inject: the overall status is don't inject.
	 */
	public void updateStatusFromChildren() {
		updateStatusFromChildren(null);
	}

	/*
	 * Same as updateStatusFromChildren, but will exclude all methods with name
	 * methodName from the hierarchy when calculating the status. This is
	 * actually more a view code and doesn't really belong here.
	 */
	public void updateStatusFromChildren(String methodName) {

		boolean cantCheck = false;
		boolean canInject = false;
		int count = 0;
		for (InjectStatus s : children) {

			if (s.entityType != StatusEntityType.JRTRACE_CHECKED_METHOD
					|| (methodName == null || (s.entityType == StatusEntityType.JRTRACE_CHECKED_METHOD && s
							.getEntityName().contains(methodName)))) {
				count++;

				s.updateStatusFromChildren(methodName);

				if (s.getInjectionState() == StatusState.CANT_CHECK)
					cantCheck = true;
				if (s.getInjectionState() == StatusState.INJECTS)
					canInject = true;
			}
		}
		if (count == 0)
			return;
		if (canInject)
			injectionState = StatusState.INJECTS;
		else {
			if (cantCheck)
				injectionState = StatusState.CANT_CHECK;
			else
				injectionState = StatusState.DOESNT_INJECT;
		}
	}

	public InjectStatus getChildByEntityName(String methodName) {
		if (!methodName.startsWith(".*"))
			methodName = ".*" + methodName;
		if (!methodName.endsWith(".*"))
			methodName = methodName + ".*";
		InjectStatus result = null;
		for (InjectStatus s : children) {
			String entityName = s.getEntityName();

			if (entityName.matches(methodName)) {
				if (result != null)
					throw new IllegalArgumentException(String.format(
							"More than one child matched by %s in %s",
							methodName, children));
				result = s;

			}
		}
		return result;
	}
}
