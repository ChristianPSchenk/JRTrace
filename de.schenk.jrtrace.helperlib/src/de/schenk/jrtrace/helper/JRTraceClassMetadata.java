/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.schenk.jrtrace.annotations.XClass;
import de.schenk.jrtrace.annotations.XClassLoaderPolicy;
import de.schenk.objectweb.asm.Type;

/**
 * Class to store the transformation information extracted from an annotated
 * class for enginex This class may not be safely used from multiple threads
 * once it is placed in the cache since it is not changed any more once created
 * 
 * @author Christian Schenk
 *
 */
public class JRTraceClassMetadata {

	/**
	 * The fully qualified name of the EngineX class that this metadata
	 * describes
	 */
	private String theClassName;
	private boolean valid = true;

	/**
	 * the classes entry in "external" notation (java.lang.Object) not
	 * java/lang/Object.
	 *
	 */
	private List<String> classes = new ArrayList<String>();

	List<JRTraceMethodMetadata> methods = new ArrayList<JRTraceMethodMetadata>();
	private byte[] classbytes;
	private boolean derived = false;
	private String classLoaderName;

	public void setClassName(String name) {
		this.theClassName = name;
	}

	/**
	 * 
	 * @return the value of the "classes" annotation on the EngineX class
	 */
	public List<String> getClasses() {
		List<String> erg = new ArrayList<String>();
		for (String c : classes) {
			erg.add(JRTraceNameUtil.getInternalName(c));
		}
		return erg;
	}

	/**
	 * The theClassName of the EngineX class this metadata represents.
	 * 
	 * @return
	 */
	public String getClassName() {
		return theClassName;
	}

	/**
	 * 
	 * @return true, if the class described by this object has valid EngineX
	 *         Annotations.
	 */
	public boolean hasXClassAnnotation() {
		return valid;
	}

	public void setHasNoXClassAnnotation() {
		valid = false;
	}

	public void addClassesEntry(String value) {
		classes.add(value);

	}

	public List<JRTraceMethodMetadata> getMethods() {

		return methods;

	}

	public void addMethod(JRTraceMethodMetadata method) {

		methods.add(method);

	}

	public void addBytes(byte[] enginexclass) {
		classbytes = enginexclass;

	}

	public byte[] getClassBytes() {
		return classbytes;
	}

	public String getExternalClassName() {
		return Type.getType("L" + theClassName + ";").getClassName();
	}

	/**
	 * checks whether the given class might be instrumented by this enginex
	 * script
	 * 
	 * This method checks the classname, the class hierarchy (if derived is
	 * true) and the method signatures.
	 * 
	 * @param theclass
	 *            a target class.
	 * 
	 * @return true, if this enginex class might require injection into the
	 *         target class
	 */
	public boolean mayMatch(Class<?> theclass) {

		Class<?> targetclass = theclass;

		boolean classMatch = false;
		if (!getDerived()) {
			classMatch = mayMatchClass(theclass);
		} else {
			classMatch = mayMatchClassHierarchy(theclass);
		}

		return classMatch && mayMatchMethods(targetclass);

	}

	/**
	 * 
	 * @param className
	 *            the name of the class
	 * @param superclass
	 *            the class of the superclass
	 * @param interfaces
	 *            the classes of the directly implemented interfaces
	 * @return
	 */
	public boolean mayMatchClassHierarchy(String className,
			Class<?> superclass, Class<?>[] interfaces) {
		if (!getDerived()) {
			return mayMatchClassName(className);
		}
		boolean result = mayMatchClassName(className)
				|| mayMatchClassHierarchy(superclass);
		if (result)
			return true;
		for (Class<?> iface : interfaces) {
			if (mayMatchClass(iface)) {
				return true;
			}
		}
		return false;
	}

	private boolean mayMatchClassHierarchy(Class<?> theclass) {

		if (mayMatchClass(theclass))
			return true;

		Class<?>[] interfaces = theclass.getInterfaces();
		for (Class<?> oneiface : interfaces) {
			if (mayMatchClass(oneiface))
				return true;

		}

		Class<?> superclass = theclass.getSuperclass();
		if (superclass == null)
			return false;

		return mayMatchClassHierarchy(superclass);
	}

	private boolean mayMatchMethods(Class<?> theclass) {

		for (JRTraceMethodMetadata method : methods) {

			if (method.mayMatch(theclass)) {
				return true;
			}

		}
		return false;
	}

	/**
	 * 
	 * @param theclass
	 *            targetclass
	 * 
	 * @return true, if the class annotations allow this to be a candidate
	 */
	private boolean mayMatchClass(Class<?> theclass) {
		String classname = theclass.getName();
		if (classname == null)
			return false;
		for (String excludedName : excludedClasses) {
			if (classname.matches(excludedName))
				return false;
		}
		return mayMatchClassName(classname);
	}

	public boolean mayMatchClassName(String classname) {
		if (classname == null)
			return false;

		for (String targetclass : classes) {

			if (useRegex) {
				if (classname.matches(targetclass)) {
					return true;
				}

			} else {
				if (classname.equals(targetclass)) {

					return true;
				}
			}
		}
		return false;
	}

	public boolean getDerived() {

		return derived;
	}

	public void setDerived(boolean value) {
		derived = value;

	}

	XClassLoaderPolicy classLoaderPolicy = XClassLoaderPolicy.BOOT;
	private boolean useRegex;
	private HashSet<String> excludedClasses = new HashSet<String>();
	private int classVersion;

	public XClassLoaderPolicy getClassLoaderPolicy() {
		return classLoaderPolicy;
	}

	public void setClassLoaderPolicy(XClassLoaderPolicy classLoaderPolicy) {
		this.classLoaderPolicy = classLoaderPolicy;
	}

	public void setClassLoaderName(String name) {
		this.classLoaderName = name;

	}

	public String getClassLoaderName() {
		return this.classLoaderName;

	}

	public void setUseRegex(boolean value) {
		useRegex = true;

	}

	public boolean getUseRegEx() {
		return useRegex;
	}

	@Override
	public String toString() {
		return getExternalClassName();
	}

	/**
	 * 
	 * 
	 * @param methodName
	 * @return the metadata of the specified method or null if the injection
	 *         class doesn't contain a method with this name
	 */
	public JRTraceMethodMetadata getMethod(String methodName) {
		for (JRTraceMethodMetadata m : methods) {
			if (m.getMethodName().equals(methodName)) {
				return m;
			}
		}
		return null;
	}

	public void addExcludedClass(String exclude) {
		excludedClasses.add(exclude);
	}

	/**
	 * @return the set of regular expressions that excludes classes from
	 *         instrumentation
	 */
	public Set<String> getExcludedClasses() {

		return excludedClasses;
	}

	/**
	 * 
	 * Checks whether a class of name classname is excluded from instrumentation
	 * via the {@link XClass#exclude} attribute
	 * 
	 * @param classname
	 * @return true, if this class is excludes.
	 */
	public boolean excludesClass(String classname) {
		if (classname == null)
			return false;
		for (String excludePattern : getExcludedClasses()) {
			if (classname.matches(excludePattern))
				return true;
		}
		return false;
	}

	public int getClassVersion() {
		return classVersion;
	}

	public void setClassVersion(int version) {
		this.classVersion = version;

	}

}
