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
 * {@link XReturn} if and only if the {@link XMethod#location() } is set to
 * {@Link XLocation#EXIT}. In this case the value of one of the arguments
 * of the target method will be injected into this parameter when called. It is
 * in the responsibility of the developer to ensure that the types of the target
 * method parameter and the injected method parameter match.
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XReturn {

}
