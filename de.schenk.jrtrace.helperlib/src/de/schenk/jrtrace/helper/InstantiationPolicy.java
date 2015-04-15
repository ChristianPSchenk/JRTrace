package de.schenk.jrtrace.helper;

public enum InstantiationPolicy {
	/**
	 * One object per classloader that is requested by the classloaderpolicy
	 */
	CLASSLOADER,
	/**
	 * One object per method that is instrumented
	 */
	METHOD
}
