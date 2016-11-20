/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If an argument of the injected method is annotated with {@link XField} the
 * value of the corresponding field from the target class is injected into this
 * argument.
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XField {

	/**
	 * 
	 * @return the name of the field to inject into this parameter. The field
	 *         can be static or non-static and the search will also extend to
	 *         fields in superclasses. Note: access to fields in superclasses
	 *         will internally rely on java reflection, while the access to
	 *         declared fields of the targetclass will use native
	 *         GETFIELD/PUTFIELD statements (less overhead).
	 */
	String name() default "";
}
