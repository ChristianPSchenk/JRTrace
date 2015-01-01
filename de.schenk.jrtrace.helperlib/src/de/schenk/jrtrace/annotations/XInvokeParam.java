/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used when instrumenting before, after or instead of a method invocation:
 * If a parameter of the injected method is annotated with {@link XInvokeParam} the
 * value of the corresponding call argument of the instrumented method invocation 
 * is injected here. 
 * <p>Only valid with {@XLocation.BEFORE_INVOKE},{@XLocation.AFTER_INVOKE} and {@XLocation.REPLACE_INVOKE}
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XInvokeParam {

	/**
	 * 
	 * @return the position of the parameter in the invoked method that is to be
	 *         injected into the instrumentation method.
	 *         <p>
	 *         1: the first parameter of the invoked method.
	 *         <p>
	 *         2: ...
	 *         <p>
	 *         0: only valid for non-static method calls: the instance on which the method is invoked.
	 */
	int n();
}
