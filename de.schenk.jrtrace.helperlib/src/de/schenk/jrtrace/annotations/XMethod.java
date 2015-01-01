/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Methods inside of a class annotated with {@link XClass} can be annotated with
 * {@link XMethod} to define that they are intended to be called from the target
 * class. The attributes define the specific call locations.
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface XMethod {
	/**
	 * 
	 * @return a list with one or more method names. If this attribute is set
	 *         only methods that exactly match one of these names are considered
	 *         as potential target for injection. If this attribute is not set,
	 *         all methods inside the target class are potential target for
	 *         injection.
	 * 
	 */
	String[] names() default "";

	/**
	 * 
	 * @return a list with fully qualified argument types. If this attribute is
	 *         set only methods that match the provided argument list exactly
	 *         will be targets for injection. If this attribute is not set, the
	 *         argument list is not considered when deciding whether to inject
	 *         code. An empty argument list requirement is specified as
	 *         <code>arguments={}</code>
	 * 
	 */
	String[] arguments() default "";

	/**
	 * @return location specifies, where to inject the code. See
	 *         {@link XLocation} for the options. If not set, the injection will
	 *         take place at method entry see {@link XLocation.ENTRY}
	 * 
	 */
	XLocation location() default XLocation.ENTRY;

	/** 
	 * 
	 * @return valid only for {@link XLocation.BEFORE_INVOCATION} and similar: the name of the 
	 * method that is invoked. If not set, any method will match. If set, any method with the given name
	 * will match. If set and the class has "useregex" set, will match any method name that
	 * matches the regular expression provided.
	 */
  String invokedname() default "";
}
