package de.schenk.jrtrace.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Storage Module:
 * 
 * Stores the jrtrace definitions for all required jrtrace class set versions
 * and classes and provides convenience methods for accessing and manipulating
 * them.
 * 
 * Not synchronized. Access has to be synchronized.
 * 
 * @author Christian Schenk
 *
 */
public class JRTraceClassStore {

	/**
	 * Usually during transformation, instrumented classes with the current
	 * jrtrace set id and the previous jrtrace set id are met. But in tests I
	 * have encountered situations when even older variants were hit. This
	 * number is a tradeoff between memory (how many old versions of JRTrace
	 * classes to keep?) and safety (all version=full security).
	 * 
	 * Cases that might happen: - a method that is instrumented with EXIT is
	 * executed and calls other code in expensive sub methods. While this
	 * happens jrtrace sets are reinstalled a few times. Since the method is
	 * active it may not be replaced with the new version and the call site
	 * initialization may happen for a rather old jrtrace set id.
	 */
	private static final int OLDVERSION_CACHE_SIZE = 5;

	LinkedList<Integer> ids = new LinkedList<Integer>();

	/**
	 * Integer: jrtrace class set id String: the jrtrace class name.
	 */
	Map<Integer, Map<String, JRTraceClassAndObjectCache>> classCache = new HashMap<Integer, Map<String, JRTraceClassAndObjectCache>>();

	public Collection<JRTraceClassMetadata> getAllForId(int currentCacheId) {

		Collection<JRTraceClassAndObjectCache> values = getMapForId(
				currentCacheId).values();
		ArrayList<JRTraceClassMetadata> result = new ArrayList<JRTraceClassMetadata>();
		for (JRTraceClassAndObjectCache value : values) {
			result.add(value.getMetadata());
		}
		return result;
	}

	private Map<String, JRTraceClassAndObjectCache> getMapForId(
			int currentCacheId) {
		Map<String, JRTraceClassAndObjectCache> map = classCache
				.get(currentCacheId);
		if (map == null) {

			map = new HashMap<String, JRTraceClassAndObjectCache>();
			classCache.put(currentCacheId, map);
			ids.add(currentCacheId);
			if (ids.size() > OLDVERSION_CACHE_SIZE) {
				Integer id = ids.getFirst();
				classCache.remove(id);

			}
		}
		return map;
	}

	public JRTraceClassAndObjectCache get(int cacheId, String enginexclass) {
		Map<String, JRTraceClassAndObjectCache> map = getMapForId(cacheId);

		return map.get(enginexclass);

	}

	public void put(int cacheId, String externalClassName,
			JRTraceClassAndObjectCache jrTraceClassAndObjectCache) {
		Map<String, JRTraceClassAndObjectCache> map = getMapForId(cacheId);
		map.put(externalClassName, jrTraceClassAndObjectCache);

	}

}
