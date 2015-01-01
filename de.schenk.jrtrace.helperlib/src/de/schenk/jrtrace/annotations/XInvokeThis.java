/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * If a parameter of the injected method is annotated with {@link XInvokeThis} the
 * target of the current invocation will be injected into this parameter when called.
*  This is possible only for the injection points {@link XLocation#REPLACE_INVOCATION},
*  {@link XLocation#BEFORE_INVOCATION} and {@link XLocation#AFTER_INVOCATION}.
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
public @interface XInvokeThis {

}
