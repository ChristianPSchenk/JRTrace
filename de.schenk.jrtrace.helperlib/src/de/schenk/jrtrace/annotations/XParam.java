/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a parameter of the injected method is annotated with {@link XParam} the
 * value of one of the arguments of the target method will be injected into this
 * parameter when called. It is in the responsibility of the developer to ensure
 * that the types of the target method parameter and the injected method
 * parameter match.
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XParam {

	/**
	 * 
	 * @return the position of the parameter of the target method that is to be
	 *         injected into the called method.
	 *         <p>
	 *         1: the first parameter of the target method.
	 *         <p>
	 *         2: ...
	 */
	int n();
}
