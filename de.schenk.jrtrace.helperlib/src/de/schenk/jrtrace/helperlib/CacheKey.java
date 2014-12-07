/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

public class CacheKey {
	String expression;
	ClassLoader classLoader;

	public CacheKey(String e, ClassLoader c) {
		classLoader = c;
		expression = e;
	}

	@Override
	public int hashCode() {
		return expression.hashCode() + 7
				* (classLoader == null ? 0 : classLoader.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CacheKey))
			return false;
		CacheKey key2 = (CacheKey) obj;
		if (!expression.equals(key2.expression))
			return false;
		if (classLoader == null && key2.classLoader != null)
			return false;
		if (classLoader != null && key2.classLoader == null)
			return false;
		if (classLoader == null && key2.classLoader == null)
			return true;
		if (!classLoader.equals(key2.classLoader))
			return false;
		return true;

	}
}