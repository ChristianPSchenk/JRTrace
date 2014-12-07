/**
* (c) 2014 by Christian Schenk
**/
package de.schenk.jrtrace.helperlib;

import java.util.HashMap;

public class GroovyScriptCache {

	HashMap<CacheKey, CachedGroovyScript> cache = new HashMap<CacheKey, CachedGroovyScript>();

	void putScript(String expression, ClassLoader cl,
			CachedGroovyScript scriptObject) {
		cache.put(new CacheKey(expression, cl), scriptObject);
	}

	CachedGroovyScript getScript(String expression, ClassLoader cl) {
		return cache.get(new CacheKey(expression, cl));
	}

	public void clear() {
		cache.clear();

	}
}
