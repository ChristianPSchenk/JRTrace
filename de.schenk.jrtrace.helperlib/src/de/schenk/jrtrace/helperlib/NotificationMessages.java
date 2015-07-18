/**
 * (c) 2014/2015 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

public interface NotificationMessages {

	final String MESSAGE_MISSING_NO_ARGUMENT_CONSTRUCTOR = "Failed to create an new instance of the injected class. Note: Every JRTrace class that is injected into target code requires a public no-argument constructor. Note: non-static inner class don't have a public no-argument constructor and cannot be used for injection.";

}
