/**
 * (c) 2014 by Christian Schenk
 **/
package de.schenk.jrtrace.helperlib;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InspectUtil {

  /**
   * when using the detail formatter, the detailformatterobject wil be used
   * to search for formatting methods.
   */
	private Object detailFormatterObject=null;
	private ClassLoader classLoader;

	public InspectUtil() {
		
	}

	public InspectUtil(Object detailFormatterObject)
	{
	  this.detailFormatterObject=detailFormatterObject;
	}
	


	private int linebreaklimit = 120;
	private boolean skipStatics = true;
	private boolean useQualifiedClassNames = false;
	private HashSet<Object> parentTracker;
	private boolean skipOuterClassReference = true;
	private boolean skipNullFields = true; /* don't show fields with value null */
	private Set<String> toStringClasses;
	private Set<String> skipFields;
	private HashMap<String, String> detailFormatter;

	public String inspect(Object o) {
		return inspect(o, 2, null, null, false);
	}

	public String inspect(Object o, int depth, List<String> toStringClasses,
			List<String> skipFields, boolean includeStatics) {
		return inspect(o, depth, toStringClasses, skipFields, includeStatics,
				null);
	}

	/**
	 * 
	 * @param o
	 *            the object to investigate
	 * @param depth
	 *            recursion depth
	 * @param toStringClasses
	 *            all classes containing any of these strings will be formatted
	 *            with toString instead of with inspect
	 * @param skipFields
	 *            list of field usedForNames that will not be followed
	 * @param includeStatics
	 *            include static fields
	 * @param detailFormatter
	 *            a map that specifies methods for named fields. If a field with a specified
	 *            name is found, the specified method will be invoked to format it.
	 *            The method must take an object parameter and must return a String or a List<String>
	 * @return a string representation of the object
	 */
	public String inspect(Object o, int depth, List<String> toStringClasses,
			List<String> skipFields, boolean includeStatics,
			Map<String, String> detailFormatter) {
		skipStatics = !includeStatics;
		this.toStringClasses = new HashSet<String>();
		if (toStringClasses != null)
			this.toStringClasses.addAll(toStringClasses);
		this.skipFields = new HashSet<String>();
		if (skipFields != null)
			this.skipFields.addAll(skipFields);

		this.detailFormatter = new HashMap<String, String>();
		if (detailFormatter != null) {
			this.detailFormatter.putAll(detailFormatter);
		}

		parentTracker = new HashSet<Object>();
		StringBuffer result = new StringBuffer();
		List<String> formatted = formattedFieldValue(o, depth);
		for (int i = 0; i < formatted.size(); i++) {
			result.append(formatted.get(i));
			result.append("\n");
		}

		return result.toString();
	}

	private List<String> formattedField(Object o, String prefix, int depth) {

		List<String> fieldValue = formattedFieldValue(o, depth);

		// one line values are just prefixed with the fieldname
		if (fieldValue.size() == 1) {
			fieldValue.set(0, prefix + fieldValue.get(0));
			return fieldValue;
		}
		// multi line values are indented
		List<String> result = new ArrayList<String>();
		if (!prefix.isEmpty())
			result.add(prefix);
		for (int i = 0; i < fieldValue.size(); i++) {
			result.add((prefix.isEmpty() ? "" : " ") + fieldValue.get(i));
		}
		return result;
	}

	private List<String> formattedFieldValue(Object value, int depth) {
		if (parentTracker.contains(value)) {
			ArrayList<String> result = new ArrayList<String>();
			result.add("<parent>");
			return result;
		}

		if (value instanceof Object)
			parentTracker.add(value);
		try {
			List<String> arrayList = new ArrayList<String>();
			if (value instanceof String) {

				arrayList.add("\"" + value + "\"");
				return arrayList;

			}
			if (value == null)
				arrayList.add("null");
			else {
				if (value instanceof Boolean)
					arrayList.add(String.format("%b", value));
				if (value instanceof Integer)
					arrayList.add(String.format("%d", value));
				if (value instanceof Double)
					arrayList.add(String.format("%f", value));
				if (value instanceof Byte)
					arrayList.add(String.format("%d", value));
				if (value instanceof Long)
					arrayList.add(String.format("%d", value));
				if (value instanceof Float)
					arrayList.add(String.format("%f", value));
				if (value instanceof Character)
					arrayList.add(String.format("'%c'", value));
				if (value instanceof Enum) {
					arrayList.add(value.toString());
				}

			}

			if (!arrayList.isEmpty())
				return arrayList;

			if (depth == 0) {
				arrayList.add("(depth reached)");
				return arrayList;
			}

			if (value instanceof Map<?, ?>) {
				try {
					return formattedMap(value, depth - 1);
				} catch (RuntimeException e) {
					// some maps throw unsupportedoperationexceptions on map
					// operations.
					// So if a Map cannot be formatted as map, fall through to
					// the next best option
				}
			}
			if (value instanceof Collection<?>) {
				try {
					return formattedCollection(value, depth - 1);
				} catch (RuntimeException e) {
					// some collections throw unsupportedoperationexceptions on
					// map operations.
					// So if a Collection cannot be formatted as collection,
					// fall through to
					// the next best option
				}
			}
			if (value.getClass().isArray()) {
				return formattedArray(value, depth - 1);
			}
			if (arrayList.isEmpty()) {

				return formattedObject(value, depth - 1);

			}

			return arrayList;
		} finally {
			parentTracker.remove(value);
		}
	}

	private List<String> formattedArray(Object value, int depth) {

		String classname = "Array";

		List<List<String>> allfields = new ArrayList<List<String>>();
		for (int i = 0; i < Array.getLength(value); i++) {
			List<String> formattedField = formattedField(Array.get(value, i),
					"", depth);
			allfields.add(formattedField);
		}

		return format(classname, allfields);

	}

	private List<String> format(String classname, List<List<String>> allfields) {
		return format(classname, ", ", allfields);
	}

	private List<String> formattedCollection(Object value, int depth) {

		Collection<?> arrayValue = (Collection<?>) (value);
		String classname = "Collection";

		List<List<String>> allfields = new ArrayList<List<String>>();
		for (Object entry : arrayValue) {
			List<String> formattedField = formattedField(entry, "", depth);
			allfields.add(formattedField);
		}

		return format(classname, allfields);

	}

	private List<String> formattedMap(Object value, int depth) {

		Map<?, ?> arrayValue = (Map<?, ?>) (value);
		String classname = "Map";

		List<List<String>> allfields = new ArrayList<List<String>>();

		Set<?> keySet = arrayValue.keySet();
		for (Object keyObject : keySet) {
			List<List<String>> oneentryfields = new ArrayList<List<String>>();
			List<String> key = formattedField(keyObject, "", depth);
			List<String> mapvalue = formattedField(arrayValue.get(keyObject),
					"", depth);
			oneentryfields.add(key);
			oneentryfields.add(mapvalue);
			ArrayList<String> result = format("", ">", oneentryfields);
			allfields.add(result);
		}

		return format(classname, allfields);

	}

	private List<String> formattedObject(Object value, int depth) {

		if (isAToStringClass(value)) {
			List<String> result = new ArrayList<String>();
			String[] split = value.toString().split("\n");

			result.addAll(Arrays.asList(split));
			return result;
		}

		Field[] fields = ReflectionUtil.getFields(value);
		String classname;

		classname = value.getClass().toString();
		if (!useQualifiedClassNames) {
			int lastIndex = classname.lastIndexOf('.');
			if (lastIndex != -1) {
				classname = classname.substring(lastIndex + 1,
						classname.length());
			}

		}
		List<List<String>> allfields = new ArrayList<List<String>>();
		for (int i = 0; i < fields.length; i++) {
			String fieldName = fields[i].getName();
			if (isSkipField(fieldName))
				continue;

			if (skipStatics && Modifier.isStatic(fields[i].getModifiers()))
				continue;
			if (skipOuterClassReference && fieldName.equals("this$0"))
				continue;
			Object fieldValue = ReflectionUtil
					.getPrivateField(value, fields[i]);
			if (fieldValue == null && skipNullFields)
				continue;

			List<String> formattedField = null;
			String detailFormat = detailFormatter.get(fieldName);
			if (detailFormat != null) {
				formattedField = getDetailedFormat(detailFormat, fieldValue);
			} else {
				formattedField = formattedField(fieldValue, fieldName + "=",
						depth);
			}
			allfields.add(formattedField);
		}

		List<String> result = format(classname, allfields);
		return result;
	}

	/**
	 * Formats the value object using a detail formatter
	 * 
	 * @param detailFormat
	 *            the name of the method to invoke on the detailFormatterObject, should return
	 *            String or List<String> and must take one parameter of the field type
	 * @param fieldValue
	 * @return the formatted value
	 */
	@SuppressWarnings("unchecked")
	private List<String> getDetailedFormat(String detailFormat,
			Object fieldValue) {
		Object erg = ReflectionUtil.invokeMethod(detailFormatterObject, detailFormat,fieldValue);
		if (erg instanceof List<?>)
			return (List<String>) erg;
		if (erg instanceof String) {
			ArrayList<String> result = new ArrayList<String>();
			result.add((String) erg);
			return result;
		}

		throw new RuntimeException("Error while evaluating " + detailFormat
				+ " on " + fieldValue.toString()
				+ ". Formatter must return String or List<String>");
	}

	private boolean isSkipField(String name) {
		if (skipFields.contains(name))
			return true;
		return false;
	}

	/**
	 * 
	 * 
	 * @param value
	 * @return true for those classes that should be displayed using toString()
	 */
	private boolean isAToStringClass(Object value) {

		Class<?> c = value.getClass();
		while (c != null) {
			String classname = c.getSimpleName();
			if (toStringClasses.contains(classname)) {
				return true;
			}
			Class<?>[] ifs = c.getInterfaces();
			for (Class<?> iface : ifs) {
				if (toStringClasses.contains(iface.getSimpleName())) {
					return true;
				}
			}
			c = c.getSuperclass();
		}

		return false;
	}

	private ArrayList<String> format(String description,
			String onelineseparator, List<List<String>> allfields) {
		boolean hasMultiLineField = false;

		for (int i = 0; i < allfields.size(); i++) {
			if (allfields.get(i).size() > 1)
				hasMultiLineField = true;

		}

		ArrayList<String> result = new ArrayList<String>();
		if (!hasMultiLineField) {
			StringBuffer erg = new StringBuffer();
			erg.append(description + " [");
			for (int i = 0; i < allfields.size(); i++) {
				String next = allfields.get(i).get(0);
				if (erg.length() + next.length() + onelineseparator.length() >= linebreaklimit) {
					result.add(erg.toString());
					erg = new StringBuffer();
				}

				if (i > 0)
					erg.append(onelineseparator);
				erg.append(next);

			}
			erg.append("]");
			result.add(erg.toString());

		} else {
			result.add(description + " [");
			for (int i = 0; i < allfields.size(); i++) {
				for (int j = 0; j < allfields.get(i).size(); j++)
					result.add(" " + allfields.get(i).get(j));
			}
			result.add("]");
		}
		return result;
	}

	public String inspect(Object object, int i) {
		return inspect(object, i, null);
	}

	public String inspect(Object object, int depth, List<String> toStringClasses) {

		return inspect(object, depth, toStringClasses, null, false);
	}

}
