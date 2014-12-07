/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.annotations;

/**
 * The enumeration of options for the ClassLoader to use when loading injected
 * classes. The default value is BOOT.
 * <p>
 * TARGET: the injected code is loaded using the ClassLoader of the target class
 * (the class into which the code is injected). As a consequence, if the same
 * code is injected in different classes that use different ClassLoaders
 * multiple Class objects and Objects are created, one for each ClassLoader.
 * <p>
 * BOOT: the injected code is loaded using the root ClassLoader null. Code
 * injected with the root ClassLoader will only have access to the core java and
 * the few libraries directly loaded into the boot class path
 * <p>
 * NAMED: an arbitrary class can be specified using
 * {@link XClass#classloadername()}. When the code is injected the ClassLoader
 * of this class is identified and used to load the injected code.
 * 
 * 
 * 
 * @author Christian Schenk
 *
 */
public enum XClassLoaderPolicy {
	TARGET, BOOT, NAMED
}
