/**
 * (c) 2014 by Christian Schenk.
 */
package de.schenk.jrtrace.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Terms:
 * <p>
 * The framework will work with a "target" java application and provides the
 * ability to inject method calls into methods of the target java application
 * (into "target methods" of "target classes").
 * <p>
 * The classes and methods that are inserted into the target application are
 * referred to as the "injected classes" or "injected methods".
 * <p>
 * 
 * The XClass annotation annotates classes that contain methods that should be
 * injected into other java classes.
 * <p>
 * The current injection strategy will create one Class<?> and one instance of
 * this class for each ClassLoader and use the respective Class<?> and instance
 * for injection. So all target classes that share a ClassLoader will also share
 * a Class<?> and an instance of this class.
 * 
 * 
 * @author Christian Schenk
 *
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface XClass {

	/**
	 * 
	 * 
	 * 
	 * @return This annotation can specify one ore more fully qualified
	 *         classnames. Only target classes or interfaces that match one of
	 *         these classnames are potential targets for code injection from
	 *         this class. Which classes qualify as targets for injection can
	 *         also be influenced using the annotation {@link #derived()}.
	 *         <p>
	 *         If not set, this class will not inject any code. An XClass might
	 *         still be useful, since it can be used by other code injected and
	 *         the {@link #classloaderpolicy()} attribute can be used to specify
	 *         the ClassLoader that will be used for it.
	 * 
	 */
	String[] classes() default "";

	/**
	 * 
	 * 
	 * @return the injected class can be loaded with different ClassLoaders. See
	 *         {@link XClassLoaderPolicy} for a description of the available
	 *         options. Depending on this option, injected code will be loaded
	 *         with different ClassLoaders. The default is to load the class
	 *         with a classloader that can access the bootclasspath only.
	 *         <p>
	 *         Example:
	 * 
	 *         <pre>
	 *         @XClass(classloaderpolicy=XClassLoaderPolicy.BOOT)
	 * </pre
	 * 
	 * 
	 */
	XClassLoaderPolicy classloaderpolicy() default XClassLoaderPolicy.BOOT;

	/**
	 * 
	 * @return if {@link XClassLoaderPolicy#NAMED} is selected the name of a
	 *         class has to be specified. The ClassLoader of this class will be
	 *         the used for the injected code.
	 */
	String classloadername() default "";

	/**
	 * 
	 * @return if derived is set to true, all classes that are derived from the
	 *         classes or interfaces specified in {@link #classes()} become
	 *         potential targets for code injection.
	 */
	boolean derived() default false;

	/**
	 * 
	 * @return if set to true, regular expressions will be used to test
	 *         classnames, methodnames and argument names.
	 * 
	 *         <p>
	 *         Example:
	 *         </p>
	 * 
	 *         <pre>
	 * @XClass(classes="org\\.eclipse.*",regex=true)
	 * </pre>
	 * 
	 *         Methods from this Class maybe injected in all classes from the
	 *         namespace <b>org.eclipse</b>
	 *         <p>
	 *         Note: if regex is set to true, the java.util.regex.Pattern class
	 *         is excluded from including tracing code.
	 * 
	 */
	boolean regex() default false;

	/**
	 * 
	 * @return a collection of regular expressions. All classes matching will be
	 *         excluded from potential instrumentation from this {@link XClass}.
	 *         Note that this is always a regular expression and the
	 *         {@link XClass#regex()} flag has no influence here.
	 * 
	 */
	String[] exclude() default "";

}
