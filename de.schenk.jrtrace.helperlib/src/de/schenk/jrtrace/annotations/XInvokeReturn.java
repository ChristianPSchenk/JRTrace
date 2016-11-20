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
 * {@link XInvokeReturn} if and only if the {@link XMethod#location() } is set to
 * {@Link XLocation#AFTER_INVOCATION}. In this case the value of the return value of
 * the previously invoked method will be available in the first paramter.
 * 
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XInvokeReturn {

}
