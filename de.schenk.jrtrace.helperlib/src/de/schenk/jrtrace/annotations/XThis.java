/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a parameter of the injected method is annotated with {@link XThis} the
 * object of the target method will be injected into this parameter when called.
 * It is in the responsibility of the developer to ensure that the types of the
 * target object and the type of the method parameter match. 
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XThis {

}
