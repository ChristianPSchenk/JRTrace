/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The first parameter of the injected method may be annotated with
 * {@link XException} if and only if the {@link XMethod#location() } is set to
 * {@Link XLocation#EXCEPTION}. In this case the exception that is thrown
 * is assigned to the first parameter.
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XException {

}
