/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.enginex.helper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.schenk.jrtrace.annotations.XLocation;

public class EngineXMethodMetadata {

	private EngineXMetadata parent;

	/**
	 * The UsedFor annotation values to determine for which target methods this
	 * is called.
	 */
	Set<String> targetMethodNames = new HashSet<String>();

	/**
	 * The name of the method in the enginex class.
	 */
	private String name;
	/**
	 * The descriptor of the method in the enginex class
	 */
	private String parameters;

	public EngineXMethodMetadata(EngineXMetadata md, String name, String desc) {
		this.name = name;
		this.parameters = desc;
		this.parent = md;

	}

	public Set<String> getTargetMethodNames() {
		return Collections.unmodifiableSet(targetMethodNames);
	}

	public String getDescriptor() {
		return parameters;
	}

	public EngineXMetadata getClassMetadata() {
		return parent;
	}

	public void addTargetMethodName(String value) {
		targetMethodNames.add(value);

	}

	public String getMethodName() {
		return name;
	}

	/**
	 * null: no argument list provided.
	 */
	List<String> argumentList = null;

	private Map<Integer, Object> injection = new HashMap<Integer, Object>();

	private XLocation injectLocation = XLocation.ENTRY;

	public void addArgument(String value) {
		if (argumentList == null)
			argumentList = new ArrayList<String>();
		argumentList.add(value);
	}

	public List<String> getArgumentList() {
		return argumentList == null ? null : Collections
				.unmodifiableList(argumentList);
	}

	/**
	 * 
	 * @param methodName
	 * @param desc
	 * @return true, if the methodName and Descriptor passed in is matched by
	 *         this metadata.
	 */
	public boolean mayMatch(String methodName, String desc) {
		if (mayMatchMethodName(methodName)) {
			String[] ps = MethodUtil.getParametersAsString(desc);
			if (mayMatchParameters(ps))
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param targetposition
	 *            the parameter index to set
	 * @param source
	 *            the source index to take this from (0=this,1=first
	 *            parameter,...) -or- the source field name to inject here
	 */
	public void addInjection(int targetposition, Object source) {
		injection.put(targetposition, source);

	}

	public Map<Integer, Object> getInjections() {
		return Collections.unmodifiableMap(injection);
	}

	public Object getInjection(int i) {
		return injection.get(i);
	}

	public XLocation getInjectLocation() {
		return injectLocation;
	}

	public void setInjectLocation(XLocation valueOf) {
		this.injectLocation = valueOf;

	}

	/**
	 * 
	 * @param methodName
	 * @return true if the method name is matched by this metadata
	 */
	private boolean mayMatchMethodName(String methodName) {
		if (getTargetMethodNames().isEmpty())
			return true;
		if (!parent.getUseRegEx()) {
			for (String targetMethodMatcher : getTargetMethodNames()) {
				if (targetMethodMatcher.equals(methodName))
					return true;

			}
			return false;
		} else {

			for (String targetMethodMatcher : getTargetMethodNames()) {
				if (methodName.matches(targetMethodMatcher))
					return true;
			}
			return false;
		}
	}

	/**
	 * 
	 * @param theclass
	 * @return true, if the theclass contains at least one method that might be
	 *         in the transformation scope of this enginexmethod
	 */
	public boolean mayMatch(Class<?> theclass) {
		Method[] methods = theclass.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			String name = methods[i].getName();
			if (mayMatchMethodName(name)) {
				Method theMethod = methods[i];
				String[] ptypesStrings = getParameterList(theMethod);
				if (mayMatchParameters(ptypesStrings))
					return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param theMethod
	 * @return a string array containing the parameters of this method
	 */
	private String[] getParameterList(Method theMethod) {
		Class<?>[] ptypes = theMethod.getParameterTypes();
		String[] ptypesStrings = new String[ptypes.length];
		for (int j = 0; j < ptypes.length; j++) {
			ptypesStrings[j] = ptypes[j].getName();
		}
		return ptypesStrings;
	}

	/**
	 * 
	 * @param ptypesStrings
	 *            an array with argument types
	 * @return true, if the list matches the method metadata description
	 */
	private boolean mayMatchParameters(String[] ptypesStrings) {
		if (this.argumentList == null)
			return true;
		if (ptypesStrings.length != argumentList.size())
			return false;
		if (!parent.getUseRegEx()) {

			for (int i = 0; i < ptypesStrings.length; i++) {

				String argType = ptypesStrings[i];
				if (!argType.equals(argumentList.get(i)))
					return false;
			}
			return true;

		} else {
			for (int i = 0; i < ptypesStrings.length; i++) {
				String argType = ptypesStrings[i];
				if (!argType.matches(argumentList.get(i))) {
					return false;
				}
			}
			return true;
		}

	}

	public void setArgumentListEmpty() {
		argumentList = Collections.emptyList();

	}

}
