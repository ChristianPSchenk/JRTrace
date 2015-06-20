/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helper;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.schenk.jrtrace.annotations.XLocation;
import de.schenk.jrtrace.annotations.XModifier;
import de.schenk.jrtrace.helperlib.JRLog;
import de.schenk.jrtrace.helperlib.status.InjectStatus;
import de.schenk.jrtrace.helperlib.status.StatusState;

public class JRTraceMethodMetadata {

	private JRTraceClassMetadata parent;

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

	public JRTraceMethodMetadata(JRTraceClassMetadata md, String name,
			String desc) {
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

	public JRTraceClassMetadata getClassMetadata() {
		return parent;
	}

	public void addTargetMethodName(String value) {
		targetMethodNames.add(value);

	}

	public String getMethodName() {
		return name;
	}

	/**
	 * null: no argument list provided = matches any argument list
	 */
	List<String> argumentList = null;

	/**
	 * a map describing the argument, return and field injections into the
	 * instrumented method: key: the argument position (0,...) value: Integer:
	 * 0: this, -1: return value, n>=1: paramter index, String: field injection.
	 */
	private Map<Integer, Injection> injection = new HashMap<Integer, Injection>();

	private XLocation injectLocation = XLocation.ENTRY;

	private String invokedethodName = "";

	private String invokedClass = "";

	private String fieldAccessClass = "";

	private String fieldAccessName = "";

	private List<XModifier> modifier = new ArrayList<XModifier>();

	public void addArgument(String value) {
		if (argumentList == null)
			argumentList = new ArrayList<String>();
		argumentList.add(value);
	}

	public List<String> getArgumentList() {
		return argumentList == null ? null : Collections
				.unmodifiableList(argumentList);
	}

	public boolean mayMatch(String methodName, String desc, int access) {
		return mayMatch(methodName, desc, access, null);
	}

	/**
	 * 
	 * @param methodName
	 * @param desc
	 * @param access
	 * @param status
	 *            .
	 * @return true, if the methodName and Descriptor passed in is matched by
	 *         this metadata.
	 */
	public boolean mayMatch(String methodName, String desc, int access,
			InjectStatus status) {
		if (mayMatchMethodName(methodName)) {
			String[] ps = MethodUtil.getParametersAsString(desc);
			if (mayMatchParameters(ps)) {

				if (mayMatchModifier(access)) {
					return true;
				} else {
					if (status != null) {
						status.setMessage(InjectStatus.MSG_METHOD_MODIFIERS_DONT_MATCH);
						status.setInjected(StatusState.DOESNT_INJECT);
					}
					return false;
				}
			} else {
				if (status != null) {
					status.setMessage(InjectStatus.MSG_PARAMETERS_DONT_MATCH);
					status.setInjected(StatusState.DOESNT_INJECT);
				}
				return false;
			}

		}
		if (status != null) {
			status.setMessage(InjectStatus.MSG_METHODNAME_DOESNT_MATCH);
			status.setInjected(StatusState.DOESNT_INJECT);
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
	public void addInjection(int targetposition, Injection source) {
		injection.put(targetposition, source);

	}

	public Map<Integer, Injection> getInjections() {
		return Collections.unmodifiableMap(injection);
	}

	public Injection getInjection(int i) {
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
	 * @param clazz
	 * @return true, if the clazz contains at least one method that might be in
	 *         the transformation scope of this jrtrace class
	 */
	public boolean mayMatch(Class<?> clazz) {

		Method[] methods = null;
		Constructor<?>[] constructors = null;
		try {
			constructors = clazz.getDeclaredConstructors();
			methods = clazz.getDeclaredMethods();

		} catch (NoClassDefFoundError e) {
			JRLog.error("getDeclaredMethods failed on Class<?> "
					+ clazz
					+ " with NoClassDefFoundError. The class can anyway probably not load and will therefore be ignored. Known cases when this happens: DynamicImport-Package in Osgi (probably also optional dependencies.)");
			return false;
		}

		for (int i = 0; i < methods.length; i++) {
			Method theMethod = methods[i];
			String name = theMethod.getName();
			if (mayMatchMethodName(name)) {
				if (mayMatchMethodSignature(theMethod))
					return true;
			}

		}
		for (int i = 0; i < constructors.length; i++) {
			Constructor<?> theMethod = constructors[i];
			if (mayMatchMethodName("<init>")) {
				if (mayMatchMethodSignature(theMethod))
					return true;
			}
		}
		return false;
	}

	private boolean mayMatchMethodSignature(Method theMethod) {
		return mayMatchSignature(theMethod.getModifiers(),
				theMethod.getParameterTypes());
	}

	private boolean mayMatchMethodSignature(Constructor<?> theConstructor) {
		return mayMatchSignature(theConstructor.getModifiers(),
				theConstructor.getParameterTypes());
	}

	private boolean mayMatchSignature(int modifiers, Class<?>[] parameterTypes) {

		String[] ptypesStrings = getParameterList(parameterTypes);

		if (mayMatchParameters(ptypesStrings)) {

			boolean modifierresult = mayMatchModifier(modifiers);
			if (modifierresult)
				return true;
		}

		return false;
	}

	private boolean mayMatchModifier(int modifiers) {

		for (XModifier modifier : getMethodModifiers()) {
			switch (modifier) {
			case PUBLIC:
				if (!((modifiers & Modifier.PUBLIC) == Modifier.PUBLIC))
					return false;
				break;
			case NOT_PUBLIC:
				if (!((modifiers & Modifier.PUBLIC) == 0))
					return false;
				break;
			case PRIVATE:
				if (!((modifiers & Modifier.PRIVATE) == Modifier.PRIVATE))
					return false;
				break;
			case NOT_PRIVATE:
				if (!((modifiers & Modifier.PRIVATE) == 0))
					return false;
				break;
			case PROTECTED:
				if (!((modifiers & Modifier.PROTECTED) == Modifier.PROTECTED))
					return false;
				break;
			case NOT_PROTECTED:
				if (!((modifiers & Modifier.PROTECTED) == 0))
					return false;
				break;
			case STATIC:
				if (!((modifiers & Modifier.STATIC) == Modifier.STATIC))
					return false;
				break;
			case NOT_STATIC:
				if (!((modifiers & Modifier.STATIC) == 0))
					return false;
				break;

			case FINAL:
				if (!((modifiers & Modifier.FINAL) == Modifier.FINAL))
					return false;
				break;
			case NOT_FINAL:
				if (!((modifiers & Modifier.FINAL) == 0))
					return false;
				break;

			}
		}
		return true;
	}

	/**
	 * 
	 * @param theMethod
	 * @return a string array containing the parameters of this method
	 */
	private String[] getParameterList(Class<?>[] parameterTypes) {

		String[] ptypesStrings = new String[parameterTypes.length];
		for (int j = 0; j < parameterTypes.length; j++) {
			ptypesStrings[j] = parameterTypes[j].getName();
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

	/**
	 * @return
	 */
	public String getInvokedMethodName() {

		return invokedethodName;
	}

	/**
	 * @param value
	 */
	public void setInvokedMethod(String value) {
		invokedethodName = value;

	}

	/**
	 * @param invokedMethodName
	 * @return true: if the passed in method name matches the
	 *         "invokedMethodName"
	 */
	public boolean matchesInvoker(String invokedMethodClass,
			String invokedMethodName) {

		return checkStringMatch(invokedMethodName, getInvokedMethodName())
				&& checkStringMatch(invokedMethodClass.replace('/', '.'),
						getInvokedMethodClass());
	}

	/**
	 * @param invokedMethodName
	 * @return
	 */
	private boolean checkStringMatch(String state, String match) {
		if (match.isEmpty())
			return true;
		if (this.parent.getUseRegEx()) {
			return state.matches(match);
		} else {
			return state.equals(match);
		}
	}

	/**
	 * @param value
	 */
	public void setInvokedClass(String value) {
		this.invokedClass = value;

	}

	/**
	 * @return
	 */
	public String getInvokedMethodClass() {
		return invokedClass;
	}

	/**
	 * @return
	 */
	public String getFieldAccessClass() {

		return fieldAccessClass;
	}

	/**
	 * @return
	 */
	public String getFieldAccessName() {

		return fieldAccessName;
	}

	/**
	 * @param value
	 *            the name of the class to restrict instrumentation of field
	 *            access (GETFIELD/PUTFIELD)
	 */
	public void setFieldAccessClass(String value) {
		fieldAccessClass = value;

	}

	/**
	 * @param value
	 *            the name of the field to restrict instrumentation of field
	 *            access (GETFIELD/PUTFIELD)
	 */
	public void setFieldAccessName(String value) {
		this.fieldAccessName = value;

	}

	public boolean matchesField(String fieldClassName, String fieldMethodName) {
		return checkStringMatch(fieldMethodName, getFieldAccessName())
				&& checkStringMatch(fieldClassName.replace('/', '.'),
						getFieldAccessClass());
	}

	public XModifier[] getMethodModifiers() {
		return modifier.toArray(new XModifier[modifier.size()]);
	}

	public void addModifier(XModifier valueOf) {
		modifier.add(valueOf);

	}

}
