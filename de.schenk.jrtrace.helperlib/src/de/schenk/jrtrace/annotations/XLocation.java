/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

/**
 * Method calls can be injected in different locations:
 * <p>
 * ENTRY: method is called directly after entering the target method. Methods
 * injected at ENTRY are required to have a {@link void} return type.
 * <p>
 * EXIT: method is called directly before returning from a target method. If a
 * method is injected at EXIT, it can either have a {@link void} return type or
 * a return type that matches the return type of the method it is injected into.
 * In the later case, the value returned from the injected code will become the
 * return value of the target method. This allows to alter return values of
 * methods by injecting code.
 * 
 * @author Christian Schenk
 *
 */
public enum XLocation {
	/**
	 * Injected code will be called at target method entry.
	 */
	ENTRY,
	/**
	 * Injected code will be called before target method return.
	 */
	EXIT, 
	/**
	 * Injected code will be called before any method is invoked. 
	 * (restrict 
     * method names to {@link XMethod.invokedname}.)
	 */
	BEFORE_INVOCATION,
	/**
     * Injected code will be called after any method is invoked. (restrict 
     * method names to {@link XMethod.invokedname}.) 
     */
    AFTER_INVOCATION,
    /**
     * Injected code will be called instead of invoking any method (restrict 
     * method names to {@link XMethod.invokedname}.)
     */
    REPLACE_INVOCATION,
	
}
